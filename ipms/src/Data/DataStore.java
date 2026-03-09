package Data;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.*;

/**
 * Manages the storage for the IPMS. Logs events and saves system states and
 * capacity count to their respective files.
 */
public class DataStore {

    List <Event> eventLog;
    String lastCapacitySnapshot;
    String lastSystemState;

    // files to write state to
    static final String CAPACITY_FILE = "capacity_state.txt";
    static final String SYSTEM_FILE = "system_state.txt";
    static final String EVENT_LOG_FILE = "event_log.txt";

    /**
     * Initializes the event log list
     */
    public DataStore() {
        eventLog = new ArrayList <>();
    }

    /**
     * Logs an event and saves it to the event log file
     * @param e the event to log
     * @return false if the file fails to log event, true otherwise
     */
    public boolean logEvent(Event e) {
        try {
            eventLog.add(e);

            // append event to log file
            String line = e.timestamp + " | " + e.type + " | spotID=" +
                    e.spotID + "\n";

            Files.write(Paths.get(EVENT_LOG_FILE), line.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            System.out.println("logged event: " + e.type);

            return true;

        } catch (Exception ex) {
            System.out.println("failed to log event: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Saves the current capacity snapshot to its respective file
     * @return true if file write was successful, false otherwise.
     */
    public boolean storeCapacity() {

        try {
            lastCapacitySnapshot = "capacity saved at " +
                    System.currentTimeMillis();

            Files.write(Paths.get(CAPACITY_FILE),
                    lastCapacitySnapshot.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            return true;

        } catch (Exception ex) {
            System.out.println("failed to store capacity: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Saves the current system state to its respective file.
     * @return true if file write was successful, false otherwise.
     */
    public boolean storeSystemState() {

        try {
            lastSystemState = "state saved at " + System.currentTimeMillis();

            Files.write(Paths.get(SYSTEM_FILE), lastSystemState.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            return true;

        } catch (Exception ex) {
            System.out.println("failed to store system state: " +
                    ex.getMessage());
            return false;
        }
    }
}