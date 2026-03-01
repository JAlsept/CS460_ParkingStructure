import java.util.HashMap;
import java.util.Map;

public class IPMSController {

    int totalAvailable;
    int totalSpots;
    int[] floorAvailable;
    Map<Integer, Boolean> spotOccupied;
    Map<Integer, Integer> spotToFloor;
    boolean systemOperational;

    // sub-controllers
    MainEntryExitGateController gateController;
    ParkingSpotOccupancyController occupancyController;
    DataStore dataStore;

    public IPMSController(int totalSpots, int[] floorLayout) {
        this.totalAvailable = totalSpots;
        this.totalSpots = totalSpots;
        this.floorAvailable = floorLayout.clone();
        this.spotOccupied = new HashMap<>();
        this.spotToFloor = new HashMap<>();
        this.systemOperational = true;

        // TODO: load spot-to-floor mapping from config or DB on startup
        // hardcoded for now, fix later

        dataStore = new DataStore();
        gateController = new MainEntryExitGateController(this);
        occupancyController = new ParkingSpotOccupancyController(this);
    }

    public void processInput(Event e) {
        if (e == null) return;

        switch (e.type) {
            case "ENTRY":
                authorizeEntry();
                break;
            case "EXIT":
                authorizeExit();
                break;
            case "SPOT_OCCUPIED":
                spotOccupied.put(e.spotID, true);
                updateAvailability();
                break;
            case "SPOT_EMPTY":
                spotOccupied.put(e.spotID, false);
                updateAvailability();
                break;
            default:
                System.out.println("unknown event: " + e.type);
        }

        dataStore.logEvent(e);
        updateSystemState();
    }

    public boolean capacityAvailable() {
        return totalAvailable > 0;
    }

    public boolean authorizeEntry() {
        if (!capacityAvailable()) {
            System.out.println("structure full, denying entry");
            return false;
        }
        gateController.raiseMainGate();
        return true;
    }

    public void authorizeExit() {
        // just open the gate, occupancy updated by spot sensors
        gateController.raiseMainGate();
    }

    public void updateAvailability() {
        int occupied = 0;
        for (boolean val : spotOccupied.values()) {
            if (val) occupied++;
        }
        // TODO: need actual total capacity stored somewhere, using spotOccupied size for now
        totalAvailable = totalSpots - occupied;

        // update per-floor counts
        // TODO: implement floor-level breakdown once spotToFloor is populated

        dataStore.storeCapacity();
        gateController.updateDisplay(totalAvailable, floorAvailable);
    }

    public void updateSystemState() {
        // TODO: add health checks for sub-controllers
        // for now just save state
        dataStore.storeSystemState();
    }

    public String generateDisplayData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total Available: ").append(totalAvailable).append("\n");
        for (int i = 0; i < floorAvailable.length; i++) {
            sb.append("Floor ").append(i + 1).append(": ").append(floorAvailable[i]).append("\n");
        }
        return sb.toString();
    }
}
