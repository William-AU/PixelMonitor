package solo.pixelmonitor.ui.sceneManagement;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import solo.pixelmonitor.common.SharedApplicationContext;
import solo.pixelmonitor.ui.configScene.ConfigSceneContent;
import solo.pixelmonitor.ui.imageCompareScene.ImageCompareSceneContent;
import solo.pixelmonitor.ui.listeners.ApplicationClosedListener;
import solo.pixelmonitor.ui.listeners.SceneChangeListener;
import solo.pixelmonitor.ui.pictureInPictureScene.PictureInPictureSceneContent;
import solo.pixelmonitor.ui.pixelViewerScene.PixelViewerSceneContent;
import solo.pixelmonitor.ui.navigation.NavigationPanel;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class SceneManager {
    private final NavigationPanel navigationPanel;
    private final List<SceneChangeListener> sceneChangeListeners;
    private final List<ApplicationClosedListener> applicationClosedListeners;
    private final PixelViewerSceneContent pixelViewerSceneContent;
    private final ConfigSceneContent configSceneContent;
    private final ImageCompareSceneContent imageCompareSceneContent;
    private final PictureInPictureSceneContent pictureInPictureSceneContent;
    private final SharedApplicationContext sharedApplicationContext;
    private final ResourceLoader resourceLoader;
    private Stage primaryStage;
    private BorderPane contentBorder;
    private Scene scene;


    @Autowired
    public SceneManager(NavigationPanel navigationPanel, List<SceneChangeListener> sceneChangeListeners,
                        List<ApplicationClosedListener> applicationClosedListeners,
                        PixelViewerSceneContent pixelViewerSceneContent, ConfigSceneContent configSceneContent,
                        ImageCompareSceneContent imageCompareSceneContent,
                        PictureInPictureSceneContent pictureInPictureSceneContent,
                        SharedApplicationContext sharedApplicationContext, ResourceLoader resourceLoader) {
        this.navigationPanel = navigationPanel;
        this.sceneChangeListeners = sceneChangeListeners;
        this.applicationClosedListeners = applicationClosedListeners;
        this.pixelViewerSceneContent = pixelViewerSceneContent;
        this.configSceneContent = configSceneContent;
        this.imageCompareSceneContent = imageCompareSceneContent;
        this.pictureInPictureSceneContent = pictureInPictureSceneContent;
        this.sharedApplicationContext = sharedApplicationContext;
        this.resourceLoader = resourceLoader;
    }

    public void changeScene(ApplicationScene newScene) {
        Node content = switch (newScene) {
            case PIXEL_VIEWER_SCENE -> pixelViewerSceneContent.getContent();
            case CONFIG_SCENE -> configSceneContent.getContent();
            case IMAGE_COMPARE_SCENE -> imageCompareSceneContent.getContent();
            case PICTURE_IN_PICTURE_SCENE -> pictureInPictureSceneContent.getContent();
            case null, default -> throw new IllegalStateException("Unknown scene: " + newScene);
        };
        contentBorder.setCenter(content);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnHiding(_ -> applicationClosedListeners.forEach(ApplicationClosedListener::onApplicationClose));
        primaryStage.setOnCloseRequest(_ -> {
            Platform.exit();
            System.exit(0);
        });
        sceneChangeListeners.forEach(sceneChangeListener -> sceneChangeListener.onSceneChange(newScene));
    }


    public void initialize(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        sharedApplicationContext.setPrimaryStage(primaryStage);
        contentBorder = new BorderPane();
        HBox topPanel = navigationPanel.navigationPanel();
        contentBorder.setTop(topPanel);
        scene = new Scene(contentBorder, 1280, 720);
        Resource baseCSS = resourceLoader.getResource("classpath:styles/base.css");
        scene.getStylesheets().add(baseCSS.getURL().toExternalForm());
        changeScene(ApplicationScene.PIXEL_VIEWER_SCENE);
    }
}
