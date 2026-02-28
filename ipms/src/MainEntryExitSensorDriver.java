// handles signals from the piezoelectric sensors at the entry/exit
// translates raw sensor input -> events for the gate controller
public class MainEntryExitSensorDriver {

    MainEntryExitGateController gateController;

    public MainEntryExitSensorDriver(MainEntryExitGateController gateController) {
        this.gateController = gateController;
    }

    // called when physical entry sensor detects weight/pressure
    public void onEntrySensorTriggered() {
        System.out.println("entry sensor triggered");
        gateController.entrySensorTriggered();
    }

    // called when physical exit sensor detects weight/pressure
    public void onExitSensorTriggered() {
        System.out.println("exit sensor triggered");
        gateController.exitSensorTriggered();
    }

    // TODO: add polling or interrupt-based sensor reading
    // right now assuming something external calls these methods
    // need to wire up actual hardware interface later
}
