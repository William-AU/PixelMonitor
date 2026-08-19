package solo.pixelmonitor.ui.pictureInPictureScene;

import jakarta.annotation.PostConstruct;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Pos;


import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.common.UIConstants;
import solo.pixelmonitor.logic.imaging.PixelReaderService;
import solo.pixelmonitor.logic.imaging.utils.BufferedImageUtility;
import solo.pixelmonitor.ui.factories.LabelFactory;
import solo.pixelmonitor.ui.pictureInPictureScene.options.GrayscaleOptionsHandler;
import solo.pixelmonitor.ui.pictureInPictureScene.options.ImageOptionsContext;
import solo.pixelmonitor.ui.pictureInPictureScene.options.ImageTransformationOrderContext;
import solo.pixelmonitor.ui.pictureInPictureScene.transformations.ImageTransformation;
import solo.pixelmonitor.ui.pictureInPictureScene.transformations.ImageTransformationType;
import solo.pixelmonitor.ui.sceneManagement.WindowManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

@Slf4j
@Component
public class PictureInPictureSceneContent {
    private final PixelReaderService pixelReaderService;
    private final WindowManager windowManager;
    private final ImportImageHandler importImageHandler;
    private final FindTargetImageHandler findTargetImageHandler;
    private final GrayscaleOptionsHandler grayscaleOptionsHandler;
    @Getter
    private Node content;
    private BufferedImage placeholderImage;
    private BufferedImage dropFilesHereImage;
    private BufferedImage targetImage;
    private BufferedImage sourceImage;
    private final ResourceLoader resourceLoader;
    private ImageView toFindPictureImageView;
    private ImageView sourceImageView;
    private GridPane uploadImageGrid;
    private final ImageOptionsContext imageOptionsContext;
    private final ImageTransformationOrderContext targetImageTransformationContext;
    private final ImageTransformationOrderContext sourceImageTransformationContext;


    @Autowired
    public PictureInPictureSceneContent(ResourceLoader resourceLoader, PixelReaderService pixelReaderService,
                                        WindowManager windowManager, ImportImageHandler importImageHandler,
                                        FindTargetImageHandler findTargetImageHandler, GrayscaleOptionsHandler grayscaleOptionsHandler) {
        this.resourceLoader = resourceLoader;
        this.pixelReaderService = pixelReaderService;
        this.windowManager = windowManager;
        this.importImageHandler = importImageHandler;
        this.findTargetImageHandler = findTargetImageHandler;
        this.grayscaleOptionsHandler = grayscaleOptionsHandler;

        // TODO: From config
        imageOptionsContext = new ImageOptionsContext();
        targetImageTransformationContext = new ImageTransformationOrderContext();
        sourceImageTransformationContext = new ImageTransformationOrderContext();
    }

    // TODO: Refactor (code duplication with ImageCompareScene)
    private void initPlaceholderImage() throws IOException {
        Resource placeholderImageResource = resourceLoader.getResource("classpath:images/no_image_placeholder.png");
        try (InputStream inputStream = placeholderImageResource.getInputStream()) {
            placeholderImage = ImageIO.read(inputStream);
        }
    }

    // TODO: Also handle resource loading in a single place
    private void initDropFilesHereImage() throws IOException {
        Resource dropFilesHereResource = resourceLoader.getResource("classpath:images/drop_files_here.png");
        try (InputStream inputStream = dropFilesHereResource.getInputStream()) {
            dropFilesHereImage = ImageIO.read(inputStream);
        }
    }

    private Node createTopPanel() {
        return LabelFactory.createTitleLabelInGrid("Picture in Picture");
    }

    private Node createToFindPicturePreview() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Picture to Find (Preview)");
        BufferedImage image = targetImage == null ? placeholderImage : targetImage;
        toFindPictureImageView = new ImageView(SwingFXUtils.toFXImage(image, null));
        toFindPictureImageView.setPreserveRatio(true);
        toFindPictureImageView.setSmooth(false);

        // TODO: Get number from config
        toFindPictureImageView.setFitWidth(250);
        grid.add(titleLabel, 0, 0);
        grid.add(toFindPictureImageView, 0, 1);

        GridPane.setHalignment(titleLabel, HPos.CENTER);
        return grid;
    }

    private void createAndShowUnsupportedFileTypeAlert(File chosenFile) {
        Alert unsupportedFileTypeAlert = new Alert(
                Alert.AlertType.ERROR,
                "Unable to convert " + chosenFile + " to an image, unsupported file type."
        );
        unsupportedFileTypeAlert.show();
    }

    private boolean loadSourceImageFromFile(File file) {
        try {
            sourceImage = ImageIO.read(file);
            BufferedImage unmodifiedSourceImage = ImageIO.read(file);
            sourceImageTransformationContext.setOriginalImage(unmodifiedSourceImage);
            updateSourceImage();
            if (sourceImage != null) {
                return true;
            }
            createAndShowUnsupportedFileTypeAlert(file);
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean loadToFindImageFromFile(File file) {
        try {
            targetImage = ImageIO.read(file);
            BufferedImage unmodifiedTargetImage = ImageIO.read(file);
            targetImageTransformationContext.setOriginalImage(unmodifiedTargetImage);
            updateTargetImage();
            if (targetImage != null) {
                return true;
            }
            createAndShowUnsupportedFileTypeAlert(file);
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private Node createImageUploadNode() {
        uploadImageGrid = new GridPane();
        uploadImageGrid.setAlignment(Pos.TOP_CENTER);
        uploadImageGrid.setPickOnBounds(true);
        ImageView dropFilesHereImageView = new ImageView(SwingFXUtils.toFXImage(dropFilesHereImage, null));
        dropFilesHereImageView.setPreserveRatio(true);
        dropFilesHereImageView.setSmooth(true);
        // TODO: Config...
        dropFilesHereImageView.setFitWidth(250);
        uploadImageGrid.add(dropFilesHereImageView, 0, 0);

        uploadImageGrid.setOnDragOver(event -> {
            if (event.getGestureSource() != uploadImageGrid && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        uploadImageGrid.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().getFirst();
                success = loadToFindImageFromFile(file);
            }
            event.setDropCompleted(success);
            event.consume();
        });

        Button chooseFileButton = createChooseFileButton(dropFilesHereImageView, this::loadToFindImageFromFile);
        uploadImageGrid.add(chooseFileButton, 0, 1);
        return uploadImageGrid;
    }

    private File showImageFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );

        File chosenFile = fileChooser.showOpenDialog(new Stage());
        log.info("Uploaded file: {}", chosenFile);
        return chosenFile;
    }

    private @NonNull Button createChooseFileButton(
            ImageView dropFilesImageView,
            Consumer<File> fileProcessor) {

        Button chooseFileButton = new Button("Or Choose File");
        chooseFileButton.prefWidthProperty().bind(dropFilesImageView.fitWidthProperty());

        chooseFileButton.setOnAction(_ -> {
            File chosenFile = showImageFileChooser();
            if (chosenFile != null) {
                fileProcessor.accept(chosenFile);
            }
        });

        return chooseFileButton;
    }

    private void updateTargetImage() {
        BufferedImage imageToShow = placeholderImage;
        if (!targetImageTransformationContext.isEmpty()) {
            imageToShow = targetImageTransformationContext.applyAllTransformations();
        }
        if (targetImageTransformationContext.isEmpty() && targetImage != null) {
            imageToShow = targetImage;
        }

        toFindPictureImageView.setImage(SwingFXUtils.toFXImage(imageToShow, null));
    }

    private void updateSourceImage() {
        BufferedImage imageToShow = placeholderImage;
        if (!sourceImageTransformationContext.isEmpty()) {
            imageToShow = sourceImageTransformationContext.applyAllTransformations();
        }
        if (sourceImageTransformationContext.isEmpty() && sourceImage != null) {
            imageToShow = sourceImage;
        }

        sourceImageView.setImage(SwingFXUtils.toFXImage(imageToShow, null));
    }

    private Button createClearPictureButton() {
        Button button = new Button("Clear Pictures");
        button.setOnAction(_ -> {
            targetImage = null;
            sourceImage = null;
            targetImageTransformationContext.setOriginalImage(null);
            sourceImageTransformationContext.setOriginalImage(null);
            updateTargetImage();
            updateSourceImage();
        });
        return button;
    }

    private void addTransformation(ImageTransformationType type, ImageTransformation transformation) {
        targetImageTransformationContext.addTransformation(type, transformation);
        sourceImageTransformationContext.addTransformation(type, transformation);
        updateTargetImage();
        updateSourceImage();
    }

    private Node createOptionsColumn() {
        Node centeredTitle = LabelFactory.createCenteredLabel("Image Options");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setVgap(UIConstants.DEFAULT_GRID_V_GAP);
        grid.setHgap(UIConstants.DEFAULT_GRID_H_GAP);
        grid.add(centeredTitle, 0, 0);

        Button clearPictureButton = createClearPictureButton();
        GridPane.setHalignment(clearPictureButton, HPos.CENTER);
        grid.add(clearPictureButton, 0, 1);
        grid.add(grayscaleOptionsHandler.createGrayscaleOptions(
                ((imageTransformation, sceneStaticID) -> {
                    addTransformation(ImageTransformationType.GRAYSCALE, imageTransformation);
                    windowManager.closeStage(sceneStaticID);
                }),
                (imageTransformation) -> addTransformation(ImageTransformationType.GRAYSCALE, imageTransformation),
                imageOptionsContext
        ), 0, 2);
        return grid;
    }

    private Node createSourceImageScreenShotButton(ObservableValue<? extends Number> observableValue) {
        Button takeScreenshotButton = new Button("Take Screenshot");
        takeScreenshotButton.setOnAction(_ -> {
            BufferedImage screenshot = pixelReaderService.takeScreenshotUsingPreviousMonitorIndex();
            BufferedImage copy = BufferedImageUtility.copyImage(screenshot);
            sourceImage = screenshot;
            sourceImageTransformationContext.setOriginalImage(copy);
            updateSourceImage();
        });
        takeScreenshotButton.prefWidthProperty().bind(observableValue);
        return takeScreenshotButton;
    }

    private Node createImportFromImageCompareButton(ObservableValue<? extends Number> observableValue) {
        Button importFromImageCompareButton = new Button("Import from Image Compare");
        importFromImageCompareButton.setOnAction(_ -> importImageHandler.importImageFromImageCompare((image, sceneStaticID) -> {
            sourceImage = image;
            BufferedImage copy = BufferedImageUtility.copyImage(image);
            sourceImageTransformationContext.setOriginalImage(copy);
            windowManager.closeStage(sceneStaticID);
            updateSourceImage();
        }));
        importFromImageCompareButton.prefWidthProperty().bind(observableValue);
        return importFromImageCompareButton;
    }

    private GridPane createSourceImageDropAndSelectionNode() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        ImageView dropFilesHereImageView = new ImageView(SwingFXUtils.toFXImage(dropFilesHereImage, null));
        dropFilesHereImageView.setPreserveRatio(true);
        dropFilesHereImageView.setSmooth(true);
        // TODO: also config
        dropFilesHereImageView.setFitWidth(250);

        grid.setOnDragOver(event -> {
            if (event.getGestureSource() != uploadImageGrid && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        grid.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().getFirst();
                success = loadSourceImageFromFile(file);
            }
            event.setDropCompleted(success);
            event.consume();
        });

        grid.add(dropFilesHereImageView, 0, 0);

        Button selectionButton = createChooseFileButton(dropFilesHereImageView, this::loadSourceImageFromFile);

        grid.add(selectionButton, 0, 1);
        return grid;
    }

    private Node createSelectedSourceImagePreview() {
        Label titleLabel = new Label("Source Image (Preview)");
        BufferedImage image = sourceImage == null ? placeholderImage : sourceImage;
        sourceImageView = new ImageView(SwingFXUtils.toFXImage(image, null));
        updateSourceImage();
        sourceImageView.setPreserveRatio(true);
        sourceImageView.setSmooth(true);
        // TODO: Config...
        sourceImageView.setFitWidth(250);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.add(titleLabel, 0, 0);
        grid.add(sourceImageView, 0, 1);

        GridPane.setHalignment(titleLabel, HPos.CENTER);
        return grid;
    }

    private Node createSourceImageSelectionColumn() {
        Node centeredTitle = LabelFactory.createCenteredLabel("Select Source Image");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setVgap(UIConstants.DEFAULT_GRID_V_GAP);
        grid.add(centeredTitle, 0, 0);
        GridPane sourceImageDropAndSelectionNode = createSourceImageDropAndSelectionNode();
        grid.add(createSelectedSourceImagePreview(), 0, 1);
        grid.add(sourceImageDropAndSelectionNode, 0, 2);
        grid.add(createSourceImageScreenShotButton(
                sourceImageDropAndSelectionNode.widthProperty()), 0, 3);
        grid.add(createImportFromImageCompareButton(
                sourceImageDropAndSelectionNode.widthProperty()), 0, 4);


        return grid;
    }

    private Node createFindPictureButton() {
        Button findPictureButton = new Button("Find Target in Source");
        findPictureButton.getStyleClass().add("main-button");
        findPictureButton.prefWidthProperty().bind(toFindPictureImageView.fitWidthProperty());
        findPictureButton.setOnAction(_ -> findTargetImageHandler.handleFindPictureInSourceButtonClicked(
                targetImage, sourceImage
        ));
        return findPictureButton;
    }

    private Node createToFindPreviewColumn() {
        Node centeredTitle = LabelFactory.createCenteredLabel("Select Target Image");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setVgap(UIConstants.DEFAULT_GRID_H_GAP);
        grid.add(centeredTitle, 0, 0);
        grid.add(createToFindPicturePreview(), 0, 1);
        grid.add(createImageUploadNode(), 0, 2);
        grid.add(createFindPictureButton(), 0, 3);
        return grid;
    }

    private Node createCenterPanel() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        // TODO: Config... again
        grid.setHgap(UIConstants.DEFAULT_GRID_H_GAP);
        grid.setPadding(UIConstants.DEFAULT_UNIFORM_INSETS);


        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 3);
            grid.getColumnConstraints().add(column);
        }

        grid.add(createToFindPreviewColumn(), 0, 0);
        grid.add(createSourceImageSelectionColumn(), 1, 0);
        grid.add(createOptionsColumn(), 2, 0);
        return grid;
    }

    private Node createContent() {
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createTopPanel());
        borderPane.setCenter(createCenterPanel());
        return borderPane;
    }

    @PostConstruct
    private void setupContent() throws IOException {
        initPlaceholderImage();
        initDropFilesHereImage();
        content = createContent();
    }
}
