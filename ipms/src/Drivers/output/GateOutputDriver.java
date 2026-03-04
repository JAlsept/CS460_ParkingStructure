package Drivers.output;

import GUI.ParkingFloorPane;

/**
 * Output driver for the boom gates mechanisms at entry and exit.
 * Receives command from MainEntryExitGateController and triggers GUI
 * animations on ParkingFloorPane.
 */
public class GateOutputDriver {

    private final ParkingFloorPane floor;

    public GateOutputDriver(ParkingFloorPane floor) {
        this.floor = floor;
    }

    /**
     * Called by MainEntryExitGateController when the entry gate should open.
     */
    public void openEntryGate() {

        javafx.application.Platform.runLater(() ->
                floor.animateEntryGate(true));

        System.out.println("Entry gate opening");
    }

    /**
     * Called by MainEntryExitGateController when the entry gate should close.
     */
    public void closeEntryGate() {
        javafx.application.Platform.runLater(() ->
                floor.animateEntryGate(false));

        System.out.println("Entry gate closing");
    }
}