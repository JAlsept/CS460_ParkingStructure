// handles signals from ultrasonic sensors above each parking spot
// passes occupancy state changes up to the occupancy controller
public class ParkingSpotOccupancySensorDriver {

    ParkingSpotOccupancyController occupancyController;

    public ParkingSpotOccupancySensorDriver(ParkingSpotOccupancyController occupancyController) {
        this.occupancyController = occupancyController;
    }

    // ultrasonic sensor returns true = something detected = spot occupied
    public void onSensorReading(int spotID, boolean occupied) {
        if (occupied) {
            occupancyController.spotOccupied(spotID);
        } else {
            occupancyController.spotEmpty(spotID);
        }
    }

    // TODO: need to implement continuous polling for all spots
    // probably loop through all spot IDs and read sensor values
    // not sure how sensor hardware communicates yet, leaving for now
}
