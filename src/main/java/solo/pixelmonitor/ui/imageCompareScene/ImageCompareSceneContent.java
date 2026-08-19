package solo.pixelmonitor.ui.imageCompareScene;

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.common.SharedApplicationContext;
import solo.pixelmonitor.logic.imaging.ImageComparisonService;
import solo.pixelmonitor.logic.imaging.PixelReaderService;
import solo.pixelmonitor.ui.factories.GridFactory;
import solo.pixelmonitor.ui.factories.ImageFactory;
import solo.pixelmonitor.ui.factories.LabelFactory;
import solo.pixelmonitor.ui.factories.SpinnerFactory;
import solo.pixelmonitor.ui.sceneManagement.WindowManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Slf4j
@Component
public class ImageCompareSceneContent {
    private final PixelReaderService pixelReaderService;
    private final ImageComparisonService imageComparisonService;
    private final WindowManager windowManager;
    private final SharedApplicationContext sharedApplicationContext;
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
    public ImageCompareSceneContent(PixelReaderService pixelReaderService, ResourceLoader resourceLoader,
                                    ImageComparisonService imageComparisonService, WindowManager windowManager, SharedApplicationContext sharedApplicationContext) {
        this.pixelReaderService = pixelReaderService;
        numberOfMonitors = pixelReaderService.getNumberOfMonitors();
        this.resourceLoader = resourceLoader;
        monitorIndex = 1;
        // TODO: Let this be configurable
        imagePreviewWidth = 250;
        imagesToCompare = new ArrayList<>();
        outputImageMap = new HashMap<>();
        this.imageComparisonService = imageComparisonService;
        this.windowManager = windowManager;
        this.sharedApplicationContext = sharedApplicationContext;
    }

    private void initPlaceholderImage() throws IOException {
        Resource placeholderImageResource = resourceLoader.getResource("classpath:images/no_image_placeholder.png");
        try (InputStream inputStream = placeholderImageResource.getInputStream()) {
            placeholderImage = ImageIO.read(inputStream);
        }
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
            @SuppressWarnings("unchecked") ChoiceBox<Integer> src = (ChoiceBox<Integer>) event.getSource();
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
            @SuppressWarnings("unchecked") ChoiceBox<ImageComparisonMode> src = (ChoiceBox<ImageComparisonMode>) event.getSource();
            mode = src.getSelectionModel().getSelectedItem();
            updateScenes();
        });

        VBox combinedBox = new VBox();
        combinedBox.getChildren().addAll(nodeSelectionLabel, modeSelectionBox);
        return combinedBox;
    }

    private Node createNumberOfScenesSpinner() {
        Label numberOfScenesLabel = new Label("Number of Scenes");
        numberOfScenes = 2;
        Spinner<Integer> spinner = SpinnerFactory.getIntegerDefaultSpinner(2, 3, 2, 1);
        spinner.valueProperty().addListener((_, _, newValue) -> {
            numberOfScenes = newValue;
            updateScenes();
        });

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

    private Stage createShowOutputStage() {
        Stage outputStage = new Stage();
        BorderPane rootBorder = new BorderPane();
        BufferedImage outputImage = outputImageMap.get(mode);
        Scene scene = new Scene(rootBorder, 1280, 720);
        outputStage.setScene(scene);

        GridPane grid = new GridPane();
        ImageFactory.StaticImageOutput outputResult = ImageFactory.createStaticImageViewWithScrollPaneAndCoordinateLabel(outputImage);
        grid.add(outputResult.scrollPane(), 0, 0);
        rootBorder.setCenter(grid);
        rootBorder.setBottom(outputResult.coordinateLabel());
        return outputStage;
    }

    private Node createShowOutputButton() {
        Label spacingLabel = new Label();
        Button showOutputButton = new Button("Show Output Image");
        showOutputButton.getStyleClass().add("main-button");
        showOutputButton.setOnAction(_ -> windowManager.addStage(UUID.randomUUID(), createShowOutputStage()));
        VBox combinedBox = new VBox();
        combinedBox.getChildren().addAll(spacingLabel, showOutputButton);
        return combinedBox;
    }


    private Node createOptionsPane() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
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
            imageBox.setPadding(new Insets(0, 2, 0, 2));
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
        updateSharedApplicationContext();
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
        grid.setPadding(new Insets(0, 5, 0, 5));
        border.setTop(createOptionsPane());
        border.setCenter(grid);
        border.setLeft(createPreviewPane());
        //border.setRight(new Spacer(imagePreviewWidth));
        updateScenes();
        return border;
    }

    private void updateSharedApplicationContext() {
        sharedApplicationContext.setCachedScreenshots(imagesToCompare, numberOfScenes);
        sharedApplicationContext.setCachedOutputImages(outputImageMap);
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
