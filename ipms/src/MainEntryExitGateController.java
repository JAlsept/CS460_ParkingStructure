/**
 * MainEntryExitGateController - coordinates entry/exit sensors, gate mechanism,
 * and IPMS. Receives sensor events, forwards them to IPMSController, and
 * controls the physical gate via MainGateMechanismDriver.
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

    public MainEntryExitGateController(IPMSController ipms) {
        this.ipms = ipms;
        this.gateOpen = false;
        this.gateDriver = new MainGateMechanismDriver();
    }

    // links gate output driver so that gate animations are sent to the GUI
    public void setGateOutputDriver(GateOutputDriver driver) {
        this.gateOutputDriver = driver;
    }

    public void setMainEntranceDisplayDriver(MainEntranceDisplayDriver driver) {
        this.entranceDisplayDriver = driver;
    }

    public void setFloorDisplayDriver(FloorDisplayDriver driver) {
        this.floorDisplayDriver = driver;
    }

    // called when entry sensor detects a vehicle
    public void entrySensorTriggered() {
        entrySensorActive = true;
        sendEventToIPMS(new Event("ENTRY"));
    }

    // called when exit sensor detects a vehicle
    public void exitSensorTriggered() {
        exitSensorActive = true;
        sendEventToIPMS(new Event("EXIT"));
    }

    // forwards events to the main IPMS controller
    public void sendEventToIPMS(Event e) {
        ipms.processInput(e);
    }

    // raises the entry gate and triggers GUI animation via output driver
    public void raiseMainGate() {
        gateDriver.openGate();
        gateOpen = true;
        System.out.println("gate opened");
        if (gateOutputDriver != null) {
            gateOutputDriver.openEntryGate();
        }
    }

    // lowers the entry gate and triggers GUI animation via output driver
    public void closeMainGate() {
        gateDriver.closeGate();
        gateOpen = false;
        System.out.println("gate closed");
        if (gateOutputDriver != null) {
            gateOutputDriver.closeEntryGate();
        }
    }

    // raises the exit gate and triggers GUI animation via output driver
    public void raiseExitGate() {
        gateDriver.openGate();
        gateOpen = true;
        System.out.println("exit gate opened");
        if (gateOutputDriver != null) {
            gateOutputDriver.openExitGate();
        }
    }

    // lowers the exit gate and triggers GUI animation via output driver
    public void closeExitGate() {
        gateDriver.closeGate();
        gateOpen = false;
        System.out.println("exit gate closed");
        if (gateOutputDriver != null) {
            gateOutputDriver.closeExitGate();
        }
    }

    public void updateDisplay(int totalAvailable, int[] floorAvailable) {
        System.out.println("display updated: " + totalAvailable + " spots available");
        if (entranceDisplayDriver != null) {
            entranceDisplayDriver.updateEntranceDisplay(
                    totalAvailable,
                    ipms.getTotalSpots(),
                    floorAvailable
            );
        }
        if (floorDisplayDriver != null && floorAvailable.length > 0) {
            floorDisplayDriver.updateFloorDisplay(floorAvailable[0], ipms.getTotalSpots());
        }
    }
}