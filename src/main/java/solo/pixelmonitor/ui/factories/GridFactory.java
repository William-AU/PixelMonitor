package solo.pixelmonitor.ui.factories;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class GridFactory {
    public static ColumnConstraints getCenteredColumnConstraint() {
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        col.setHalignment(HPos.CENTER);
        return col;
    }

    public static GridPane getCenteredGrid() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        return grid;
    }
}
