/**
 * MainGateMechanismDriver - output driver for the physical boom barrier gate mechanism.
 */
public class MainGateMechanismDriver {

    private boolean gateOpen;

    public MainGateMechanismDriver() {
        gateOpen = false;
    }

    /** Sends open signal to the gate motor, raising the boom barrier. */
    public void openGate() {
        gateOpen = true;
        System.out.println("OPEN signal sent to gate motor");
    }

    /** Sends close signal to the gate motor, lowering the boom barrier. */
    public void closeGate() {
        gateOpen = false;
        System.out.println("CLOSE signal sent to gate motor");
    }

    /** Returns the current state */
    public boolean isGateOpen() {
        return gateOpen;
    }
}