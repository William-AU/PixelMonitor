package solo.pixelmonitor.ui.factories;

import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public class LabelFactory {
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
