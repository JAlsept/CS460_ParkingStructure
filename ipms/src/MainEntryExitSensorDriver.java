// handles signals from the piezoelectric sensors at the entry/exit
// translates raw sensor input -> events for the gate controller
public class MainEntryExitSensorDriver {

    MainEntryExitGateController gateController;
    boolean entrySensorActive;
    boolean exitSensorActive;

    public MainEntryExitSensorDriver(MainEntryExitGateController gateController) {
        this.gateController = gateController;
        this.entrySensorActive = false;
        this.exitSensorActive = false;
    }

    // called when physical entry sensor detects weight/pressure
    public void onEntrySensorTriggered() {
        System.out.println("entry sensor triggered");
        entrySensorActive = true;
        gateController.entrySensorTriggered();
    }

    // called when physical exit sensor detects weight/pressure
    public void onExitSensorTriggered() {
        System.out.println("exit sensor triggered");
        exitSensorActive = true;
        gateController.exitSensorTriggered();
    }

    // called when sensor is no longer detecting a vehicle
    public void onEntrySensorCleared() {
        entrySensorActive = false;
        System.out.println("entry sensor cleared");
    }

    public void onExitSensorCleared() {
        exitSensorActive = false;
        System.out.println("exit sensor cleared");
    }

    public boolean isEntrySensorActive() {
        return entrySensorActive;
    }

    public boolean isExitSensorActive() {
        return exitSensorActive;
    }

    // TODO: add polling or interrupt-based sensor reading
    // right now assuming something external calls these methods
    // need to wire up actual hardware interface later
}
