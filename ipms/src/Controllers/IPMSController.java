package Controllers;

import Data.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class IPMSController {

    public int totalAvailable;
    public int[] floorAvailable;
    int totalCapacity;
    int[] floorCapacity;
    boolean systemOperational;
    Map<Integer, Boolean> spotOccupied;
    Map<Integer, Integer> spotToFloor;

    // sub-controllers
    public MainEntryExitGateController gateController;
    public ParkingSpotOccupancyController occupancyController;
    DataStore dataStore;

    /**
     *
     * @param totalSpots
     * @param floorLayout
     */
    public IPMSController(int totalSpots, int[] floorLayout) {

        this.totalCapacity = totalSpots;
        this.totalAvailable = totalSpots;
        this.floorAvailable = floorLayout.clone();
        this.floorCapacity = floorLayout.clone();
        this.spotOccupied = new HashMap <>();
        this.spotToFloor = new HashMap <>();
        this.systemOperational = true;

        int spotID = 0;
        for (int floor = 0; floor < floorLayout.length; floor++) {
            for (int i = 0; i < floorLayout[floor]; i++) {
                spotToFloor.put(spotID, floor);
                spotOccupied.put(spotID, false);
                spotID++;
            }
        }

        dataStore = new DataStore();
        gateController = new MainEntryExitGateController(this);
        occupancyController = new ParkingSpotOccupancyController(this);
    }

    /**
     * Overload so spot map can be passed in directly.
     * @param totalSpots
     * @param floorLayout
     * @param spotMap
     */
    public IPMSController(int totalSpots, int[] floorLayout, Map <Integer,
            Integer> spotMap) {

        this(totalSpots, floorLayout);
        this.spotToFloor.putAll(spotMap);
        for (int id: spotMap.keySet()) {
            spotOccupied.put(id, false);
        }
    }

    /**
     *
     * @param e
     */
    public void processInput(Event e) {

        if (e == null) return;

        switch (e.type) {
            case "ENTRY" -> authorizeEntry();
            case "EXIT" -> authorizeExit();
            case "SPOT_OCCUPIED" -> {
                spotOccupied.put(e.spotID, true);
                updateAvailability();
            }
            case "SPOT_EMPTY" -> {
                spotOccupied.put(e.spotID, false);
                updateAvailability();
            }
            default -> System.out.println("unknown event: " + e.type);
        }

        dataStore.logEvent(e);
        updateSystemState();
    }

    /**
     *
     * @return
     */
    public boolean capacityAvailable() {
        return totalAvailable > 0;
    }

    /**
     *
     * @return
     */
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

    /**
     *
     */
    public void authorizeExit() {

        // just open the gate, occupancy updated by spot sensors
        gateController.raiseMainGate();
    }

    /**
     *
     */
    public void updateAvailability() {

        // recalculate per-floor counts from spotToFloor map
        int[] newFloorAvailable = new int[floorAvailable.length];

        for (Map.Entry <Integer, Boolean> entry: spotOccupied.entrySet()) {
            int spotID = entry.getKey();
            boolean isOccupied = entry.getValue();
            int floor = spotToFloor.get(spotID);
            if (!isOccupied) {
                newFloorAvailable[floor]++;
            }
        }

        floorAvailable = newFloorAvailable;

        // sum floor counts for total
        int total = 0;
        for (int count: floorAvailable) {
            total += count;
        }

        totalAvailable = total;
        dataStore.storeCapacity();
        gateController.updateDisplay(totalAvailable, floorAvailable);
    }

    /**
     *
     */
    public void updateSystemState() {

        // if either sub-controller is gone something is seriously wrong
        if (gateController == null || occupancyController == null) {
            systemOperational = false;
            System.out.println("a sub-controller is null, something went wrong");
        }
        dataStore.storeSystemState();
    }

    /**
     *
     * @return
     */
    public String generateDisplayData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total Available: ").append(totalAvailable).append("\n");
        for (int i = 0; i < floorAvailable.length; i++) {
            sb.append("Floor ").append(i + 1).append(": ")
                    .append(floorAvailable[i]).append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns the floor a given spot is on, -1 if none.
     */
    public int getFloorForSpot(int spotID) {
        return spotToFloor.getOrDefault(spotID, -1);
    }

    /**
     * True if a specific spot is available.
     * @param spotID
     * @return
     */
    public boolean isSpotAvailable(int spotID) {
        return spotOccupied.containsKey(spotID) && !spotOccupied.get(spotID);
    }

    /**
     * Get total number of spots.
     * @return total number of spots.
     */
    public int getTotalSpots() {
        return spotOccupied.size();
    }
}