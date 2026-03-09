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
     * takes a reference to parkingFloorPane to trigger parking spotlight
     * changes.
     * @param floor parking floor we are taking into consideration
     */
    public ParkingSpotDisplayOutputDriver(ParkingFloorPane floor) {

        this.floor = floor;
        System.out.println
                ("[Drivers.output.ParkingSpotDisplayOutputDriver] initialized");
    }

    /**
     * Spot changed state. If occupied (true) red light displayed. Else
     * occupied (false) green light displayed
     * @param spotID, spot that is being considered
     * @param occupied true if the parking spot is occupied, false otherwise
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