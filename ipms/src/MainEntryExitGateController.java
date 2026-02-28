// not my portion - stub so everything compiles
public class MainEntryExitGateController {

    boolean entrySensorActive;
    boolean exitSensorActive;
    boolean gateOpen;
    IPMSController ipms;

    public MainEntryExitGateController(IPMSController ipms) {
        this.ipms = ipms;
        this.gateOpen = false;
    }

    public void entrySensorTriggered() {
        entrySensorActive = true;
        sendEventToIPMS(new Event("ENTRY"));
    }

    public void exitSensorTriggered() {
        exitSensorActive = true;
        sendEventToIPMS(new Event("EXIT"));
    }

    public void sendEventToIPMS(Event e) {
        ipms.processInput(e);
    }

    public void raiseMainGate() {
        gateOpen = true;
        System.out.println("gate opened");
        // TODO: send actual signal to gate hardware
    }

    public void closeMainGate() {
        gateOpen = false;
        System.out.println("gate closed");
    }

    public void updateDisplay(int totalAvailable, int[] floorAvailable) {
        // TODO: push to display driver
        System.out.println("display updated: " + totalAvailable + " spots available");
    }
}
