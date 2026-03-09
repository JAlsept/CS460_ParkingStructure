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
     * Initializes the sensor driver with a reference to the gate controller
     * @param gateController the gate controller
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
     * Called when exit sensor no longer detects a vehicle
     */
    public void onExitSensorCleared() {

        exitSensorActive = false;
        System.out.println("exit sensor cleared");
    }

    /**
     *Returns whether the entry sensor is currently active.
     * @return true if vehicle is detected at entry sensor
     */
    public boolean isEntrySensorActive() {
        return entrySensorActive;
    }

    /**
     * Returns if the exit sensor is currently active
     * @return true if a vehicle is detected at the exit sensor
     */
    public boolean isExitSensorActive() {
        return exitSensorActive;
    }
}