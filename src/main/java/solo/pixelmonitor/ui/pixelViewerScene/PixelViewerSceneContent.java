package solo.pixelmonitor.ui.pixelViewerScene;

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.logic.imaging.PixelGrid;
import solo.pixelmonitor.logic.imaging.PixelReaderService;
import solo.pixelmonitor.logic.listeners.MouseMoveListener;
import solo.pixelmonitor.ui.factories.LabelFactory;
import solo.pixelmonitor.ui.factories.SpinnerFactory;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Component
public class PixelViewerSceneContent implements MouseMoveListener {
    private final PixelReaderService pixelReaderService;
    private Label globalMouseLabel;
    private Label monitorMouseLabel;
    private Label colorLabel;
    private Rectangle colorRectangle;
    private GridPane pixelGridPane;
    private int radius;

    @Getter
    private Node content;

    public PixelViewerSceneContent(PixelReaderService pixelReaderService) {
        this.pixelReaderService = pixelReaderService;
        // TODO: Get from config
        radius = 1;
    }

    private Node createTopPanel() {
        return LabelFactory.createTitleLabelInGrid("Pixel Viewer");
    }

    private VBox createLeftContent() {
        VBox leftBox = new VBox();
        GridPane positionGrid = new GridPane();
        positionGrid.setHgap(10);
        positionGrid.setVgap(5);

        leftBox.setAlignment(Pos.TOP_LEFT);
        globalMouseLabel = new Label();
        Label globalTitleLabel = new Label("Global Mouse Pos:");
        positionGrid.add(globalTitleLabel, 0, 0);
        positionGrid.add(globalMouseLabel, 1, 0);

        monitorMouseLabel = new Label();
        Label monitorTitleLabel = new Label("Monitor Mouse Pos:");
        positionGrid.add(monitorTitleLabel, 0, 1);
        positionGrid.add(monitorMouseLabel, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setHalignment(HPos.LEFT);
        col2.setHalignment(HPos.RIGHT);
        positionGrid.getColumnConstraints().addAll(col1, col2);

        colorLabel = new Label();

        colorRectangle = new Rectangle();
        colorRectangle.setHeight(colorLabel.getFont().getSize());

        StackPane rectanglePane = new StackPane();
        rectanglePane.getStyleClass().add("no-debug-border");
        rectanglePane.setMaxWidth(Double.MAX_VALUE);
        rectanglePane.setAlignment(Pos.CENTER_LEFT);
        colorRectangle.widthProperty().bind(rectanglePane.widthProperty());
        rectanglePane.setMaxWidth(200);
        rectanglePane.getChildren().add(colorRectangle);

        leftBox.getChildren().addAll(positionGrid, colorLabel, rectanglePane);
        return leftBox;
    }

    private VBox createRightContent() {
        VBox rightBox = new VBox();
        rightBox.setAlignment(Pos.CENTER);

        pixelGridPane = new GridPane();
        pixelGridPane.setHgap(1);
        pixelGridPane.setVgap(1);
        pixelGridPane.setPadding(Insets.EMPTY);

        rightBox.getChildren().add(pixelGridPane);
        return rightBox;
    }

    private Node createOptionsPanel() {
        HBox hBox = new HBox();
        Label radiusInputLabel = new Label("Radius");
        Spinner<Integer> radiusSpinner = new Spinner<>();
        radiusSpinner.setEditable(true);

        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 1, 1);
        radiusSpinner.setValueFactory(valueFactory);
        radiusSpinner.valueProperty().addListener((_, _, newValue) -> radius = newValue);

        radiusSpinner.getEditor().setTextFormatter(SpinnerFactory.getIntegerOnlyFormatter());
        radiusSpinner.getEditor().addEventFilter(ScrollEvent.SCROLL, SpinnerFactory.createSpinnerScrollHandler(radiusSpinner));

        VBox radiusBox = new VBox();
        radiusBox.getChildren().addAll(radiusInputLabel, radiusSpinner);
        hBox.getChildren().add(radiusBox);

        return hBox;
    }

    private Node createCenterPanel() {
        GridPane outerGrid = new GridPane();
        ColumnConstraints outerGridCol1 = new ColumnConstraints();
        outerGridCol1.setPercentWidth(80);
        outerGrid.getColumnConstraints().add(outerGridCol1);
        outerGrid.alignmentProperty().set(Pos.TOP_CENTER);


        Node optionsPanel = createOptionsPanel();

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setAlignment(Pos.CENTER);

        VBox leftContent = createLeftContent();
        grid.add(leftContent, 0, 0);

        VBox rightContent = createRightContent();
        grid.add(rightContent, 1, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        outerGrid.add(optionsPanel, 0, 0);
        outerGrid.add(grid, 0, 1);
        return outerGrid;
    }

    @PostConstruct
    private void setupContent() {
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(createTopPanel());
        contentPane.setCenter(createCenterPanel());

        content = contentPane;
    }


    private void updateRightContent(PixelGrid pixelsAroundCursor) {
        pixelGridPane.getChildren().clear();

        int rows = pixelsAroundCursor.getRows();
        int cols = pixelsAroundCursor.getColumns();
        if (cols <= 0) {
            cols = rows;
        }

        int centerRow = rows / 2;
        int centerCol = cols / 2;

        Map<Integer, List<Color>> rowColors = pixelsAroundCursor.getRowColors();
        rowColors.forEach((rowIndex, rowColor) -> {
            for (int col = 0; col < rowColor.size(); col++) {
                java.awt.Color awtColor = rowColor.get(col);
                javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(
                        awtColor.getRed(),
                        awtColor.getGreen(),
                        awtColor.getBlue(),
                        awtColor.getAlpha() / 255.0
                );
                boolean isCenter = (rowIndex == centerRow && col == centerCol);
                StackPane cell = createColorCell(fxColor, isCenter);
                pixelGridPane.add(cell, col, rowIndex);
            }
        });

        double cellWidth = 10;
        double cellHeight = 10;
        double hSpacing = 1;
        double vSpacing = 1;

        double totalWidth = cols * cellWidth + (cols - 1) * hSpacing;
        double totalHeight = rows * cellHeight + (rows - 1) * vSpacing;

        pixelGridPane.setMinSize(totalWidth, totalHeight);
        pixelGridPane.setPrefSize(totalWidth, totalHeight);
        pixelGridPane.setMaxSize(totalWidth, totalHeight);
    }

    /**
     * Creates a fixed-size StackPane containing a colored rectangle
     */
    private StackPane createColorCell(javafx.scene.paint.Color fillColor, boolean highlight) {
        // TODO: Get from config
        double cellWidth = 10;
        double cellHeight = 10;

        Rectangle rect = new Rectangle(cellWidth, cellHeight, fillColor);

        StackPane cell = new StackPane();
        cell.setPrefSize(cellWidth, cellHeight);
        cell.setMinSize(cellWidth, cellHeight);
        cell.setMaxSize(cellWidth, cellHeight);
        cell.getChildren().add(rect);

        if (highlight) {
            Rectangle border = new Rectangle(cellWidth, cellHeight);
            border.setFill(null);
            border.setStroke(javafx.scene.paint.Color.RED);
            border.setStrokeWidth(1.5);
            cell.getChildren().add(border);
        }

        return cell;
    }

    private void updateLeftContent(Point globalMousePosition, Point monitorMousePosition, Color pixelColor) {
        globalMouseLabel.setText(
                "(" + globalMousePosition.x + "," + globalMousePosition.y + ")"
        );
        monitorMouseLabel.setText(
                "(" + monitorMousePosition.x + "," + monitorMousePosition.y + ")"
        );
        colorLabel.setText(
                "Color (R,G,B,A): (" + pixelColor.getRed() + "," + pixelColor.getGreen() + ","
                        + pixelColor.getBlue() + "," + pixelColor.getAlpha() + ")"
        );
        colorRectangle.setFill(javafx.scene.paint.Color.rgb(
                pixelColor.getRed(),
                pixelColor.getGreen(),
                pixelColor.getBlue(),
                pixelColor.getAlpha() / 255.0   // AWT alpha is 0-255, javafx expects 0.0-1.0
        ));
    }

    @Override
    public void onMouseMoved(int x, int y) {
        if (content == null || content.getScene() == null) {
            return;
        }
        Point globalMousePosition = pixelReaderService.getGlobalMousePosition();
        Point monitorMousePosition = pixelReaderService.getMonitorRelativeMousePosition();
        Color pixelColor = pixelReaderService.getPixelColor(globalMousePosition);
        Platform.runLater(() -> this.updateLeftContent(globalMousePosition, monitorMousePosition, pixelColor));

        PixelGrid pixelsAroundCursor = pixelReaderService.getPixelsAroundPoint(globalMousePosition, radius);
        Platform.runLater(() -> this.updateRightContent(pixelsAroundCursor));
    }
}
