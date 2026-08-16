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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.common.SharedApplicationContext;
import solo.pixelmonitor.common.UIConstants;
import solo.pixelmonitor.ui.factories.LabelFactory;
import solo.pixelmonitor.ui.imageCompareScene.ImageComparisonMode;
import solo.pixelmonitor.ui.sceneManagement.WindowManager;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class responsible for handling the dialogue window responsible for import of screenshots from the "Image Compare" tool.
 */
@Slf4j
@Component
public class ImportImageHandler {
    private final WindowManager windowManager;
    private final SharedApplicationContext sharedApplicationContext;
    private final UUID sourceImageStaticID;

    public ImportImageHandler(WindowManager windowManager, SharedApplicationContext sharedApplicationContext) {
        this.sharedApplicationContext = sharedApplicationContext;
        this.windowManager = windowManager;
        this.sourceImageStaticID = UUID.randomUUID();
    }

    protected void importImageFromImageCompare(SelectButtonCallback buttonCallback) {
        List<BufferedImage> cachedScreenshots = sharedApplicationContext.getCachedScreenshots();
        if (cachedScreenshots.isEmpty()) {
            Alert noCachedScreenshotsAlert = new Alert(
                    Alert.AlertType.INFORMATION,
                    "No screenshots have been taken, use \"Image Compare\" tool to take screenshots, or upload them manually using this tool.");
            noCachedScreenshotsAlert.show();
            return;
        }

        BorderPane selectionBorder = new BorderPane();
        Stage selectionStage = new Stage();
        int sceneWidth = 1000;
        Scene selectionScene = new Scene(selectionBorder, sceneWidth, 600);
        selectionStage.setScene(selectionScene);
        if (!windowManager.addStage(sourceImageStaticID, selectionStage)) {
            // TODO: Consider adding alert here
            // TODO: also consider if we should allow a user to open a new panel, but deleting the previous one first
            log.warn("Attempted to open a new import image screen when one is already open.");
            return;
        }

        Node titleLabel = LabelFactory.createTitleLabelInGrid("Select Screenshot from Below");
        selectionBorder.setTop(titleLabel);
        GridPane contentGrid = new GridPane();
        contentGrid.setPadding(UIConstants.DEFAULT_UNIFORM_INSETS);
        contentGrid.setAlignment(Pos.TOP_CENTER);


        int numberOfScreenshots = cachedScreenshots.size();
        double imageWidth = (((double) sceneWidth / (double) numberOfScreenshots) - 20);
        Map<String, BufferedImage> nameImageMap = new LinkedHashMap<>();
        for (int i = 0; i < numberOfScreenshots; i++) {
            nameImageMap.put("Screenshot " + (i + 1), cachedScreenshots.get(i));
        }

        HBox screenshotBox = createImageSelectionBox(
                imageWidth,
                nameImageMap,
                buttonCallback
        );

        nameImageMap = new LinkedHashMap<>();
        for (Map.Entry<ImageComparisonMode, BufferedImage> entry : sharedApplicationContext.getCachedOutputImages().entrySet()) {
            nameImageMap.put("Output Image (" + entry.getKey() + ")", entry.getValue());
        }
        HBox outputImageBox = createImageSelectionBox(
                imageWidth,
                nameImageMap,
                buttonCallback
        );

        contentGrid.add(screenshotBox, 0, 0);
        contentGrid.add(outputImageBox, 0, 1);
        selectionBorder.setCenter(contentGrid);
    }

    private HBox createImageSelectionBox(
            double imageWidth,
            Map<String, BufferedImage> nameImageMap,
            SelectButtonCallback buttonCallback) {
        HBox hBox = new HBox();
        for (Map.Entry<String, BufferedImage> entry : nameImageMap.entrySet()) {
            String name = entry.getKey();
            BufferedImage image = entry.getValue();

            GridPane imageGrid = new GridPane();
            imageGrid.setAlignment(Pos.CENTER);
            Label imageLabel = new Label(name);
            ImageView imageView = new ImageView(SwingFXUtils.toFXImage(image, null));
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setFitWidth(imageWidth);

            Button selectButton = new Button("Select Image");
            selectButton.prefWidthProperty().bind(imageView.fitWidthProperty());
            selectButton.setOnAction(_ -> buttonCallback.onButtonClicked(image, sourceImageStaticID));

            imageGrid.add(imageLabel, 0, 0);
            imageGrid.add(imageView, 0, 1);
            imageGrid.add(selectButton, 0, 2);
            hBox.setSpacing(5);
            hBox.setPadding(UIConstants.DEFAULT_UNIFORM_INSETS);
            hBox.getChildren().add(imageGrid);
        }
        return hBox;
    }

    protected interface SelectButtonCallback {
        void onButtonClicked(BufferedImage image, UUID sceneStaticID);
    }
}
