// not my portion - stub so everything compiles
public class ParkingSpotOccupancyController {

    IPMSController ipms;

    public ParkingSpotOccupancyController(IPMSController ipms) {
        this.ipms = ipms;
    }

    public void spotOccupied(int spotID) {
        ipms.processInput(new Event("SPOT_OCCUPIED", spotID));
        setIndicatorLight(spotID, true);
    }

    public void spotEmpty(int spotID) {
        ipms.processInput(new Event("SPOT_EMPTY", spotID));
        setIndicatorLight(spotID, false);
    }

    public void setIndicatorLight(int spotID, boolean occupied) {
        // TODO: send signal to LED hardware
        System.out.println("spot " + spotID + " light -> " + (occupied ? "RED" : "GREEN"));
    }
}
