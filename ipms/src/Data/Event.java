package Data;

/**
 * Basic event object passed around the system.
 */
public class Event {

    // "ENTRY", "EXIT", "SPOT_OCCUPIED", "SPOT_EMPTY"
    public String type;

    // only relevant for spot events, -1 otherwise
    public int spotID;
    long timestamp;

    public Event(String type, int spotID) {

        this.type = type;
        this.spotID = spotID;
        this.timestamp = System.currentTimeMillis();
    }

    public Event(String type) {
        this(type, -1);
    }
}