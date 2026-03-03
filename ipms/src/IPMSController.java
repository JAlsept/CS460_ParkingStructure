import java.util.HashMap;
import java.util.Map;

public class IPMSController {

    int totalAvailable;
    int totalCapacity;
    int[] floorAvailable;
    Map<Integer, Boolean> spotOccupied;
    Map<Integer, Integer> spotToFloor;
    boolean systemOperational;

    // sub-controllers
    MainEntryExitGateController gateController;
    ParkingSpotOccupancyController occupancyController;
    DataStore dataStore;

    public IPMSController(int totalSpots, int[] floorLayout) {
        this.totalCapacity = totalSpots;
        this.totalAvailable = totalSpots;
        this.totalSpots = totalSpots;
        this.floorAvailable = floorLayout.clone();
        this.spotOccupied = new HashMap<>();
        this.spotToFloor = new HashMap<>();
        this.systemOperational = true;

        dataStore = new DataStore();
        gateController = new MainEntryExitGateController(this);
        occupancyController = new ParkingSpotOccupancyController(this);
    }

    // overload so spot map can be passed in directly
    public IPMSController(int totalSpots, int[] floorLayout, Map<Integer, Integer> spotMap) {
        this(totalSpots, floorLayout);
        this.spotToFloor.putAll(spotMap);
        for (int id : spotMap.keySet()) {
            spotOccupied.put(id, false);
        }
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
        if (!systemOperational) {
            System.out.println("system not operational, denying entry");
            return false;
        }
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
        totalAvailable = totalCapacity - occupied;

        // recalculate per-floor counts from spotToFloor map
        int[] updatedFloor = new int[floorAvailable.length];
        for (Map.Entry<Integer, Boolean> entry : spotOccupied.entrySet()) {
            Integer floor = spotToFloor.get(entry.getKey());
            if (floor != null && !entry.getValue()) {
                updatedFloor[floor]++;
            }
        }
        floorAvailable = updatedFloor;

        dataStore.storeCapacity();
        gateController.updateDisplay(totalAvailable, floorAvailable);
    }

    public void updateSystemState() {
        // if either sub-controller is gone something is seriously wrong
        if (gateController == null || occupancyController == null) {
            systemOperational = false;
            System.out.println("a sub-controller is null, something went wrong");
        }
        // TODO: add heartbeat or status polling per sub-controller
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
