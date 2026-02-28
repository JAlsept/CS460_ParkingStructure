import java.util.ArrayList;
import java.util.List;

public class DataStore {

    // just storing in memory for now
    // TODO: hook up to actual DB or file system
    List<Event> eventLog;
    String lastCapacitySnapshot;
    String lastSystemState;

    public DataStore() {
        eventLog = new ArrayList<>();
    }

    public boolean logEvent(Event e) {
        try {
            eventLog.add(e);
            System.out.println("logged event: " + e.type);
            return true;
        } catch (Exception ex) {
            System.out.println("failed to log event: " + ex.getMessage());
            return false;
        }
    }

    public boolean storeCapacity() {
        // TODO: actually persist this somewhere
        lastCapacitySnapshot = "capacity saved at " + System.currentTimeMillis();
        return true;
    }

    public boolean storeSystemState() {
        // TODO: serialize full system state properly
        lastSystemState = "state saved at " + System.currentTimeMillis();
        return true;
    }
}
