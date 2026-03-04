package Drivers.output;

import GUI.ParkingFloorPane;

/**
 * Output driver for the Floor Availability Display.
 * Receives signal from Controllers.MainEntryExitGateController and updates
 * the floor-level LCD screen showing availability for the specific floor.
 */
public class FloorDisplayDriver {

    private final ParkingFloorPane floor;
    private final int floorNumber;

    /**
     * Drivers.output.FloorDisplayDriver Constructor.
     *
     * @param floor       - the parking floor pane containing the visual
     *                      display elements.
     * @param floorNumber - the floor number this display represents.
     */
    public FloorDisplayDriver(ParkingFloorPane floor, int floorNumber) {
        this.floor = floor;
        this.floorNumber = floorNumber;
    }

    /**
     * Updates the floor availability display with current occupancy
     * information.
     * 
     * totalSpots is unused in demo but would be used in real implementation.
     *
     * @param availableSpots - number of available spots on this specific floor.
     * @param totalSpots     - total number of spots on this floor.
     */
    public void updateFloorDisplay(int availableSpots, int totalSpots) {

        javafx.application.Platform.runLater(() -> {
            if (availableSpots == 0) {
                floor.floorDisplayLabel.setText("FLOOR FULL");
                floor.floorDisplayLabel.setTextFill
                        (ParkingFloorPane.SPOT_OCCUPIED);
            } else {
                floor.floorDisplayLabel.setText("AVAILABLE: " + availableSpots);
                floor.floorDisplayLabel.setTextFill
                        (javafx.scene.paint.Color.web("#58ff8a"));
            }
        });
    }

    /**
     * Gets the floor number this driver is associated with.
     * Unused in demo but would be used in real implementation.
     *
     * @return - floor number.
     */
    public int getFloorNumber() {
        return floorNumber;
    }
}