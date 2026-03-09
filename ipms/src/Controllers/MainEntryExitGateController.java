package Controllers;

import Data.Event;
import Drivers.output.FloorDisplayDriver;
import Drivers.output.GateOutputDriver;
import Drivers.output.MainEntranceDisplayDriver;
import Drivers.output.MainGateMechanismDriver;

/**
 * MainEntryExitGateController coordinates entry/exit sensors, gate mechanism,
 * and IPMS. Receives sensor events, forwards them to IPMSController, and
 * controls the physical gate via Drivers.output.MainGateMechanismDriver.
 */
public class MainEntryExitGateController {

    boolean entrySensorActive;
    boolean exitSensorActive;
    boolean gateOpen;

    IPMSController ipms;
    MainGateMechanismDriver gateDriver;
    GateOutputDriver gateOutputDriver;

    MainEntranceDisplayDriver entranceDisplayDriver;
    FloorDisplayDriver floorDisplayDriver;

    /**
     * MainEntryExitGateController constructor.
     * @param ipms - the IPMS controller that manages the overall parking
     *             system.
     */
    public MainEntryExitGateController(IPMSController ipms) {

        this.ipms = ipms;
        this.gateOpen = false;
        this.gateDriver = new MainGateMechanismDriver();
    }

    /**
     * Links gate output driver so that gate animations are sent to the GUI.
     * Called from initSystem in ParkingGUI.
     * @param driver - the gate output driver.
     */
    public void setGateOutputDriver(GateOutputDriver driver) {
        this.gateOutputDriver = driver;
    }

    /**
     * Called when entry sensor detects a vehicle; sends ENTRY event to IPMS.
     */
    public void entrySensorTriggered() {

        entrySensorActive = true;
        sendEventToIPMS(new Event("ENTRY"));
    }

    /**
     * Links the main entrance display driver so availability updates are
     * sent to the Main Display
     * @param driver - the main entrance display driver.
     */
    public void setMainEntranceDisplayDriver(MainEntranceDisplayDriver driver) {
        this.entranceDisplayDriver = driver;
    }

    /**
     * Links the floor display driver so availability updates are sent to
     * the floor displays.
     * @param driver the floor display driver
     */
    public void setFloorDisplayDriver(FloorDisplayDriver driver) {
        this.floorDisplayDriver = driver;
    }

    /**
     * Called when exit sensor detects a vehicle; sends EXIT event to IPMS.
     */
    public void exitSensorTriggered() {

        exitSensorActive = true;
        sendEventToIPMS(new Event("EXIT"));
    }

    /**
     * Forwards events to the main IPMS controller
     * @param e
     */
    public void sendEventToIPMS(Event e) {
        ipms.processInput(e);
    }

    /**
     * Raises the main gate via Drivers.output.MainGateMechanismDriver and
     * updates local state.
     */
    public void raiseMainGate() {

        gateDriver.openGate();
        gateOpen = true;
        System.out.println("gate opened");

        //sends signal to physical hardware and Triggers GUI animation
        if (gateOutputDriver != null) {
            gateOutputDriver.openEntryGate();
        }
    }

    /**
     * Lowers the main gate via MainGateMechanismDriver and updates local state.
     */
    public void closeMainGate() {

        gateDriver.closeGate();
        gateOpen = false;
        System.out.println("gate closed");

        //sends signal to physical hardware and triggers GUI animation
        if (gateOutputDriver != null) {
            gateOutputDriver.closeEntryGate();
        }
    }

    /**
     * Updates the entrance and floor displays with current availability counts
     * @param totalAvailable total available spots in the structure
     * @param floorAvailable number of available spots per floor
     */
    public void updateDisplay(int totalAvailable, int[] floorAvailable) {

        System.out.println("display updated: " + totalAvailable +
                " spots available");

        if (entranceDisplayDriver != null) {
            entranceDisplayDriver.updateEntranceDisplay(
                    totalAvailable,
                    ipms.getTotalSpots(),
                    floorAvailable
            );
        }

        if (floorDisplayDriver != null && floorAvailable.length > 0) {
            floorDisplayDriver.updateFloorDisplay(floorAvailable[0],
                    ipms.getTotalSpots());
        }
    }
}