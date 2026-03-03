/**
 * Monitors individual parking spot and controls the light indicators.
 * Sends the spot change to IPMSController and updates ParkingSpotDisplayOutputDriver
 */
public class ParkingSpotOccupancyController {

    IPMSController ipms;
    ParkingSpotDisplayOutputDriver displayOutputDriver;

    public ParkingSpotOccupancyController(IPMSController ipms) {
        this.ipms = ipms;
    }

    //links the display output driver so that the lights updates are sent to GUI
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

    // sends a light update to the display output driver if connected
    public void setIndicatorLight(int spotID, boolean occupied) {
        if(displayOutputDriver != null){
            displayOutputDriver.updateSpotLight(spotID, occupied);
        }else{
            System.out.println("spot " + spotID + " light -> " + (occupied ? "RED" : "GREEN"));
        }

    }
}
