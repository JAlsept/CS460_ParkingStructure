package Drivers.output;

import GUI.ParkingFloorPane;

/**
 * Output driver for the Main Entrance Availability Display.
 * Receives signal from Controllers.MainEntryExitGateController and updates the
 * main entrance LCD screen showing total occupancy and per-floor availability.
 */
public class MainEntranceDisplayDriver {

    private final ParkingFloorPane floor;

    /**
     * Drivers.output.MainEntranceDisplayDriver Constructor.
     *
     * @param floor - the parking floor pane containing the visual
     *                display elements.
     */
    public MainEntranceDisplayDriver(ParkingFloorPane floor) {
        this.floor = floor;
    }

    /**
     * updateEntranceDisplay updates the main entrance display with total
     * structure occupancy, including per-floor availability.
     *
     * @param totalAvailable - total number of available spots in the
     *                         entire structure.
     * @param totalCapacity  - total capacity of the structure.
     * @param floorAvailable - array containing available spots per floor.
     */
    public void updateEntranceDisplay(int totalAvailable, int totalCapacity,
                                      int[] floorAvailable) {

        javafx.application.Platform.runLater(() -> {
            if (totalAvailable == 0) {
                floor.entranceDisplayTotal.setText("NO AVAILABILITY");

                floor.entranceDisplayTotal.setTextFill
                        (ParkingFloorPane.SPOT_OCCUPIED);

                floor.entranceDisplayFloor.setText("STRUCTURE FULL");

                floor.entranceDisplayFloor.setTextFill
                        (ParkingFloorPane.SPOT_OCCUPIED);
            } else {
                floor.entranceDisplayTotal.setText("TOTAL: " + totalAvailable +
                        " / " + totalCapacity);

                floor.entranceDisplayTotal.setTextFill
                        (javafx.scene.paint.Color.web("#58ff8a"));

                if (floorAvailable != null && floorAvailable.length > 0) {

                    StringBuilder floorInfo = new StringBuilder();
                    for (int i = 0; i < floorAvailable.length; i++) {

                        if (i > 0) floorInfo.append(" | ");

                        floorInfo.append("FLOOR ").append(i + 1).append(": ")
                                .append(floorAvailable[i]).append(" AVAIL");
                    }

                    floor.entranceDisplayFloor.setText(floorInfo.toString());
                    floor.entranceDisplayFloor.setTextFill
                            (ParkingFloorPane.TEXT_PRIMARY);
                }
            }
        });
    }

    /**
     * Updates the main entrance display showing a "STRUCTURE FULL" message.
     * This is called when the structure reaches maximum capacity and entry
     * should be denied.
     * Unused in demo but would be used in real implementation.
     */
    public void displayMaxCapacityMessage() {
        javafx.application.Platform.runLater(() -> {
            floor.entranceDisplayTotal.setText("STRUCTURE FULL");
            floor.entranceDisplayTotal.setTextFill
                    (ParkingFloorPane.SPOT_OCCUPIED);
        });
    }
}