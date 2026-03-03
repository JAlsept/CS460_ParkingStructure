import javafx.animation.PauseTransition;
import javafx.util.Duration;

// Output driver for the boom gates mechanisms at entry and exit
// Receives commands from MainEntryExitGateController and triggers GUI animations on
// ParkingFloorPane
public class GateOutputDriver {

    private ParkingFloorPane floor;

    public GateOutputDriver(ParkingFloorPane floor) {
        this.floor = floor;
    }

    // called by MainEntryExitGateController when the entry gate should open
    public void openEntryGate() {
        System.out.println("Entry gate opening");
        PauseTransition pause = new PauseTransition(Duration.millis(1300));
        pause.setOnFinished(e ->
                javafx.application.Platform.runLater(() -> floor.animateEntryGate(true))
        );
        pause.play();
    }

    // called by MainEntryExitGateController when the entry gate should close
    public void closeEntryGate() {
        javafx.application.Platform.runLater(() -> floor.animateEntryGate(false));
        System.out.println("Entry gate closing");
    }

    // called by MainEntryExitGateController when the exit gate should open
    public void openExitGate() {
        System.out.println("Exit gate opening");
        PauseTransition pause = new PauseTransition(Duration.millis(1300));
        pause.setOnFinished(e ->
                javafx.application.Platform.runLater(() -> floor.animateExitGate(true))
        );
        pause.play();
    }

    // called by MainEntryExitGateController when the exit gate should close
    public void closeExitGate() {
        javafx.application.Platform.runLater(() -> floor.animateExitGate(false));
        System.out.println("Exit gate closing");
    }
}