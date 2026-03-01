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

    public MainEntryExitGateController(IPMSController ipms) {
        this.ipms = ipms;
        this.gateOpen = false;
        this.gateDriver = new MainGateMechanismDriver();
    }

    /** Called when entry sensor detects a vehicle; sends ENTRY event to IPMS. */
    public void entrySensorTriggered() {
        entrySensorActive = true;
        sendEventToIPMS(new Event("ENTRY"));
    }

    /** Called when exit sensor detects a vehicle; sends EXIT event to IPMS. */
    public void exitSensorTriggered() {
        exitSensorActive = true;
        sendEventToIPMS(new Event("EXIT"));
    }

    /** Forwards events to the main IPMS controller. */
    public void sendEventToIPMS(Event e) {
        ipms.processInput(e);
    }

    /** Raises the main gate via MainGateMechanismDriver and updates local state. */
    public void raiseMainGate() {
        gateDriver.openGate();
        gateOpen = true;
        System.out.println("gate opened");
    }

    /** Lowers the main gate via MainGateMechanismDriver and updates local state. */
    public void closeMainGate() {
        gateDriver.closeGate();
        gateOpen = false;
        System.out.println("gate closed");
    }

    /** Updates availability display; TODO: wire to display output driver when implemented. */
    public void updateDisplay(int totalAvailable, int[] floorAvailable) {
        System.out.println("display updated: " + totalAvailable + " spots available");
    }
}
