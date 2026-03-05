package Drivers.input;

import Controllers.MainEntryExitGateController;

/**
 * Handles signal from the piezoelectric sensors at the entry/exit.
 * Translates raw sensor input -> events for the gate controller.
 */
public class MainEntryExitSensorDriver {

    MainEntryExitGateController gateController;
    boolean entrySensorActive;
    boolean exitSensorActive;

    /**
     *
     * @param gateController
     */
    public MainEntryExitSensorDriver
    (MainEntryExitGateController gateController) {

        this.gateController = gateController;
        this.entrySensorActive = false;
        this.exitSensorActive = false;
    }

    /**
     * Called when physical entry sensor detects weight/pressure
     */
    public void onEntrySensorTriggered() {

        System.out.println("entry sensor triggered");
        entrySensorActive = true;
        gateController.entrySensorTriggered();
    }

    /**
     * Called when physical exit sensor detects weight/pressure
     */
    public void onExitSensorTriggered() {

        System.out.println("exit sensor triggered");
        exitSensorActive = true;
        gateController.exitSensorTriggered();
    }

    /**
     * Called when sensor is no longer detecting a vehicle
     */
    public void onEntrySensorCleared() {

        entrySensorActive = false;
        System.out.println("entry sensor cleared");
    }

    /**
     *
     */
    public void onExitSensorCleared() {

        exitSensorActive = false;
        System.out.println("exit sensor cleared");
    }

    /**
     *
     * @return
     */
    public boolean isEntrySensorActive() {
        return entrySensorActive;
    }

    /**
     *
     * @return
     */
    public boolean isExitSensorActive() {
        return exitSensorActive;
    }
}