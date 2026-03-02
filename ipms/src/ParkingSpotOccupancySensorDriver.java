// handles signals from ultrasonic sensors above each parking spot
// passes occupancy state changes up to the occupancy controller
import java.util.HashMap;
import java.util.Map;

public class ParkingSpotOccupancySensorDriver {

    ParkingSpotOccupancyController occupancyController;

    // tracks last known state per spot so we only fire events on actual changes
    Map<Integer, Boolean> lastKnownState;

    public ParkingSpotOccupancySensorDriver(ParkingSpotOccupancyController occupancyController) {
        this.occupancyController = occupancyController;
        this.lastKnownState = new HashMap<>();
    }

    // ultrasonic sensor returns true = something detected = spot occupied
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

    // get last known state for a spot, returns null if never read
    public Boolean getLastKnownState(int spotID) {
        return lastKnownState.get(spotID);
    }

    // TODO: need to implement continuous polling for all spots
    // probably loop through all spot IDs and read sensor values
    // not sure how sensor hardware communicates yet, leaving for now
}
