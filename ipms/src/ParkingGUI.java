import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * ParkingGUI - main JavaFX application entry point for the IPMS Parking Structure Monitor.
 * Wires together the backend controllers, floor pane, side panel, and button handlers.
 * Manages car entry/exit animations and keeps the GUI in sync with backend state.
 */
public class ParkingGUI extends Application {

    // backend wiring
    private IPMSController ipms;
    private MainEntryExitGateController gateController;
    private MainEntryExitSensorDriver entrySensorDriver;
    private ParkingSpotOccupancyController occupancyController;
    private ParkingSpotOccupancySensorDriver spotSensorDriver;

    // floor pane handles all visual components
    private ParkingFloorPane floor;

    // animation lock — prevents button spam mid-animation
    private boolean animating = false;

    // side panel labels
    private Label      availableCountLabel;
    private VBox       logBox;
    private ScrollPane logScroll;

    // -------------------------------------------------
    //  START
    // -------------------------------------------------

    @Override
    public void start(Stage stage) {
        floor = new ParkingFloorPane();
        initSystem();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(ParkingFloorPane.BG_DARK, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setPadding(new Insets(16));
        root.setTop(buildTitleBar());
        root.setLeft(buildSidePanel());

        VBox center = new VBox(0);
        center.setAlignment(Pos.TOP_CENTER);
        center.setFillWidth(false);
        center.getChildren().addAll(
                floor.buildFloorPane(),
                floor.buildInfoBar(),
                floor.buildEntryLane(),
                floor.buildLaneDivider(),
                floor.buildExitLane()
        );
        root.setCenter(center);
        root.setBottom(buildControls());

        Scene scene = new Scene(root, 1040, 800);
        stage.setTitle("CS460 – IPMS Parking Structure Monitor");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        updateSidePanelCount();
        updateAllDisplays();
    }

    // -------------------------------------------------
    //  SYSTEM INIT
    // -------------------------------------------------

    /**
     * Initializes backend controllers and wires GUI callbacks.
     * Controllers are overridden to trigger GUI updates when backend state changes.
     * These overrides are a temporary workaround until output drivers are implemented.
     */
    private void initSystem() {
        int[] layout = {ParkingFloorPane.TOTAL_SPOTS};
        ipms = new IPMSController(ParkingFloorPane.TOTAL_SPOTS, layout) {
            @Override
            public void updateAvailability() {
                super.updateAvailability();
                javafx.application.Platform.runLater(() -> {
                    updateSidePanelCount();
                });
            }
        };

        gateController = ipms.gateController;
        occupancyController = ipms.occupancyController;

        GateOutputDriver gateDriver = new GateOutputDriver(floor);
        ParkingSpotDisplayOutputDriver spotDriver = new ParkingSpotDisplayOutputDriver(floor);
        MainEntranceDisplayDriver entranceDisplayDriver = new MainEntranceDisplayDriver(floor);
        FloorDisplayDriver floorDisplayDriver = new FloorDisplayDriver(floor, 1);

        gateController.setGateOutputDriver(gateDriver);
        gateController.setMainEntranceDisplayDriver(entranceDisplayDriver);
        gateController.setFloorDisplayDriver(floorDisplayDriver);
        occupancyController.setDisplayOutputDriver(spotDriver);

        entrySensorDriver = new MainEntryExitSensorDriver(gateController);
        spotSensorDriver = new ParkingSpotOccupancySensorDriver(occupancyController);
    }

    // -------------------------------------------------
    //  TITLE BAR
    // -------------------------------------------------

    private HBox buildTitleBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 12, 0));
        Label title = lbl("IPMS  //  Parking Structure Monitor  //  Floor 1", "Menlo", 13, true, ParkingFloorPane.TEXT_PRIMARY);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label sys = lbl("● SYSTEM OPERATIONAL", "Menlo", 11, false, ParkingFloorPane.SPOT_EMPTY);
        bar.getChildren().addAll(title, sp, sys);
        return bar;
    }

    // -------------------------------------------------
    //  SIDE PANEL
    // -------------------------------------------------

    private VBox buildSidePanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(200);
        panel.setPadding(new Insets(0, 14, 0, 0));

        VBox countBox = floor.vbox(12, ParkingFloorPane.BG_PANEL);
        Label ct = lbl("AVAILABLE SPACES", "Menlo", 10, true, ParkingFloorPane.TEXT_MUTED);
        availableCountLabel = lbl(String.valueOf(ParkingFloorPane.TOTAL_SPOTS), "Menlo", 42, true, ParkingFloorPane.SPOT_EMPTY);
        Label tot = lbl("of " + ParkingFloorPane.TOTAL_SPOTS + " total", "Menlo", 10, false, ParkingFloorPane.TEXT_MUTED);
        countBox.getChildren().addAll(ct, availableCountLabel, tot);

        VBox legendBox = floor.vbox(8, ParkingFloorPane.BG_PANEL);
        Label lt = lbl("INDICATOR LIGHTS", "Menlo", 10, true, ParkingFloorPane.TEXT_MUTED);
        legendBox.getChildren().addAll(lt,
                lbl("● GREEN — Available", "Menlo", 11, false, ParkingFloorPane.SPOT_EMPTY),
                lbl("● RED   — Occupied",  "Menlo", 11, false, ParkingFloorPane.SPOT_OCCUPIED));

        Label logTitle = lbl("EVENT LOG", "Menlo", 10, true, ParkingFloorPane.TEXT_MUTED);
        logTitle.setPadding(new Insets(4, 0, 2, 0));

        logBox = new VBox(4);
        logBox.setPadding(new Insets(10));
        logBox.setStyle("-fx-background-color: #161b22;");

        logScroll = new ScrollPane(logBox);
        logScroll.setPrefHeight(300);
        logScroll.setMinHeight(300);
        logScroll.setMaxHeight(300);
        logScroll.setFitToWidth(true);
        logScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        logScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        logScroll.setStyle("-fx-background: #161b22; -fx-background-color: #161b22; -fx-border-color: #161b22;");

        panel.getChildren().addAll(countBox, legendBox, logTitle, logScroll);
        return panel;
    }

    // -------------------------------------------------
    //  CONTROLS
    // -------------------------------------------------

    private HBox buildControls() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(12, 0, 0, 0));
        Button e = styledBtn("→  Car Enters", ParkingFloorPane.DISPLAY_BLUE);
        Button x = styledBtn("→  Car Exits",  ParkingFloorPane.GATE_COLOR);
        Button r = styledBtn("↺  Reset",       Color.web("#30363d"));
        e.setOnAction(ev -> handleCarEnter());
        x.setOnAction(ev -> handleCarExit());
        r.setOnAction(ev -> handleReset());
        bar.getChildren().addAll(e, x, r);
        return bar;
    }

    // -------------------------------------------------
    //  BUTTON HANDLERS
    // -------------------------------------------------

    /**
     * Handles car entry — triggers sensor, animates entry car through gate,
     * and marks the first available spot as occupied.
     */
    private void handleCarEnter() {
        if (animating) { addLog("⚠ Animation in progress"); return; }
        int spot = floor.findFirstEmptySpot();
        if (spot < 0) { addLog("✗ Structure full — entry denied"); return; }

        animating = true;
        addLog("→ Entry sensor triggered");
        entrySensorDriver.onEntrySensorTriggered();
        floor.entryCar.setLayoutX(-70);

        double gateX = ParkingFloorPane.FW * 0.38;

        Timeline approach = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(floor.entryCar.layoutXProperty(), -70)),
                new KeyFrame(Duration.millis(700), new KeyValue(floor.entryCar.layoutXProperty(), gateX - 100))
        );
        approach.setOnFinished(e -> {
            PauseTransition pauseAfterOpen = new PauseTransition(Duration.millis(800));
            pauseAfterOpen.setOnFinished(ev2 -> {
                Timeline drive = new Timeline(
                        new KeyFrame(Duration.ZERO,       new KeyValue(floor.entryCar.layoutXProperty(), gateX - 100)),
                        new KeyFrame(Duration.millis(600), new KeyValue(floor.entryCar.layoutXProperty(), ParkingFloorPane.FW + 80))
                );
                drive.setOnFinished(evv -> {
                    spotSensorDriver.onSensorReading(spot, true);
                    floor.updateSpotLight(spot, true);
                    addLog("✓ Parked in spot P" + (spot + 1));
                    floor.entryCar.setLayoutX(-70);
                    PauseTransition close = new PauseTransition(Duration.millis(400));
                    close.setOnFinished(ec -> {
                        gateController.closeMainGate();
                        animating = false;
                    });
                    close.play();
                });
                drive.play();
            });
            pauseAfterOpen.play();
        });
        approach.play();
    }

    /**
     * Handles car exit — frees the first occupied spot, animates exit car through gate.
     */
    private void handleCarExit() {
        if (animating) { addLog("⚠ Animation in progress"); return; }
        int spot = floor.findFirstOccupiedSpot();
        if (spot < 0) { addLog("⚠ No cars inside to exit"); return; }

        animating = true;
        addLog("← Exit sensor triggered");
        entrySensorDriver.onExitSensorTriggered();
        floor.exitCar.setLayoutX(-70);

        double gateX = ParkingFloorPane.FW * 0.38;

        Timeline approach = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(floor.exitCar.layoutXProperty(), -70)),
                new KeyFrame(Duration.millis(700), new KeyValue(floor.exitCar.layoutXProperty(), gateX - 100))
        );
        approach.setOnFinished(e -> {
            PauseTransition pauseAfterOpen = new PauseTransition(Duration.millis(800));
            pauseAfterOpen.setOnFinished(ev2 -> {
                spotSensorDriver.onSensorReading(spot, false);
                floor.updateSpotLight(spot, false);
                addLog("✓ Spot P" + (spot + 1) + " now available");
                Timeline drive = new Timeline(
                        new KeyFrame(Duration.ZERO,       new KeyValue(floor.exitCar.layoutXProperty(), gateX - 100)),
                        new KeyFrame(Duration.millis(700), new KeyValue(floor.exitCar.layoutXProperty(), ParkingFloorPane.FW + 80))
                );
                drive.setOnFinished(evv -> {
                    floor.exitCar.setLayoutX(ParkingFloorPane.FW + 100);
                    PauseTransition close = new PauseTransition(Duration.millis(400));
                    close.setOnFinished(ec -> {
                        gateController.closeExitGate();
                        animating = false;
                    });
                    close.play();
                });
                drive.play();
            });
            pauseAfterOpen.play();
        });
        approach.play();
    }

    /**
     * Resets all spots to available, hides cars, closes gates, clears event log.
     */
    private void handleReset() {
        for (int i = 0; i < ParkingFloorPane.TOTAL_SPOTS; i++) {
            spotSensorDriver.onSensorReading(i, false);
            floor.updateSpotLight(i, false);
        }
        animating = false;
        floor.entryCar.setLayoutX(-70);
        floor.exitCar.setLayoutX(ParkingFloorPane.FW + 100);
        floor.animateEntryGate(false);
        floor.animateExitGate(false);
        logBox.getChildren().clear();
        addLog("↺ System reset — all spots cleared");
    }

    // -------------------------------------------------
    //  DISPLAY HELPERS
    // -------------------------------------------------

    /**
     * Updates all displays through the proper output drivers.
     * This is called when we need to force a refresh.
     */
    private void updateAllDisplays() {
        if (gateController != null) {
            gateController.updateDisplay(ipms.totalAvailable, ipms.floorAvailable);
        }
    }

    /**
     * Updates just the side panel count (this is GUI-specific, not part of the display drivers)
     */
    private void updateSidePanelCount() {
        int a = floor.countAvailableSpots();
        availableCountLabel.setText(String.valueOf(a));
        availableCountLabel.setTextFill(a == 0 ? ParkingFloorPane.SPOT_OCCUPIED : ParkingFloorPane.SPOT_EMPTY);
    }

    private void addLog(String msg) {
        Label l = lbl(msg, "Menlo", 10, false, ParkingFloorPane.TEXT_PRIMARY);
        l.setWrapText(true);
        logBox.getChildren().add(0, l);
        javafx.application.Platform.runLater(() -> logScroll.setVvalue(0));
    }

    // -------------------------------------------------
    //  WIDGET HELPERS
    // -------------------------------------------------

    private Label lbl(String text, String font, double size, boolean bold, Color color) {
        Label l = new Label(text);
        l.setFont(bold ? Font.font(font, FontWeight.BOLD, size) : Font.font(font, size));
        l.setTextFill(color);
        return l;
    }

    private Button styledBtn(String text, Color color) {
        Button b = new Button(text);
        b.setFont(Font.font("Menlo", FontWeight.BOLD, 12));
        b.setTextFill(Color.WHITE);
        b.setPadding(new Insets(10, 24, 10, 24));
        String h = toHex(color);
        b.setStyle("-fx-background-color:" + h + ";-fx-background-radius:6;-fx-cursor:hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:derive(" + h + ",20%);-fx-background-radius:6;-fx-cursor:hand;"));
        b.setOnMouseExited(e  -> b.setStyle("-fx-background-color:" + h + ";-fx-background-radius:6;-fx-cursor:hand;"));
        return b;
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));
    }

    public static void main(String[] args) { launch(args); }
}