package solo.pixelmonitor.ui.pictureInPictureScene.options;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.common.UIConstants;
import solo.pixelmonitor.logic.imaging.ImageManipulationService;
import solo.pixelmonitor.ui.callbacks.GenericImageTransformationCallback;
import solo.pixelmonitor.ui.callbacks.ImageTransformationButtonDialogueCallback;
import solo.pixelmonitor.ui.factories.GridFactory;
import solo.pixelmonitor.ui.factories.LabelFactory;
import solo.pixelmonitor.ui.factories.SpinnerFactory;
import solo.pixelmonitor.ui.pictureInPictureScene.transformations.ImageTransformation;
import solo.pixelmonitor.ui.sceneManagement.WindowManager;

import java.util.UUID;

@Slf4j
@Component
public class GrayscaleOptionsHandler {
    private final WindowManager windowManager;
    private final UUID uuid;
    private final ImageManipulationService imageManipulationService;

    public GrayscaleOptionsHandler(WindowManager windowManager, ImageManipulationService imageManipulationService) {
        this.windowManager = windowManager;
        this.uuid = UUID.randomUUID();
        this.imageManipulationService = imageManipulationService;
    }

    private ImageTransformation createTransformation(ImageOptionsContext ctx) {
        return (inputImage -> {
            if (ctx.isEnabled()) {
                return imageManipulationService.turnGrayscale(inputImage, ctx.getNumberOfGrayscaleColors());
            }
            return inputImage;
        });
    }

    private void onStageClose(ImageTransformationButtonDialogueCallback callback, ImageOptionsContext ctx) {
        callback.onDialogueClosed(createTransformation(ctx), uuid);
    }

    private void createDialogueAndUpdateOptionsContext(ImageTransformationButtonDialogueCallback callback, ImageOptionsContext ctx, GridPane infoGrid, GenericImageTransformationCallback transformationCallback) {
        BorderPane dialogueBorder = new BorderPane();
        Stage dialogueStage = new Stage();
        dialogueStage.setOnCloseRequest(_ -> onStageClose(callback, ctx));
        Scene dialogueScene = new Scene(dialogueBorder, UIConstants.NEW_DIALOGUE_SMALL_WIDTH, UIConstants.NEW_DIALOGUE_SMALL_HEIGHT);
        dialogueStage.setScene(dialogueScene);
        if (!windowManager.addStage(uuid, dialogueStage)) {
            log.warn("Attempted to open a new Grayscale options dialogue, but one is already open.");
            return;
        }

        dialogueBorder.setTop(LabelFactory.createTitleLabelInGrid("Grayscale Options"));
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setPadding(UIConstants.DEFAULT_UNIFORM_INSETS);
        dialogueBorder.setCenter(grid);

        GridPane toggleGrid = createToggleGrid(ctx, infoGrid, transformationCallback);
        GridPane colorSelectionGrid = createColorSelectionGrid(ctx, infoGrid, transformationCallback);

        grid.add(toggleGrid, 0, 0);
        grid.add(colorSelectionGrid, 1, 0);

        GridPane confirmGrid = GridFactory.getCenteredGridWithDefaultInsets();
        Button confirmButton = new Button("Confirm");
        confirmButton.getStyleClass().add("main-button");
        confirmButton.setOnAction(_ -> {
            windowManager.closeStage(uuid);
            onStageClose(callback, ctx);
        });
        confirmGrid.add(confirmButton, 0, 0);
        dialogueBorder.setBottom(confirmGrid);
    }

    private @NonNull GridPane createToggleGrid(ImageOptionsContext ctx, GridPane infoGrid, GenericImageTransformationCallback callback) {
        GridPane toggleGrid = GridFactory.getCenteredGrid();
        Label spacerLabel = new Label();
        ToggleButton grayscaleToggle = new ToggleButton("Toggle Grayscale");
        grayscaleToggle.setSelected(ctx.isEnabled());
        grayscaleToggle.setOnAction(e -> {
            ToggleButton src = (ToggleButton) e.getSource();
            ctx.setEnabled(src.isSelected());
            callback.onImageTransformChanged(createTransformation(ctx));
            updateInfoGrid(ctx, infoGrid);
        });
        toggleGrid.add(spacerLabel, 0, 0);
        toggleGrid.add(grayscaleToggle, 0, 1);
        return toggleGrid;
    }

    private @NonNull GridPane createColorSelectionGrid(ImageOptionsContext ctx, GridPane infoGrid, GenericImageTransformationCallback callback) {
        GridPane colorSelectionGrid = GridFactory.getCenteredGrid();
        Node colorSelectionLabel = LabelFactory.createCenteredLabel("Number of Colors");
        Spinner<Integer> colorSelectionSpinner = SpinnerFactory.getIntegerDefaultSpinner(2, 16, ctx.getNumberOfGrayscaleColors(), 1);
        colorSelectionSpinner.valueProperty().addListener((_, _, newValue) -> {
            ctx.setNumberOfGrayscaleColors(newValue);
            updateInfoGrid(ctx, infoGrid);
            callback.onImageTransformChanged(createTransformation(ctx));
        });

        colorSelectionGrid.add(colorSelectionLabel, 0, 0);
        colorSelectionGrid.add(colorSelectionSpinner, 0, 1);
        return colorSelectionGrid;
    }

    private void updateInfoGrid(ImageOptionsContext ctx, GridPane infoGrid) {
        String enabledString = ctx.isEnabled() ? "Grayscale Enabled" : "Grayscale Disabled";
        Label enabledLabel = new Label(enabledString);
        enabledLabel.setTextFill(ctx.isEnabled() ? Color.DARKGREEN : Color.RED);
        enabledLabel.setUnderline(true);
        infoGrid.getChildren().clear();
        infoGrid.add(enabledLabel, 0, 0);
        Label numberOfColorsLabel = new Label("Number of Grayscale Colors: " + ctx.getNumberOfGrayscaleColors());
        infoGrid.add(numberOfColorsLabel, 1, 0);
    }

    public Node createGrayscaleOptions(ImageTransformationButtonDialogueCallback dialogueCallback, GenericImageTransformationCallback transformationCallback, ImageOptionsContext ctx) {
        GridPane outerGrid = GridFactory.getCenteredGridWithDefaultInsets();
        GridPane grid = GridFactory.getCenteredGrid();

        Node title = LabelFactory.createCenteredLabel("Grayscale");

        outerGrid.add(title, 0, 0);
        outerGrid.add(grid, 0, 1);
        grid.setPadding(UIConstants.DEFAULT_UNIFORM_INSETS);
        grid.setHgap(UIConstants.DEFAULT_GRID_H_GAP);

        Button button = new Button("Grayscale Options");
        button.setOnAction(_ -> createDialogueAndUpdateOptionsContext(dialogueCallback, ctx, grid, transformationCallback));
        GridPane.setHalignment(button, HPos.CENTER);
        outerGrid.add(button, 0, 2);

        updateInfoGrid(ctx, grid);

        return outerGrid;
    }
}
