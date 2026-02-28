// basic event object passed around the system
public class Event {
    String type;  // "ENTRY", "EXIT", "SPOT_OCCUPIED", "SPOT_EMPTY"
    int spotID;   // only relevant for spot events, -1 otherwise
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
