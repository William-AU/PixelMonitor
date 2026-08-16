package solo.pixelmonitor.ui.factories;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class LabelFactory {
    public static GridPane createCenteredLabel(String text) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        Label label = new Label(text);
        grid.add(label, 0, 0);
        return grid;
    }

    public static GridPane createTitleLabelInGrid(String titleText) {
        GridPane grid = new GridPane();
        ColumnConstraints col1 = GridFactory.getCenteredColumnConstraint();
        grid.getColumnConstraints().add(col1);

        Label titleLabel = new Label(titleText);
        titleLabel.setUnderline(true);
        grid.add(titleLabel, 0, 0);
        return grid;
    }
}
