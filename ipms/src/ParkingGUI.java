import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ParkingGUI extends Application {

    // ── system wiring ──────────────────────────────────────────────
    private IPMSController ipms;
    private MainEntryExitGateController gateController;
    private MainEntryExitSensorDriver entrySensorDriver;
    private ParkingSpotOccupancyController occupancyController;
    private ParkingSpotOccupancySensorDriver spotSensorDriver;

    // ── spot counts ────────────────────────────────────────────────
    private static final int TOTAL_SPOTS  = 24;
    private static final int TOP_SPOTS    = 8;
    private static final int BOTTOM_SPOTS = 8;
    private static final int LEFT_SPOTS   = 4;
    private static final int RIGHT_SPOTS  = 4;

    private Rectangle[][] spotLines  = new Rectangle[TOTAL_SPOTS][2];
    private Circle[]       spotLights = new Circle[TOTAL_SPOTS];

    // ── gates ──────────────────────────────────────────────────────
    private Rectangle entryArm;
    private Rectangle exitArm;
    private Label     entryGateLabel;
    private Label     exitGateLabel;

    // ── cars ───────────────────────────────────────────────────────
    private Car     entryCar;
    private Car     exitCar;
    private boolean animating = false;

    // ── UI ─────────────────────────────────────────────────────────
    private Label      entranceDisplayTotal;
    private Label      entranceDisplayFloor;
    private Label      floorDisplayLabel;
    private Label      availableCountLabel;
    private VBox       logBox;
    private ScrollPane logScroll;

    // ── layout ─────────────────────────────────────────────────────
    private static final double FW     = 660;
    private static final double FH     = 360;
    private static final double MX     = 110;
    private static final double MY     = 70;
    private static final double SPOT_W = 54;
    private static final double SPOT_H = 60;
    private static final double SIDE_W = 60;
    private static final double SIDE_H = 50;
    private static final double LANE_H = 70;  // height of each gate lane

    // ── colors ─────────────────────────────────────────────────────
    private static final Color BG_DARK       = Color.web("#0d1117");
    private static final Color BG_PANEL      = Color.web("#161b22");
    private static final Color FLOOR_COLOR   = Color.web("#2a2d35");
    private static final Color LANE_COLOR    = Color.web("#383c45");
    private static final Color SPOT_EMPTY    = Color.web("#2ea043");
    private static final Color SPOT_OCCUPIED = Color.web("#da3633");
    private static final Color LINE_COLOR    = Color.web("#ffffff");
    private static final Color TEXT_PRIMARY  = Color.web("#e6edf3");
    private static final Color TEXT_MUTED    = Color.web("#8b949e");
    private static final Color GATE_COLOR    = Color.web("#f0883e");
    private static final Color DISPLAY_BG    = Color.web("#0d1f2d");
    private static final Color DISPLAY_BLUE  = Color.web("#58a6ff");

    // ══════════════════════════════════════════════════════════════
    //  START
    // ══════════════════════════════════════════════════════════════
    @Override
    public void start(Stage stage) {
        initSystem();

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BG_DARK, CornerRadii.EMPTY, Insets.EMPTY)));
        root.setPadding(new Insets(16));
        root.setTop(buildTitleBar());
        root.setLeft(buildSidePanel());

        VBox center = new VBox(0);
        center.setAlignment(Pos.TOP_CENTER);
        center.setFillWidth(false);
        center.getChildren().addAll(
                buildFloorPane(),
                buildInfoBar(),
                buildEntryLane(),
                buildLaneDivider(),
                buildExitLane()
        );
        root.setCenter(center);
        root.setBottom(buildControls());

        Scene scene = new Scene(root, 1040, 800);
        stage.setTitle("CS460 – IPMS Parking Structure Monitor");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        refreshDisplays();
    }

    // ══════════════════════════════════════════════════════════════
    //  SYSTEM INIT
    // ══════════════════════════════════════════════════════════════
    private void initSystem() {
        int[] layout = {TOTAL_SPOTS};
        ipms = new IPMSController(TOTAL_SPOTS, layout) {
            @Override public void updateAvailability() {
                super.updateAvailability();
                javafx.application.Platform.runLater(() -> refreshDisplays());
            }
        };
        gateController = new MainEntryExitGateController(ipms) {
            @Override public void raiseMainGate() {
                super.raiseMainGate();
                javafx.application.Platform.runLater(() -> animateEntryGate(true));
            }
            @Override public void closeMainGate() {
                super.closeMainGate();
                javafx.application.Platform.runLater(() -> animateEntryGate(false));
            }
            @Override public void updateDisplay(int total, int[] floors) {
                javafx.application.Platform.runLater(() -> refreshDisplays());
            }
        };
        occupancyController = new ParkingSpotOccupancyController(ipms) {
            @Override public void setIndicatorLight(int spotID, boolean occupied) {
                javafx.application.Platform.runLater(() -> updateSpotLight(spotID, occupied));
            }
        };
        entrySensorDriver = new MainEntryExitSensorDriver(gateController);
        spotSensorDriver  = new ParkingSpotOccupancySensorDriver(occupancyController);
    }

    // ══════════════════════════════════════════════════════════════
    //  TITLE BAR
    // ══════════════════════════════════════════════════════════════
    private HBox buildTitleBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 0, 12, 0));
        Label title = lbl("IPMS  //  Parking Structure Monitor  //  Floor 1", "Menlo", 13, true, TEXT_PRIMARY);
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label sys = lbl("● SYSTEM OPERATIONAL", "Menlo", 11, false, SPOT_EMPTY);
        bar.getChildren().addAll(title, sp, sys);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════
    //  PARKING FLOOR
    // ══════════════════════════════════════════════════════════════
    private Pane buildFloorPane() {
        Pane p = new Pane();
        p.setPrefSize(FW, FH);

        Rectangle floor = new Rectangle(0, 0, FW, FH);
        floor.setFill(FLOOR_COLOR);
        p.getChildren().add(floor);

        Rectangle lane = new Rectangle(MX, MY, FW - MX * 2, FH - MY * 2);
        lane.setFill(LANE_COLOR);
        p.getChildren().add(lane);

        addColumn(p, FW / 2 - 55, FH / 2 - 14);
        addColumn(p, FW / 2 + 32, FH / 2 - 14);
        addStairsElevator(p, FW / 2 - 22, FH / 2 - 28);
        buildFloorDisplay(p, FW / 2 - 55, FH / 2 + 22);

        double topZoneW  = FW - MX * 2;
        double topSpacing = topZoneW / TOP_SPOTS;

        // top row: spots 0–7
        for (int i = 0; i < TOP_SPOTS; i++) {
            double cx = MX + topSpacing * i + topSpacing / 2;
            addTopSpot(i, p, cx - SPOT_W / 2, 0, SPOT_W, SPOT_H, true);
        }
        // bottom row: spots 8–15
        for (int i = 0; i < BOTTOM_SPOTS; i++) {
            double cx = MX + topSpacing * i + topSpacing / 2;
            addTopSpot(TOP_SPOTS + i, p, cx - SPOT_W / 2, FH - SPOT_H, SPOT_W, SPOT_H, false);
        }
        // left column: spots 16–19
        double sideZoneH  = FH - MY * 2;
        double sideSpacing = sideZoneH / LEFT_SPOTS;
        for (int i = 0; i < LEFT_SPOTS; i++) {
            double cy = MY + sideSpacing * i + sideSpacing / 2;
            addSideSpot(TOP_SPOTS + BOTTOM_SPOTS + i, p, 0, cy - SIDE_H / 2, SIDE_W, SIDE_H, true);
        }
        // right column: spots 20–23
        for (int i = 0; i < RIGHT_SPOTS; i++) {
            double cy = MY + sideSpacing * i + sideSpacing / 2;
            addSideSpot(TOP_SPOTS + BOTTOM_SPOTS + LEFT_SPOTS + i, p, FW - SIDE_W, cy - SIDE_H / 2, SIDE_W, SIDE_H, false);
        }

        return p;
    }

    private void addTopSpot(int id, Pane p, double x, double y, double w, double h, boolean isTop) {
        Rectangle ll = new Rectangle(x, y, 2, h);         ll.setFill(LINE_COLOR);
        Rectangle rl = new Rectangle(x + w - 2, y, 2, h); rl.setFill(LINE_COLOR);
        spotLines[id][0] = ll; spotLines[id][1] = rl;
        p.getChildren().addAll(ll, rl);
        Label lb = lbl("P" + (id + 1), "Menlo", 8, true, Color.web("#cccccc"));
        lb.setLayoutX(x + w / 2 - 8); lb.setLayoutY(isTop ? y + h - 14 : y + 2);
        p.getChildren().add(lb);
        Circle light = new Circle(x + w / 2, isTop ? y - 8 : y + h + 8, 5, SPOT_EMPTY);
        light.setStroke(FLOOR_COLOR); light.setStrokeWidth(1.5);
        spotLights[id] = light; p.getChildren().add(light);
    }

    private void addSideSpot(int id, Pane p, double x, double y, double w, double h, boolean isLeft) {
        Rectangle tl = new Rectangle(x, y, w, 2);           tl.setFill(LINE_COLOR);
        Rectangle bl = new Rectangle(x, y + h - 2, w, 2);  bl.setFill(LINE_COLOR);
        spotLines[id][0] = tl; spotLines[id][1] = bl;
        p.getChildren().addAll(tl, bl);
        Label lb = lbl("P" + (id + 1), "Menlo", 8, true, Color.web("#cccccc"));
        lb.setLayoutX(isLeft ? x + w - 18 : x + 2); lb.setLayoutY(y + h / 2 - 6);
        p.getChildren().add(lb);
        Circle light = new Circle(isLeft ? x - 10 : x + w + 10, y + h / 2, 5, SPOT_EMPTY);
        light.setStroke(FLOOR_COLOR); light.setStrokeWidth(1.5);
        spotLights[id] = light; p.getChildren().add(light);
    }

    private void buildFloorDisplay(Pane p, double x, double y) {
        p.getChildren().add(rect(x, y, 126, 46, DISPLAY_BG, DISPLAY_BLUE, 1.5, 6));
        Label t = lbl("▣ FLOOR 1 DISPLAY", "Menlo", 8, true, DISPLAY_BLUE);
        t.setLayoutX(x + 5); t.setLayoutY(y + 3); p.getChildren().add(t);
        floorDisplayLabel = lbl("AVAILABLE: " + TOTAL_SPOTS, "Menlo", 11, true, Color.web("#58ff8a"));
        floorDisplayLabel.setLayoutX(x + 5); floorDisplayLabel.setLayoutY(y + 17); p.getChildren().add(floorDisplayLabel);
        Label sub = lbl("of " + TOTAL_SPOTS + " spots", "Menlo", 9, false, TEXT_MUTED);
        sub.setLayoutX(x + 5); sub.setLayoutY(y + 31); p.getChildren().add(sub);
    }

    // ══════════════════════════════════════════════════════════════
    //  INFO BAR
    // ══════════════════════════════════════════════════════════════
    private HBox buildInfoBar() {
        HBox bar = new HBox(16);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.setAlignment(Pos.CENTER);
        bar.setPrefWidth(FW);
        bar.setBackground(new Background(new BackgroundFill(Color.web("#161b22"), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox disp = new VBox(3);
        disp.setPadding(new Insets(5, 10, 5, 10));
        disp.setBackground(new Background(new BackgroundFill(DISPLAY_BG, new CornerRadii(6), Insets.EMPTY)));
        disp.setStyle("-fx-border-color: #58a6ff; -fx-border-radius: 6; -fx-border-width: 1.5;");

        Label dispTitle = lbl("▣ MAIN ENTRANCE AVAILABILITY DISPLAY", "Menlo", 8, true, DISPLAY_BLUE);
        HBox counts = new HBox(20);
        entranceDisplayTotal = lbl("TOTAL: " + TOTAL_SPOTS + " / " + TOTAL_SPOTS, "Menlo", 11, true, Color.web("#58ff8a"));
        entranceDisplayFloor = lbl("FLOOR 1: " + TOTAL_SPOTS + " AVAIL", "Menlo", 11, false, TEXT_PRIMARY);
        counts.getChildren().addAll(entranceDisplayTotal, entranceDisplayFloor);
        disp.getChildren().addAll(dispTitle, counts);
        bar.getChildren().add(disp);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════
    //  ENTRY LANE (top gate lane — car enters from left)
    // ══════════════════════════════════════════════════════════════
    private Pane buildEntryLane() {
        Pane lane = new Pane();
        lane.setPrefSize(FW, LANE_H);
        lane.setBackground(new Background(new BackgroundFill(Color.web("#1a1d24"), CornerRadii.EMPTY, Insets.EMPTY)));

        // road
        Rectangle road = new Rectangle(0, 5, FW, LANE_H - 10);
        road.setFill(LANE_COLOR);
        lane.getChildren().add(road);

        // ENTRY label on left
        Label entryLbl = lbl("ENTRY →", "Menlo", 10, true, TEXT_MUTED);
        entryLbl.setLayoutX(14); entryLbl.setLayoutY(10);
        lane.getChildren().add(entryLbl);

        // arrows pointing right toward gate
        addArrow(lane, 14,  28);
        addArrow(lane, 46,  28);
        addArrow(lane, 78,  28);
        addArrow(lane, 110, 28);

        // gate post — same X as exit gate so they're parallel/aligned
        double gateX = FW * 0.38;
        double armH  = LANE_H - 10;

        Rectangle post = new Rectangle(gateX, 5, 10, armH);
        post.setFill(Color.web("#9ba3ae")); post.setArcWidth(3); post.setArcHeight(3);
        lane.getChildren().add(post);

        // vertical boom arm — sits to the LEFT of post, faces the incoming car
        entryArm = new Rectangle(gateX - 8, 5, 8, armH);
        entryArm.setFill(GATE_COLOR); entryArm.setArcWidth(3); entryArm.setArcHeight(3);
        lane.getChildren().add(entryArm);
        for (int i = 0; i < 3; i++) {
            Rectangle s = new Rectangle(gateX - 8, 10 + i * 16, 8, 8);
            s.setFill(Color.web("#0d1117", 0.35)); lane.getChildren().add(s);
        }

        // gate status
        entryGateLabel = lbl("● ENTRY GATE CLOSED", "Menlo", 9, true, GATE_COLOR);
        entryGateLabel.setLayoutX(gateX + 20); entryGateLabel.setLayoutY(LANE_H - 16);
        lane.getChildren().add(entryGateLabel);

        // entry car spawns off-screen left
        entryCar = new Car();
        entryCar.setLayoutX(-70);
        entryCar.setLayoutY(12);
        lane.getChildren().add(entryCar);

        return lane;
    }

    // thin divider between entry and exit lanes
    private Pane buildLaneDivider() {
        Pane div = new Pane();
        div.setPrefSize(FW, 6);
        div.setBackground(new Background(new BackgroundFill(Color.web("#0d1117"), CornerRadii.EMPTY, Insets.EMPTY)));
        return div;
    }

    // ══════════════════════════════════════════════════════════════
    //  EXIT LANE (bottom gate lane — car exits to the right)
    // ══════════════════════════════════════════════════════════════
    private Pane buildExitLane() {
        Pane lane = new Pane();
        lane.setPrefSize(FW, LANE_H);
        lane.setBackground(new Background(new BackgroundFill(Color.web("#1a1d24"), CornerRadii.EMPTY, Insets.EMPTY)));

        // road
        Rectangle road = new Rectangle(0, 5, FW, LANE_H - 10);
        road.setFill(LANE_COLOR);
        lane.getChildren().add(road);

        // EXIT label on left (matching entry)
        Label exitLbl = lbl("EXIT →", "Menlo", 10, true, TEXT_MUTED);
        exitLbl.setLayoutX(14); exitLbl.setLayoutY(10);
        lane.getChildren().add(exitLbl);

        // arrows pointing right toward gate (matching entry layout)
        addArrow(lane, 14,  28);
        addArrow(lane, 46,  28);
        addArrow(lane, 78,  28);
        addArrow(lane, 110, 28);

        // gate post — same X as entry gate so they're perfectly parallel
        double gateX = FW * 0.38;
        double armH  = LANE_H - 10;

        Rectangle post = new Rectangle(gateX, 5, 10, armH);
        post.setFill(Color.web("#9ba3ae")); post.setArcWidth(3); post.setArcHeight(3);
        lane.getChildren().add(post);

        // vertical boom arm — sits to the left of post, faces the car
        exitArm = new Rectangle(gateX - 8, 5, 8, armH);
        exitArm.setFill(GATE_COLOR); exitArm.setArcWidth(3); exitArm.setArcHeight(3);
        lane.getChildren().add(exitArm);
        for (int i = 0; i < 3; i++) {
            Rectangle s = new Rectangle(gateX - 8, 10 + i * 16, 8, 8);
            s.setFill(Color.web("#0d1117", 0.35)); lane.getChildren().add(s);
        }

        // gate status
        exitGateLabel = lbl("● EXIT GATE CLOSED", "Menlo", 9, true, GATE_COLOR);
        exitGateLabel.setLayoutX(gateX + 20); exitGateLabel.setLayoutY(LANE_H - 16);
        lane.getChildren().add(exitGateLabel);

        // exit car hidden off-screen until needed
        exitCar = new Car();
        exitCar.setLayoutX(FW + 100);
        exitCar.setLayoutY(12);
        lane.getChildren().add(exitCar);

        return lane;
    }

    // ══════════════════════════════════════════════════════════════
    //  SIDE PANEL
    // ══════════════════════════════════════════════════════════════
    private VBox buildSidePanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(200);
        panel.setPadding(new Insets(0, 14, 0, 0));

        VBox countBox = vbox(12, BG_PANEL);
        Label ct = lbl("AVAILABLE SPACES", "Menlo", 10, true, TEXT_MUTED);
        availableCountLabel = lbl(String.valueOf(TOTAL_SPOTS), "Menlo", 42, true, SPOT_EMPTY);
        Label tot = lbl("of " + TOTAL_SPOTS + " total", "Menlo", 10, false, TEXT_MUTED);
        countBox.getChildren().addAll(ct, availableCountLabel, tot);

        VBox legendBox = vbox(8, BG_PANEL);
        Label lt = lbl("INDICATOR LIGHTS", "Menlo", 10, true, TEXT_MUTED);
        legendBox.getChildren().addAll(lt,
                lbl("● GREEN — Available", "Menlo", 11, false, SPOT_EMPTY),
                lbl("● RED   — Occupied",  "Menlo", 11, false, SPOT_OCCUPIED));

        Label logTitle = lbl("EVENT LOG", "Menlo", 10, true, TEXT_MUTED);
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

    // ══════════════════════════════════════════════════════════════
    //  CONTROLS
    // ══════════════════════════════════════════════════════════════
    private HBox buildControls() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(12, 0, 0, 0));
        Button e = styledBtn("→  Car Enters", DISPLAY_BLUE);
        Button x = styledBtn("→  Car Exits",  GATE_COLOR);
        Button r = styledBtn("↺  Reset",       Color.web("#30363d"));
        e.setOnAction(ev -> handleCarEnter());
        x.setOnAction(ev -> handleCarExit());
        r.setOnAction(ev -> handleReset());
        bar.getChildren().addAll(e, x, r);
        return bar;
    }

    // ══════════════════════════════════════════════════════════════
    //  BUTTON HANDLERS
    // ══════════════════════════════════════════════════════════════
    private void handleCarEnter() {
        if (animating) { addLog("⚠ Animation in progress"); return; }
        if (!ipms.capacityAvailable()) { addLog("✗ Structure full — entry denied"); return; }
        int spot = findFirstEmptySpot();
        if (spot < 0) { addLog("✗ No empty spots"); return; }

        animating = true;
        addLog("→ Entry sensor triggered");
        entrySensorDriver.onEntrySensorTriggered();
        entryCar.setLayoutX(-70);

        double gateX = FW * 0.38;

        Timeline approach = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(entryCar.layoutXProperty(), -70)),
                new KeyFrame(Duration.millis(700), new KeyValue(entryCar.layoutXProperty(), gateX - 100))
        );
        approach.setOnFinished(e -> {
            // car stopped — pause, THEN open gate
            PauseTransition pauseBeforeOpen = new PauseTransition(Duration.millis(800));
            pauseBeforeOpen.setOnFinished(ev1 -> {
                animateEntryGate(true);
                PauseTransition pauseAfterOpen = new PauseTransition(Duration.millis(500));
                pauseAfterOpen.setOnFinished(ev2 -> {
                    Timeline drive = new Timeline(
                            new KeyFrame(Duration.ZERO,       new KeyValue(entryCar.layoutXProperty(), gateX - 100)),
                            new KeyFrame(Duration.millis(600), new KeyValue(entryCar.layoutXProperty(), FW + 80))
                    );
                    drive.setOnFinished(evv -> {
                        spotSensorDriver.onSensorReading(spot, true);
                        addLog("✓ Parked in spot P" + (spot + 1));
                        entryCar.setLayoutX(-70);
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
            pauseBeforeOpen.play();
        });
        approach.play();
    }

    private void handleCarExit() {
        if (animating) { addLog("⚠ Animation in progress"); return; }
        int spot = findFirstOccupiedSpot();
        if (spot < 0) { addLog("⚠ No cars inside to exit"); return; }

        animating = true;
        addLog("← Exit sensor triggered");
        exitCar.setLayoutX(-70);

        double gateX = FW * 0.38;

        // car drives up to exit gate from the left
        Timeline approach = new Timeline(
                new KeyFrame(Duration.ZERO,       new KeyValue(exitCar.layoutXProperty(), -70)),
                new KeyFrame(Duration.millis(700), new KeyValue(exitCar.layoutXProperty(), gateX - 100))
        );
        approach.setOnFinished(e -> {
            // car stopped — pause, THEN open gate
            PauseTransition pauseBeforeOpen = new PauseTransition(Duration.millis(800));
            pauseBeforeOpen.setOnFinished(ev1 -> {
                animateExitGate(true);
                spotSensorDriver.onSensorReading(spot, false);
                addLog("✓ Spot P" + (spot + 1) + " now available");
                PauseTransition pauseAfterOpen = new PauseTransition(Duration.millis(500));
                pauseAfterOpen.setOnFinished(ev2 -> {
                    Timeline drive = new Timeline(
                            new KeyFrame(Duration.ZERO,       new KeyValue(exitCar.layoutXProperty(), gateX - 100)),
                            new KeyFrame(Duration.millis(700), new KeyValue(exitCar.layoutXProperty(), FW + 80))
                    );
                    drive.setOnFinished(evv -> {
                        exitCar.setLayoutX(FW + 100);  // hide off-screen
                        PauseTransition close = new PauseTransition(Duration.millis(400));
                        close.setOnFinished(ec -> {
                            animateExitGate(false);
                            animating = false;
                        });
                        close.play();
                    });
                    drive.play();
                });
                pauseAfterOpen.play();
            });
            pauseBeforeOpen.play();
        });
        approach.play();
    }

    private void handleReset() {
        for (int i = 0; i < TOTAL_SPOTS; i++) spotSensorDriver.onSensorReading(i, false);
        animating = false;
        entryCar.setLayoutX(-70);
        exitCar.setLayoutX(FW + 100);
        animateEntryGate(false);
        animateExitGate(false);
        logBox.getChildren().clear();
        addLog("↺ System reset — all spots cleared");
        refreshDisplays();
    }

    // ══════════════════════════════════════════════════════════════
    //  GATE ANIMATION
    // ══════════════════════════════════════════════════════════════
    private void animateEntryGate(boolean open) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(entryArm.heightProperty(), open ? 0 : LANE_H - 10)));
        tl.play();
        entryGateLabel.setText(open ? "● ENTRY GATE OPEN" : "● ENTRY GATE CLOSED");
        entryGateLabel.setTextFill(open ? SPOT_EMPTY : GATE_COLOR);
    }

    private void animateExitGate(boolean open) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(exitArm.heightProperty(), open ? 0 : LANE_H - 10)));
        tl.play();
        exitGateLabel.setText(open ? "● EXIT GATE OPEN" : "● EXIT GATE CLOSED");
        exitGateLabel.setTextFill(open ? SPOT_EMPTY : GATE_COLOR);
    }

    // ══════════════════════════════════════════════════════════════
    //  SPOT HELPERS
    // ══════════════════════════════════════════════════════════════
    private int findFirstEmptySpot() {
        for (int i = 0; i < TOTAL_SPOTS; i++)
            if (spotLights[i] != null && spotLights[i].getFill().equals(SPOT_EMPTY)) return i;
        return -1;
    }

    private int findFirstOccupiedSpot() {
        for (int i = 0; i < TOTAL_SPOTS; i++)
            if (spotLights[i] != null && spotLights[i].getFill().equals(SPOT_OCCUPIED)) return i;
        return -1;
    }

    private void updateSpotLight(int id, boolean occupied) {
        if (id < 0 || id >= TOTAL_SPOTS) return;
        Color c = occupied ? SPOT_OCCUPIED : SPOT_EMPTY;
        spotLights[id].setFill(c);
        spotLines[id][0].setFill(occupied ? SPOT_OCCUPIED.deriveColor(0, 1, 1, 0.6) : LINE_COLOR);
        spotLines[id][1].setFill(occupied ? SPOT_OCCUPIED.deriveColor(0, 1, 1, 0.6) : LINE_COLOR);
        ScaleTransition pulse = new ScaleTransition(Duration.millis(150), spotLights[id]);
        pulse.setFromX(1); pulse.setFromY(1); pulse.setToX(1.4); pulse.setToY(1.4);
        pulse.setAutoReverse(true); pulse.setCycleCount(2); pulse.play();
    }

    private void refreshDisplays() {
        int a = ipms.totalAvailable;
        availableCountLabel.setText(String.valueOf(a));
        availableCountLabel.setTextFill(a == 0 ? SPOT_OCCUPIED : SPOT_EMPTY);
        if (a == 0) {
            entranceDisplayTotal.setText("NO AVAILABILITY");   entranceDisplayTotal.setTextFill(SPOT_OCCUPIED);
            entranceDisplayFloor.setText("STRUCTURE FULL");    entranceDisplayFloor.setTextFill(SPOT_OCCUPIED);
            floorDisplayLabel.setText("FLOOR FULL");           floorDisplayLabel.setTextFill(SPOT_OCCUPIED);
        } else {
            entranceDisplayTotal.setText("TOTAL: " + a + " / " + TOTAL_SPOTS); entranceDisplayTotal.setTextFill(Color.web("#58ff8a"));
            entranceDisplayFloor.setText("FLOOR 1: " + a + " AVAIL");          entranceDisplayFloor.setTextFill(TEXT_PRIMARY);
            floorDisplayLabel.setText("AVAILABLE: " + a);                       floorDisplayLabel.setTextFill(Color.web("#58ff8a"));
        }
    }

    private void addLog(String msg) {
        Label l = lbl(msg, "Menlo", 10, false, TEXT_PRIMARY);
        l.setWrapText(true);
        logBox.getChildren().add(0, l);
        javafx.application.Platform.runLater(() -> logScroll.setVvalue(0));
    }

    // ══════════════════════════════════════════════════════════════
    //  SHAPE HELPERS
    // ══════════════════════════════════════════════════════════════
    private void addArrow(Pane p, double x, double y) {
        Polygon a = new Polygon(x,y+5, x+16,y+5, x+16,y, x+24,y+8, x+16,y+16, x+16,y+11, x,y+11);
        a.setFill(GATE_COLOR.deriveColor(0, 1, 1, 0.4));
        p.getChildren().add(a);
    }

    private void addColumn(Pane p, double x, double y) {
        Rectangle c = new Rectangle(x, y, 18, 18);
        c.setFill(Color.web("#555b66")); c.setArcWidth(3); c.setArcHeight(3);
        p.getChildren().add(c);
    }

    private void addStairsElevator(Pane p, double x, double y) {
        for (int i = 0; i < 3; i++) {
            Rectangle s = new Rectangle(x + i * 4, y + 14 - i * 5, 8, 4);
            s.setFill(TEXT_MUTED); p.getChildren().add(s);
        }
        Label sl = lbl("STAIRS", "Menlo", 7, false, TEXT_MUTED);
        sl.setLayoutX(x - 2); sl.setLayoutY(y + 20); p.getChildren().add(sl);
        Rectangle el = new Rectangle(x + 28, y + 3, 15, 18);
        el.setFill(LANE_COLOR); el.setStroke(TEXT_MUTED); el.setStrokeWidth(1);
        p.getChildren().add(el);
        Label ell = lbl("ELEV", "Menlo", 7, false, TEXT_MUTED);
        ell.setLayoutX(x + 29); ell.setLayoutY(y + 24); p.getChildren().add(ell);
    }

    // ── widget helpers ────────────────────────────────────────────
    private Label lbl(String text, String font, double size, boolean bold, Color color) {
        Label l = new Label(text);
        l.setFont(bold ? Font.font(font, FontWeight.BOLD, size) : Font.font(font, size));
        l.setTextFill(color);
        return l;
    }

    private Rectangle rect(double x, double y, double w, double h, Color fill, Color stroke, double sw, double arc) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill); r.setStroke(stroke); r.setStrokeWidth(sw);
        r.setArcWidth(arc); r.setArcHeight(arc);
        return r;
    }

    private VBox vbox(double spacing, Color bg) {
        VBox v = new VBox(spacing);
        v.setPadding(new Insets(12));
        v.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(8), Insets.EMPTY)));
        return v;
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

    // ══════════════════════════════════════════════════════════════
    //  CAR (inner class)
    // ══════════════════════════════════════════════════════════════
    static class Car extends Group {
        public Car() {
            Rectangle body = new Rectangle(0, 6, 56, 28);
            body.setArcWidth(8); body.setArcHeight(8); body.setFill(Color.web("#58a6ff"));
            Rectangle cabin = new Rectangle(10, 0, 34, 18);
            cabin.setArcWidth(6); cabin.setArcHeight(6); cabin.setFill(Color.web("#3a7bd5"));
            Circle w1 = new Circle(10, 36, 7, Color.web("#111418"));
            Circle w2 = new Circle(46, 36, 7, Color.web("#111418"));
            Circle r1 = new Circle(10, 36, 3, Color.web("#444950"));
            Circle r2 = new Circle(46, 36, 3, Color.web("#444950"));
            Rectangle hl = new Rectangle(54, 14, 5, 8);
            hl.setFill(Color.web("#fff9c4")); hl.setArcWidth(2); hl.setArcHeight(2);
            Rectangle tl = new Rectangle(0, 14, 4, 8);
            tl.setFill(Color.web("#ff4444", 0.7));
            getChildren().addAll(body, cabin, w1, w2, r1, r2, hl, tl);
        }
    }

    public static void main(String[] args) { launch(args); }
}