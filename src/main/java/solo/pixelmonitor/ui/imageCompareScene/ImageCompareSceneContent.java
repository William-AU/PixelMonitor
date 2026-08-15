package solo.pixelmonitor.ui.imageCompareScene;

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.logic.imaging.ImageComparisonService;
import solo.pixelmonitor.logic.imaging.PixelReaderService;
import solo.pixelmonitor.ui.factories.GridFactory;
import solo.pixelmonitor.ui.factories.LabelFactory;
import solo.pixelmonitor.ui.factories.SpinnerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class ImageCompareSceneContent {
    private final PixelReaderService pixelReaderService;
    private final ImageComparisonService imageComparisonService;
    @Getter
    private Node content;
    private final int numberOfMonitors;
    private int monitorIndex;
    private ImageView previewImageView;
    private final int imagePreviewWidth;
    private ImageComparisonMode mode;
    private int numberOfScenes;
    private GridPane outputPane;
    private List<BufferedImage> imagesToCompare;
    private Map<ImageComparisonMode, BufferedImage> outputImageMap;
    private BufferedImage placeholderImage;
    private final ResourceLoader resourceLoader;

    @Autowired
    public ImageCompareSceneContent(PixelReaderService pixelReaderService, ResourceLoader resourceLoader, ImageComparisonService imageComparisonService) {
        this.pixelReaderService = pixelReaderService;
        numberOfMonitors = pixelReaderService.getNumberOfMonitors();
        this.resourceLoader = resourceLoader;
        monitorIndex = 1;
        // TODO: Let this be configurable
        imagePreviewWidth = 250;
        imagesToCompare = new ArrayList<>();
        outputImageMap = new HashMap<>();
        this.imageComparisonService = imageComparisonService;
    }

    private void initPlaceholderImage() throws IOException {
        Resource placeholderImageResource = resourceLoader.getResource("classpath:images/no_image_placeholder.png");
        File placeholderImageFile = placeholderImageResource.getFile();
        placeholderImage = ImageIO.read(placeholderImageFile);
    }

    private Node createTopBar() {
        return LabelFactory.createTitleLabelInGrid("Image Compare");
    }

    private Node createTakeScreenshotButton() {
        Label paddingLabel = new Label("");
        Button button = new Button("Take Screenshot");
        button.setOnAction(_ -> {
            BufferedImage screenshot = pixelReaderService.takeScreenshot(monitorIndex);
            imagesToCompare.add(screenshot);
            updateScenes();
        });
        VBox combinedBox = new VBox();
        combinedBox.getChildren().addAll(paddingLabel, button);
        return combinedBox;
    }

    private Node createMonitorSelectionBox() {
        Label monitorSelectionLabel = new Label("Select Monitor");
        ChoiceBox<Integer> monitorSelectionBox = new ChoiceBox<>();
        for (int i = 0; i < numberOfMonitors; i++) {
            monitorSelectionBox.getItems().add(i);
        }
        monitorSelectionBox.getSelectionModel().select(monitorIndex);
        monitorSelectionBox.setOnAction((event) -> {
            ChoiceBox<Integer> src = (ChoiceBox<Integer>) event.getSource();
            monitorIndex = src.getSelectionModel().getSelectedItem();
        });

        VBox combinedBox = new VBox();
        combinedBox.getChildren().addAll(monitorSelectionLabel, monitorSelectionBox);
        return combinedBox;
    }

    private Node createModeSelectionBox() {
        Label nodeSelectionLabel = new Label("Comparison Mode");
        ChoiceBox<ImageComparisonMode> modeSelectionBox = new ChoiceBox<>();
        modeSelectionBox.getItems().addAll(
                ImageComparisonMode.SAME_PIXELS,
                ImageComparisonMode.UNIQUE_PIXELS
        );
        modeSelectionBox.getSelectionModel().selectFirst();
        mode = ImageComparisonMode.SAME_PIXELS;
        modeSelectionBox.setOnAction(event -> {
            ChoiceBox<ImageComparisonMode> src = (ChoiceBox<ImageComparisonMode>) event.getSource();
            mode = src.getSelectionModel().getSelectedItem();
            updateScenes();
        });

        VBox combinedBox = new VBox();
        combinedBox.getChildren().addAll(nodeSelectionLabel, modeSelectionBox);
        return combinedBox;
    }

    private Node createNumberOfScenesSpinner() {
        Label numberOfScenesLabel = new Label("Number of Scenes");
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setEditable(true);
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 3, 2, 1);
        spinner.setValueFactory(valueFactory);
        numberOfScenes = 2;
        spinner.valueProperty().addListener((_, _, newValue) -> {
            numberOfScenes = newValue;
            updateScenes();
        });
        spinner.getEditor().setTextFormatter(SpinnerFactory.getIntegerOnlyFormatter());
        spinner.getEditor().addEventFilter(ScrollEvent.SCROLL, SpinnerFactory.createSpinnerScrollHandler(spinner));

        VBox combinedBox = new VBox();
        combinedBox.getChildren().addAll(numberOfScenesLabel, spinner);
        return combinedBox;
    }

    private Node createClearImagesButton() {
        Label spacingLabel = new Label();
        Button clearImagesButton = new Button("Clear Images");
        clearImagesButton.setOnAction(_ -> {
            imagesToCompare = new ArrayList<>();
            outputImageMap = new HashMap<>();
            updateScenes();
        });
        VBox combinedBox = new VBox();
        combinedBox.getChildren().addAll(spacingLabel, clearImagesButton);
        return combinedBox;
    }

    private Node createShowOutputButton() {
        Label spacingLabel = new Label();
        Button showOutputButton = new Button("Show Output Image");
        // TODO: Add functionality to button
        VBox combinedBox = new VBox();
        combinedBox.getChildren().addAll(spacingLabel, showOutputButton);
        return combinedBox;
    }


    private Node createOptionsPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.add(createTakeScreenshotButton(), 0, 0);
        grid.add(createMonitorSelectionBox(), 1, 0);
        grid.add(createModeSelectionBox(), 2, 0);
        grid.add(createNumberOfScenesSpinner(), 3, 0);
        grid.add(createClearImagesButton(), 4, 0);
        grid.add(createShowOutputButton(), 5, 0);
        return grid;
    }

    private void updateOutputImage() {
        if (imagesToCompare.size() <= 1) {
            return;
        }
        List<BufferedImage> truncatedList = new ArrayList<>();
        int bound = Math.min(imagesToCompare.size(), numberOfScenes);
        for (int i = 0; i < bound; i++) {
            truncatedList.add(imagesToCompare.get(i));
        }
        outputImageMap.put(mode, imageComparisonService.compareImages(mode, truncatedList));
    }

    private void updateScenes() {
        updateOutputImage();
        outputPane.getChildren().clear();
        for (int i = 0; i < numberOfScenes; i++) {
            VBox imageBox = new VBox();
            Label imageLabel = new Label("Image " + (i + 1));
            BufferedImage bufferedImage;
            if (imagesToCompare.size() > i) {
                bufferedImage = imagesToCompare.get(i);
            } else {
                bufferedImage = placeholderImage;
            }
            ImageView image = new ImageView(SwingFXUtils.toFXImage(bufferedImage, null));
            image.fitWidthProperty().bind(outputPane.widthProperty().multiply(1.0 / (numberOfScenes + 1)));
            image.setPreserveRatio(true);
            image.setSmooth(true);
            imageBox.getChildren().addAll(imageLabel, image);
            // TODO: Consider using an auto sizing grid here
            outputPane.add(imageBox, i, 0);
        }
        VBox outputBox = new VBox();
        Label outputLabel = new Label("Output Image");
        outputBox.getChildren().add(outputLabel);
        ImageView outputImage;
        if (outputImageMap.containsKey(mode)) {
            outputImage = new ImageView(SwingFXUtils.toFXImage(outputImageMap.get(mode), null));
        } else {
            outputImage = new ImageView(SwingFXUtils.toFXImage(placeholderImage, null));
        }
        outputImage.fitWidthProperty().bind(outputPane.widthProperty().multiply(1.0 / (numberOfScenes + 1)));
        outputImage.setPreserveRatio(true);
        outputImage.setSmooth(true);
        outputBox.getChildren().add(outputImage);
        outputPane.add(outputBox, numberOfScenes, 0);
    }

    private Node createOutputPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        // TODO: Temporary
        grid.setMinHeight(400);
        grid.setMinWidth(800);

        outputPane = grid;
        return grid;
    }

    private Node createPreviewPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        VBox previewBox = new VBox();
        Label previewLabel = new Label("Screenshot Preview");
        previewImageView = new ImageView(SwingFXUtils.toFXImage(pixelReaderService.takeScreenshot(monitorIndex), null));
        previewImageView.setFitWidth(imagePreviewWidth);
        previewImageView.setPreserveRatio(true);
        previewImageView.setSmooth(true);

        previewBox.getChildren().addAll(previewLabel, previewImageView);

        grid.add(previewBox, 0, 0);

        return grid;
    }

    private Node createCenterContent() {
        BorderPane border = new BorderPane();
        GridPane grid = new GridPane();
        grid.getColumnConstraints().add(GridFactory.getCenteredColumnConstraint());
        grid.add(createOutputPane(), 0, 0);
        border.setTop(createOptionsPane());
        border.setCenter(grid);
        border.setLeft(createPreviewPane());
        //border.setRight(new Spacer(imagePreviewWidth));
        updateScenes();
        return border;
    }



    @Scheduled(fixedDelay = 1000)
    private void refreshPreview() {
        if (content == null || content.getScene() == null) {
            return;
        }
        BufferedImage screenShot = pixelReaderService.takeScreenshot(monitorIndex);
        Image previewImage = SwingFXUtils.toFXImage(screenShot, null);
        Platform.runLater(() -> previewImageView.setImage(previewImage));
    }

    @PostConstruct
    private void setupContent() throws IOException {
        initPlaceholderImage();
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createTopBar());
        borderPane.setCenter(createCenterContent());

        content = borderPane;
    }
}
