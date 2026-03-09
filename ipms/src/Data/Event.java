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

    /**
     * Creates an event for a specific parking spot
     * @param type the event type
     * @param spotID the spot this event applies to

     */
    public Event(String type, int spotID) {

        this.type = type;
        this.spotID = spotID;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Creates a gate event with no associated spotID
     * @param type the event type. (ENTRY or EXIT)
     */
    public Event(String type) {
        this(type, -1);
    }
}