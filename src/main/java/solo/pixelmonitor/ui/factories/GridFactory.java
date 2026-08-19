package solo.pixelmonitor.ui.factories;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import solo.pixelmonitor.common.UIConstants;

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

    public static GridPane getCenteredGridWithDefaultInsets() {
        GridPane grid = getCenteredGrid();
        grid.setPadding(UIConstants.DEFAULT_UNIFORM_INSETS);
        return grid;
    }
}
