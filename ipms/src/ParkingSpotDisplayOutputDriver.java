// Output driver for the LED indicator lights and availability displays.
// Recives updates from ParkingSpotOccupancyController
// and triggers GUI light updates on ParkingFloorPane
public class ParkingSpotDisplayOutputDriver {

    private ParkingFloorPane floor;

    // takes a reference to parkingFloorPane to trigger spot light changes
    public ParkingSpotDisplayOutputDriver(ParkingFloorPane floor) {
        this.floor = floor;
        System.out.println("[ParkingSpotDisplayOutputDriver] initialized");
    }

    // Spot changed state.
    // if Occupied (true) red light displayed
    // else occupied (false) green light displayed
    public void updateSpotLight(int spotID, boolean occupied) {
        if (occupied){
            System.out.println("Spot " + spotID + " light -> RED");
        }else{
            System.out.println("Spot " + spotID + " light -> GREEN");
        }
        javafx.application.Platform.runLater(()-> floor.updateSpotLight(spotID,occupied));
    }
}