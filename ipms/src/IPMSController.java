import java.util.HashMap;
import java.util.Map;

public class IPMSController {

    int totalAvailable;
    int[] floorAvailable;
    int[] floorCapacity;
    Map<Integer, Boolean> spotOccupied;
    Map<Integer, Integer> spotToFloor;
    boolean systemOperational;

    // sub-controllers
    MainEntryExitGateController gateController;
    ParkingSpotOccupancyController occupancyController;
    DataStore dataStore;

    public IPMSController(int totalSpots, int[] floorLayout) {
        this.floorAvailable = floorLayout.clone();
        this.floorCapacity = floorLayout.clone();
        this.spotOccupied = new HashMap<>();
        this.spotToFloor = new HashMap<>();
        this.systemOperational = true;

        // Fixed floor mapping
        int spotID = 0;
        for(int floor = 0; floor < floorLayout.length; floor++){
            for(int i = 0; i < floorLayout[floor];i++){
                spotToFloor.put(spotID,floor);
                spotOccupied.put(spotID,false); // all spots empty
                spotID++;
            }
        }
        this.totalAvailable = spotID;


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
        int[] newFloorAvailabile = new int[floorAvailable.length];
        for(Map.Entry<Integer,Boolean> entry: spotOccupied.entrySet()){
            int spotID = entry.getKey();
            boolean isOccupied = entry.getValue();
            int floor = spotToFloor.get(spotID);

            if(!isOccupied){
                newFloorAvailabile[floor]++;
            }
        }

        this.floorAvailable =newFloorAvailabile;
        int total = 0;
        for(int count :floorAvailable){
            total += count;
        }
        this.totalAvailable = total;
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

    /**
     * Returns the floor a given spot is on, -1 if none
     * @return
     */
    public int getFloorForSpot(int spotID){
        return spotToFloor.getOrDefault(spotID,-1);
    }

    // true if a specific spot is available
    public boolean isSpotAvailable(int spotID){
        return spotOccupied.containsKey(spotID) && !spotOccupied.get(spotID);
    }

    /**
     * Get total number of spots
     * @return total number of spots
     */
    public int getTotalSpots(){
        return spotOccupied.size();
    }

}
