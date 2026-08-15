package solo.pixelmonitor.ui.factories;

import javafx.geometry.HPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;

public class GridFactory {
    public static ColumnConstraints getCenteredColumnConstraint() {
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        col.setHalignment(HPos.CENTER);
        return col;
    }
}
