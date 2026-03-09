package Controllers;

import Data.Event;
import Drivers.output.ParkingSpotDisplayOutputDriver;

/**
 * Monitors individual parking spot and controls the light indicators.
 * Sends the spot change to IPMSController and updates
 * ParkingSpotDisplayOutputDriver.
 */
public class ParkingSpotOccupancyController {

    IPMSController ipms;
    ParkingSpotDisplayOutputDriver displayOutputDriver;

    /**
     * Initializes the controller with a reference to the IPMS
     * @param ipms the IMPS controller
     */
    public ParkingSpotOccupancyController(IPMSController ipms) {
        this.ipms = ipms;
    }

    /**
     * Links the display output driver so that the lights updates are sent to
     * GUI.
     * @param driver Parking spot display output driver
     */
    public void setDisplayOutputDriver(ParkingSpotDisplayOutputDriver driver) {
        this.displayOutputDriver = driver;
    }

    /**
     * Called when a spot sensor detects a vehicle has parked
     * @param spotID the spot that is not occupied
     */
    public void spotOccupied(int spotID) {

        ipms.processInput(new Event("SPOT_OCCUPIED", spotID));
        setIndicatorLight(spotID, true);
    }

    /**
     * Called when a spot sensor detects a vehicle has left the spot
     * @param spotID the spot that is now free
     */
    public void spotEmpty(int spotID) {

        ipms.processInput(new Event("SPOT_EMPTY", spotID));
        setIndicatorLight(spotID, false);
    }

    /**
     * Sends a light update to the display output driver if connected.
     * @param spotID spot that the light update will occur at
     * @param occupied true if spot is occupied, false otherwise
     */
    public void setIndicatorLight(int spotID, boolean occupied) {

        if (displayOutputDriver != null) {
            displayOutputDriver.updateSpotLight(spotID, occupied);
        } else {
            System.out.println("spot " + spotID + " light -> " +
                    (occupied ? "RED" : "GREEN"));
        }
    }
}