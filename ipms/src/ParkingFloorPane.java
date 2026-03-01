import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * ParkingFloorPane - builds and manages all visual components of the parking structure.
 * This includes the floor layout with parking spots, gate lanes, info bar,
 * indicator lights, and gate animations.
 */
public class ParkingFloorPane {

    // spot counts
    static final int TOTAL_SPOTS  = 24;
    static final int TOP_SPOTS    = 8;
    static final int BOTTOM_SPOTS = 8;
    static final int LEFT_SPOTS   = 4;
    static final int RIGHT_SPOTS  = 4;

    // spot line and light references for state updates
    Rectangle[][] spotLines  = new Rectangle[TOTAL_SPOTS][2];
    Circle[]       spotLights = new Circle[TOTAL_SPOTS];

    // gate arm references for animation
    private Rectangle entryArm;
    private Rectangle exitArm;
    private Label     entryGateLabel;
    private Label     exitGateLabel;

    // display labels updated when availability changes
    Label floorDisplayLabel;
    Label entranceDisplayTotal;
    Label entranceDisplayFloor;

    // car references
    Car entryCar;
    Car exitCar;

    // layout constants
    static final double FW     = 660;
    static final double FH     = 360;
    static final double MX     = 110;
    static final double MY     = 70;
    static final double SPOT_W = 54;
    static final double SPOT_H = 60;
    static final double SIDE_W = 60;
    static final double SIDE_H = 50;
    static final double LANE_H = 70;

    // colors
    static final Color BG_DARK       = Color.web("#0d1117");
    static final Color BG_PANEL      = Color.web("#161b22");
    static final Color FLOOR_COLOR   = Color.web("#2a2d35");
    static final Color LANE_COLOR    = Color.web("#383c45");
    static final Color SPOT_EMPTY    = Color.web("#2ea043");
    static final Color SPOT_OCCUPIED = Color.web("#da3633");
    static final Color LINE_COLOR    = Color.web("#ffffff");
    static final Color TEXT_PRIMARY  = Color.web("#e6edf3");
    static final Color TEXT_MUTED    = Color.web("#8b949e");
    static final Color GATE_COLOR    = Color.web("#f0883e");
    static final Color DISPLAY_BG    = Color.web("#0d1f2d");
    static final Color DISPLAY_BLUE  = Color.web("#58a6ff");

    // -------------------------------------------------
    //  PARKING FLOOR
    // -------------------------------------------------

    /**
     * Builds the main parking floor pane with all 24 spots,
     * drive lane, structural elements, and floor display.
     */
    Pane buildFloorPane() {
        Pane p = new Pane();
        p.setPrefSize(FW, FH + 16); // extra 16px so bottom lights aren't clipped

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

        double topZoneW   = FW - MX * 2;
        double topSpacing = topZoneW / TOP_SPOTS;

        // top row: spots 0-7
        for (int i = 0; i < TOP_SPOTS; i++) {
            double cx = MX + topSpacing * i + topSpacing / 2;
            addTopSpot(i, p, cx - SPOT_W / 2, 0, SPOT_W, SPOT_H, true);
        }
        // bottom row: spots 8-15
        for (int i = 0; i < BOTTOM_SPOTS; i++) {
            double cx = MX + topSpacing * i + topSpacing / 2;
            addTopSpot(TOP_SPOTS + i, p, cx - SPOT_W / 2, FH - SPOT_H, SPOT_W, SPOT_H, false);
        }
        // left column: spots 16-19
        double sideZoneH  = FH - MY * 2;
        double sideSpacing = sideZoneH / LEFT_SPOTS;
        for (int i = 0; i < LEFT_SPOTS; i++) {
            double cy = MY + sideSpacing * i + sideSpacing / 2;
            addSideSpot(TOP_SPOTS + BOTTOM_SPOTS + i, p, 0, cy - SIDE_H / 2, SIDE_W, SIDE_H, true);
        }
        // right column: spots 20-23
        for (int i = 0; i < RIGHT_SPOTS; i++) {
            double cy = MY + sideSpacing * i + sideSpacing / 2;
            addSideSpot(TOP_SPOTS + BOTTOM_SPOTS + LEFT_SPOTS + i, p, FW - SIDE_W, cy - SIDE_H / 2, SIDE_W, SIDE_H, false);
        }

        return p;
    }

    /**
     * Adds a top or bottom parking spot with two vertical white lines and an indicator light.
     * @param isTop true if spot is in top row (light goes above), false for bottom row (light goes below)
     */
    private void addTopSpot(int id, Pane p, double x, double y, double w, double h, boolean isTop) {
        Rectangle ll = new Rectangle(x, y, 2, h);          ll.setFill(LINE_COLOR);
        Rectangle rl = new Rectangle(x + w - 2, y, 2, h);  rl.setFill(LINE_COLOR);
        spotLines[id][0] = ll; spotLines[id][1] = rl;
        p.getChildren().addAll(ll, rl);

        Label lb = lbl("P" + (id + 1), "Menlo", 8, true, Color.web("#cccccc"));
        lb.setLayoutX(x + w / 2 - 8);
        lb.setLayoutY(isTop ? y + h - 14 : y + 2);
        p.getChildren().add(lb);

        Circle light = new Circle(x + w / 2, isTop ? y - 8 : y + h + 8, 5, SPOT_EMPTY);
        light.setStroke(FLOOR_COLOR); light.setStrokeWidth(1.5);
        spotLights[id] = light;
        p.getChildren().add(light);
    }

    /**
     * Adds a left or right side parking spot with two horizontal white lines and an indicator light.
     * @param isLeft true if spot is in left column (light goes left), false for right column
     */
    private void addSideSpot(int id, Pane p, double x, double y, double w, double h, boolean isLeft) {
        Rectangle tl = new Rectangle(x, y, w, 2);           tl.setFill(LINE_COLOR);
        Rectangle bl = new Rectangle(x, y + h - 2, w, 2);  bl.setFill(LINE_COLOR);
        spotLines[id][0] = tl; spotLines[id][1] = bl;
        p.getChildren().addAll(tl, bl);

        Label lb = lbl("P" + (id + 1), "Menlo", 8, true, Color.web("#cccccc"));
        lb.setLayoutX(isLeft ? x + w - 18 : x + 2);
        lb.setLayoutY(y + h / 2 - 6);
        p.getChildren().add(lb);

        Circle light = new Circle(isLeft ? x - 10 : x + w + 10, y + h / 2, 5, SPOT_EMPTY);
        light.setStroke(FLOOR_COLOR); light.setStrokeWidth(1.5);
        spotLights[id] = light;
        p.getChildren().add(light);
    }

    /**
     * Builds the floor availability display box shown in the center of the floor.
     */
    private void buildFloorDisplay(Pane p, double x, double y) {
        p.getChildren().add(rect(x, y, 126, 46, DISPLAY_BG, DISPLAY_BLUE, 1.5, 6));
        Label t = lbl("▣ FLOOR 1 DISPLAY", "Menlo", 8, true, DISPLAY_BLUE);
        t.setLayoutX(x + 5); t.setLayoutY(y + 3); p.getChildren().add(t);
        floorDisplayLabel = lbl("AVAILABLE: " + TOTAL_SPOTS, "Menlo", 11, true, Color.web("#58ff8a"));
        floorDisplayLabel.setLayoutX(x + 5); floorDisplayLabel.setLayoutY(y + 17); p.getChildren().add(floorDisplayLabel);
        Label sub = lbl("of " + TOTAL_SPOTS + " spots", "Menlo", 9, false, TEXT_MUTED);
        sub.setLayoutX(x + 5); sub.setLayoutY(y + 31); p.getChildren().add(sub);
    }

    // -------------------------------------------------
    //  INFO BAR
    // -------------------------------------------------

    /**
     * Builds the availability display bar shown between the floor and gate lanes.
     */
    HBox buildInfoBar() {
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

    // -------------------------------------------------
    //  ENTRY LANE
    // -------------------------------------------------

    /**
     * Builds the entry gate lane. Car enters from the left and passes through the boom barrier.
     */
    Pane buildEntryLane() {
        Pane lane = new Pane();
        lane.setPrefSize(FW, LANE_H);
        lane.setBackground(new Background(new BackgroundFill(Color.web("#1a1d24"), CornerRadii.EMPTY, Insets.EMPTY)));

        Rectangle road = new Rectangle(0, 5, FW, LANE_H - 10);
        road.setFill(LANE_COLOR);
        lane.getChildren().add(road);

        Label entryLbl = lbl("ENTRY →", "Menlo", 10, true, TEXT_MUTED);
        entryLbl.setLayoutX(14); entryLbl.setLayoutY(10);
        lane.getChildren().add(entryLbl);

        addArrow(lane, 14,  28);
        addArrow(lane, 46,  28);
        addArrow(lane, 78,  28);
        addArrow(lane, 110, 28);

        double gateX = FW * 0.38;
        double armH  = LANE_H - 10;

        Rectangle post = new Rectangle(gateX, 5, 10, armH);
        post.setFill(Color.web("#9ba3ae")); post.setArcWidth(3); post.setArcHeight(3);
        lane.getChildren().add(post);

        entryArm = new Rectangle(gateX - 8, 5, 8, armH);
        entryArm.setFill(GATE_COLOR); entryArm.setArcWidth(3); entryArm.setArcHeight(3);
        lane.getChildren().add(entryArm);
        for (int i = 0; i < 3; i++) {
            Rectangle s = new Rectangle(gateX - 8, 10 + i * 16, 8, 8);
            s.setFill(Color.web("#0d1117", 0.35)); lane.getChildren().add(s);
        }

        entryGateLabel = lbl("● ENTRY GATE CLOSED", "Menlo", 9, true, GATE_COLOR);
        entryGateLabel.setLayoutX(gateX + 20); entryGateLabel.setLayoutY(LANE_H - 16);
        lane.getChildren().add(entryGateLabel);

        entryCar = new Car();
        entryCar.setLayoutX(-70);
        entryCar.setLayoutY(12);
        lane.getChildren().add(entryCar);

        return lane;
    }

    /**
     * Thin black divider between entry and exit lanes.
     */
    Pane buildLaneDivider() {
        Pane div = new Pane();
        div.setPrefSize(FW, 6);
        div.setBackground(new Background(new BackgroundFill(Color.web("#0d1117"), CornerRadii.EMPTY, Insets.EMPTY)));
        return div;
    }

    // -------------------------------------------------
    //  EXIT LANE
    // -------------------------------------------------

    /**
     * Builds the exit gate lane. Car enters from the left, passes through the boom barrier, and exits right.
     */
    Pane buildExitLane() {
        Pane lane = new Pane();
        lane.setPrefSize(FW, LANE_H);
        lane.setBackground(new Background(new BackgroundFill(Color.web("#1a1d24"), CornerRadii.EMPTY, Insets.EMPTY)));

        Rectangle road = new Rectangle(0, 5, FW, LANE_H - 10);
        road.setFill(LANE_COLOR);
        lane.getChildren().add(road);

        Label exitLbl = lbl("EXIT →", "Menlo", 10, true, TEXT_MUTED);
        exitLbl.setLayoutX(14); exitLbl.setLayoutY(10);
        lane.getChildren().add(exitLbl);

        addArrow(lane, 14,  28);
        addArrow(lane, 46,  28);
        addArrow(lane, 78,  28);
        addArrow(lane, 110, 28);

        double gateX = FW * 0.38;
        double armH  = LANE_H - 10;

        Rectangle post = new Rectangle(gateX, 5, 10, armH);
        post.setFill(Color.web("#9ba3ae")); post.setArcWidth(3); post.setArcHeight(3);
        lane.getChildren().add(post);

        exitArm = new Rectangle(gateX - 8, 5, 8, armH);
        exitArm.setFill(GATE_COLOR); exitArm.setArcWidth(3); exitArm.setArcHeight(3);
        lane.getChildren().add(exitArm);
        for (int i = 0; i < 3; i++) {
            Rectangle s = new Rectangle(gateX - 8, 10 + i * 16, 8, 8);
            s.setFill(Color.web("#0d1117", 0.35)); lane.getChildren().add(s);
        }

        exitGateLabel = lbl("● EXIT GATE CLOSED", "Menlo", 9, true, GATE_COLOR);
        exitGateLabel.setLayoutX(gateX + 20); exitGateLabel.setLayoutY(LANE_H - 16);
        lane.getChildren().add(exitGateLabel);

        exitCar = new Car();
        exitCar.setLayoutX(FW + 100); // hidden until exit is triggered
        exitCar.setLayoutY(12);
        lane.getChildren().add(exitCar);

        return lane;
    }

    // -------------------------------------------------
    //  GATE ANIMATION
    // -------------------------------------------------

    /**
     * Animates the entry gate arm open or closed and updates the status label.
     * @param open true to open (raise) the gate, false to close it
     */
    void animateEntryGate(boolean open) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(entryArm.heightProperty(), open ? 0 : LANE_H - 10)));
        tl.play();
        entryGateLabel.setText(open ? "● ENTRY GATE OPEN" : "● ENTRY GATE CLOSED");
        entryGateLabel.setTextFill(open ? SPOT_EMPTY : GATE_COLOR);
    }

    /**
     * Animates the exit gate arm open or closed and updates the status label.
     * @param open true to open (raise) the gate, false to close it
     */
    void animateExitGate(boolean open) {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(300),
                new KeyValue(exitArm.heightProperty(), open ? 0 : LANE_H - 10)));
        tl.play();
        exitGateLabel.setText(open ? "● EXIT GATE OPEN" : "● EXIT GATE CLOSED");
        exitGateLabel.setTextFill(open ? SPOT_EMPTY : GATE_COLOR);
    }

    // -------------------------------------------------
    //  SPOT STATE
    // -------------------------------------------------

    /**
     * Updates the visual state of a parking spot — changes the indicator light
     * and stall line colors to reflect occupied or available status.
     * @param id the spot index (0-based)
     * @param occupied true if the spot is now occupied, false if now available
     */
    void updateSpotLight(int id, boolean occupied) {
        if (id < 0 || id >= TOTAL_SPOTS) return;
        Color c = occupied ? SPOT_OCCUPIED : SPOT_EMPTY;
        spotLights[id].setFill(c);
        spotLines[id][0].setFill(occupied ? SPOT_OCCUPIED.deriveColor(0, 1, 1, 0.6) : LINE_COLOR);
        spotLines[id][1].setFill(occupied ? SPOT_OCCUPIED.deriveColor(0, 1, 1, 0.6) : LINE_COLOR);
        ScaleTransition pulse = new ScaleTransition(Duration.millis(150), spotLights[id]);
        pulse.setFromX(1); pulse.setFromY(1); pulse.setToX(1.4); pulse.setToY(1.4);
        pulse.setAutoReverse(true); pulse.setCycleCount(2); pulse.play();
    }

    /**
     * Returns the index of the first available spot, or -1 if all spots are occupied.
     */
    int findFirstEmptySpot() {
        for (int i = 0; i < TOTAL_SPOTS; i++)
            if (spotLights[i] != null && spotLights[i].getFill().equals(SPOT_EMPTY)) return i;
        return -1;
    }

    /**
     * Returns the index of the first occupied spot, or -1 if no spots are occupied.
     */
    int findFirstOccupiedSpot() {
        for (int i = 0; i < TOTAL_SPOTS; i++)
            if (spotLights[i] != null && spotLights[i].getFill().equals(SPOT_OCCUPIED)) return i;
        return -1;
    }

    /**
     * Counts how many spots are currently available by checking the spotLights array.
     */
    int countAvailableSpots() {
        int count = 0;
        for (int i = 0; i < TOTAL_SPOTS; i++)
            if (spotLights[i] != null && spotLights[i].getFill().equals(SPOT_EMPTY)) count++;
        return count;
    }

    // -------------------------------------------------
    //  SHAPE / WIDGET HELPERS
    // -------------------------------------------------

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

    Label lbl(String text, String font, double size, boolean bold, Color color) {
        Label l = new Label(text);
        l.setFont(bold ? Font.font(font, FontWeight.BOLD, size) : Font.font(font, size));
        l.setTextFill(color);
        return l;
    }

    Rectangle rect(double x, double y, double w, double h, Color fill, Color stroke, double sw, double arc) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill); r.setStroke(stroke); r.setStrokeWidth(sw);
        r.setArcWidth(arc); r.setArcHeight(arc);
        return r;
    }

    VBox vbox(double spacing, Color bg) {
        VBox v = new VBox(spacing);
        v.setPadding(new Insets(12));
        v.setBackground(new Background(new BackgroundFill(bg, new CornerRadii(8), Insets.EMPTY)));
        return v;
    }
}