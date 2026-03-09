package Drivers.input;

import Controllers.ParkingSpotOccupancyController;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles signals from ultrasonic sensors above each parking spot.
 * Passes occupancy state changes up to the occupancy controller.
 */
public class ParkingSpotOccupancySensorDriver {

    ParkingSpotOccupancyController occupancyController;

    // tracks last known state per spot to only fire events on actual changes
    Map<Integer, Boolean> lastKnownState;

    public ParkingSpotOccupancySensorDriver
            (ParkingSpotOccupancyController occupancyController) {
        this.occupancyController = occupancyController;
        this.lastKnownState = new HashMap<>();
    }

    /**
     * ultrasonic sensor returns true = something detected = spot occupied.
     * @param spotID spot that we are considering
     * @param occupied true if the parking space is occupied, false otherwise
     */
    public void onSensorReading(int spotID, boolean occupied) {
        Boolean prev = lastKnownState.get(spotID);

        // only forward if state actually changed, avoids spamming events
        if (prev == null || prev != occupied) {
            lastKnownState.put(spotID, occupied);
            if (occupied) {
                occupancyController.spotOccupied(spotID);
            } else {
                occupancyController.spotEmpty(spotID);
            }
        }
    }

    /**
     * Gets last known state for a spot, returns null if never read.
     * @param spotID spot that we are considering
     * @return true if we know the last state of the parking spot, false otherwise
     */
    public Boolean getLastKnownState(int spotID) {
        return lastKnownState.get(spotID);
    }
}