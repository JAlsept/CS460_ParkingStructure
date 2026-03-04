package GUI;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

/**
 * Car is the visual representation of a vehicle in the gate lanes.
 */
public class Car extends Group {

    public Car() {

        // car body
        Rectangle body = new Rectangle(0, 6, 56, 28);
        body.setArcWidth(8); body.setArcHeight(8);
        body.setFill(Color.web("#58a6ff"));

        // cabin
        Rectangle cabin = new Rectangle(10, 0, 34, 18);
        cabin.setArcWidth(6); cabin.setArcHeight(6);
        cabin.setFill(Color.web("#3a7bd5"));

        // wheels
        Circle w1 = new Circle(10, 36, 7, Color.web("#111418"));
        Circle w2 = new Circle(46, 36, 7, Color.web("#111418"));
        Circle r1 = new Circle(10, 36, 3, Color.web("#444950"));
        Circle r2 = new Circle(46, 36, 3, Color.web("#444950"));

        // headlight
        Rectangle hl = new Rectangle(54, 14, 5, 8);
        hl.setFill(Color.web("#fff9c4"));
        hl.setArcWidth(2); hl.setArcHeight(2);

        // taillight
        Rectangle tl = new Rectangle(0, 14, 4, 8);
        tl.setFill(Color.web("#ff4444", 0.7));

        getChildren().addAll(body, cabin, w1, w2, r1, r2, hl, tl);
    }
}