package Drivers.output;

import GUI.ParkingFloorPane;

/**
 * Output driver for the LED indicator lights and availability displays.
 * Receives update from ParkingSpotOccupancyController and triggers GUI light
 * updates on ParkingFloorPane.
 */
public class ParkingSpotDisplayOutputDriver {

    private final ParkingFloorPane floor;

    /**
     * takes a reference to parkingFloorPane to trigger parking spot light
     * changes.
     * @param floor
     */
    public ParkingSpotDisplayOutputDriver(ParkingFloorPane floor) {

        this.floor = floor;
        System.out.println
                ("[Drivers.output.ParkingSpotDisplayOutputDriver] initialized");
    }

    /**
     * Spot changed state. If occupied (true) red light displayed. Else
     * occupied (false) green light displayed
     * @param spotID
     * @param occupied
     */
    public void updateSpotLight(int spotID, boolean occupied) {

        if (occupied){
            System.out.println("Spot " + spotID + " light -> RED");
        }else{
            System.out.println("Spot " + spotID + " light -> GREEN");
        }
        javafx.application.Platform.runLater(()->
                floor.updateSpotLight(spotID,occupied));
    }
}