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

    public ParkingSpotOccupancyController(IPMSController ipms) {
        this.ipms = ipms;
    }

    /**
     * Links the display output driver so that the lights updates are sent to
     * GUI.
     * @param driver
     */
    public void setDisplayOutputDriver(ParkingSpotDisplayOutputDriver driver){
        this.displayOutputDriver = driver;
    }

    public void spotOccupied(int spotID) {

        ipms.processInput(new Event("SPOT_OCCUPIED", spotID));
        setIndicatorLight(spotID, true);
    }

    public void spotEmpty(int spotID) {

        ipms.processInput(new Event("SPOT_EMPTY", spotID));
        setIndicatorLight(spotID, false);
    }

    /**
     * Sends a light update to the display output driver if connected.
     * @param spotID
     * @param occupied
     */
    public void setIndicatorLight(int spotID, boolean occupied) {

        if(displayOutputDriver != null){
            displayOutputDriver.updateSpotLight(spotID, occupied);
        } else {
            System.out.println("spot " + spotID + " light -> " +
                    (occupied ? "RED" : "GREEN"));
        }
    }
}
