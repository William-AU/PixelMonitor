package solo.pixelmonitor.ui.pictureInPictureScene;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.common.UIConstants;
import solo.pixelmonitor.logic.imaging.pictureInPicture.PictureInPictureResult;
import solo.pixelmonitor.logic.imaging.pictureInPicture.PictureInPictureService;
import solo.pixelmonitor.ui.factories.ImageFactory;
import solo.pixelmonitor.ui.factories.LabelFactory;
import solo.pixelmonitor.ui.sceneManagement.WindowManager;

import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * Class responsible for handling the dialogue window responsible for finding and displaying the target image
 * (if it was found) in the source image.
 */
@Slf4j
@Component
public class FindTargetImageHandler {
    private final WindowManager windowManager;
    private final UUID findTargetImageStaticID;
    private final PictureInPictureService pictureInPictureService;
    private ImageView outputImageView;
    private boolean isTargetShown;

    public FindTargetImageHandler(WindowManager windowManager, PictureInPictureService pictureInPictureService) {
        this.findTargetImageStaticID = UUID.randomUUID();
        this.pictureInPictureService = pictureInPictureService;
        this.windowManager = windowManager;
        this.isTargetShown = false;
    }

    private String convertCoordinatesToString(int x, int y) {
        return "(" + x + "," + y + ")";
    }

    private Node createLeftPanel(PictureInPictureResult result) {
        GridPane grid = new GridPane();
        grid.setHgap(UIConstants.DEFAULT_GRID_H_GAP);
        grid.setVgap(UIConstants.DEFAULT_GRID_V_GAP);
        grid.setPadding(UIConstants.DEFAULT_UNIFORM_INSETS);
        grid.setAlignment(Pos.CENTER);

        VBox infoBox = new VBox();
        Node infoTitle = LabelFactory.createCenteredLabel("Results");
        Label foundLabel = new Label("Image Found: " + (result.imageFound() ? "TRUE" : "FALSE"));
        Label widthLabel = new Label("Width: " + (result.imageFound() ? result.foundWidth() : "NOT FOUND"));
        Label heightLabel = new Label("Height: " + (result.imageFound() ? result.foundHeight() : "NOT FOUND"));
        Label startCoordinateLabel = new Label("First Pixel Coordinates: " + (result.imageFound() ? convertCoordinatesToString(result.x(), result.y()) : "NOT FOUND"));
        infoBox.getChildren().addAll(
                infoTitle,
                foundLabel,
                widthLabel,
                heightLabel,
                startCoordinateLabel
        );
        Button showTargetButton = new Button("Toggle Target in Source");
        showTargetButton.setOnAction(_ -> {
            if (isTargetShown) {
                outputImageView.setImage(SwingFXUtils.toFXImage(result.original(), null));
            } else {
                outputImageView.setImage(SwingFXUtils.toFXImage(result.sourceImageWithTarget(), null));
            }
            isTargetShown = !isTargetShown;
        });
        grid.add(infoBox, 0, 0);
        grid.add(showTargetButton, 0, 1);
        return grid;
    }

    protected void handleFindPictureInSourceButtonClicked(BufferedImage targetImage, BufferedImage sourceImage) {
        if (targetImage == null && sourceImage == null) {
            Alert noImagesSelectedAlert = new Alert(
                    Alert.AlertType.ERROR,
                    "No images selected, please select a target image and a source image to use this feature.");
            noImagesSelectedAlert.show();
            return;
        }
        if (targetImage == null) {
            Alert noTargetSelectedAlert = new Alert(
                    Alert.AlertType.ERROR,
                    "Target image has not been selected, please select one before using this feature.");
            noTargetSelectedAlert.show();
            return;
        }
        if (sourceImage == null) {
            Alert noSourceImageSelectedAlert = new Alert(
                    Alert.AlertType.ERROR,
                    "Source image has not been selected, please select one before using this feature.");
            noSourceImageSelectedAlert.show();
            return;
        }

        BorderPane findTargetBorder = new BorderPane();
        Stage findTargetStage = new Stage();
        Scene findTargetScene = new Scene(findTargetBorder, 1280, 720);
        findTargetStage.setScene(findTargetScene);
        if (!windowManager.addStage(findTargetImageStaticID, findTargetStage)) {
            // TODO: Same considerations as importImageFromImageCompare
            log.warn("Attempted to open new find target image screen when one is already open.");
            return;
        }

        // TODO: Call the correct method depending on options
        PictureInPictureResult resultImage = pictureInPictureService.findPictureExact(targetImage, sourceImage);
        Node title = LabelFactory.createTitleLabelInGrid("Picture in Picture Result");
        findTargetBorder.setTop(title);
        ImageFactory.StaticImageOutput imageOutput = ImageFactory.createStaticImageViewWithScrollPaneAndCoordinateLabel(resultImage.original());
        outputImageView = imageOutput.outputImageView();
        GridPane grid = new GridPane();
        grid.add(imageOutput.scrollPane(), 0, 0);
        findTargetBorder.setCenter(grid);
        findTargetBorder.setBottom(imageOutput.coordinateLabel());
        findTargetBorder.setLeft(createLeftPanel(resultImage));
    }
}
