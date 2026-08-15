package solo.pixelmonitor.ui.navigation;

import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.ui.sceneManagement.ApplicationScene;
import solo.pixelmonitor.ui.listeners.SceneChangeListener;
import solo.pixelmonitor.ui.sceneManagement.SceneManager;

import java.util.HashMap;
import java.util.Map;

@Component
public class NavigationPanel implements SceneChangeListener {

    private final Map<ApplicationScene, Button> applicationSceneButtonMap;
    private final SceneManager sceneManager;
    private HBox navigationPanelHBox;

    public NavigationPanel(@Lazy SceneManager sceneManager) {
        applicationSceneButtonMap = initApplicationSceneButtonMap();
        this.sceneManager = sceneManager;
    }

    private Map<ApplicationScene, Button> initApplicationSceneButtonMap() {
       Map<ApplicationScene, Button> result = new HashMap<>();
       Button mainSceneButton = new Button("Pixel Viewer");
       mainSceneButton.setDisable(true);
       Button imageCompareButton = new Button("Image Compare");
       Button configButton = new Button("Configuration");
       result.put(ApplicationScene.PIXEL_VIEWER_SCENE, mainSceneButton);
       result.put(ApplicationScene.IMAGE_COMPARE_SCENE, imageCompareButton);
       result.put(ApplicationScene.CONFIG_SCENE, configButton);
       result.forEach((scene, btn) -> btn.setOnAction(_ -> sceneManager.changeScene(scene)));
       return result;
    }

    @Override
    public void onSceneChange(ApplicationScene newScene) {
        applicationSceneButtonMap.forEach((key, btn) -> btn.setDisable(key.equals(newScene)));
    }

    @PostConstruct
    private void setup() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);

        Button mainSceneButton = applicationSceneButtonMap.get(ApplicationScene.PIXEL_VIEWER_SCENE);
        Button imageCompareButton = applicationSceneButtonMap.get(ApplicationScene.IMAGE_COMPARE_SCENE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button configButton = applicationSceneButtonMap.get(ApplicationScene.CONFIG_SCENE);

        hbox.getChildren().addAll(mainSceneButton, imageCompareButton);
        hbox.getChildren().add(spacer);
        hbox.getChildren().add(configButton);
        this.navigationPanelHBox = hbox;
    }

    public HBox navigationPanel() {
        return this.navigationPanelHBox;
    }
}
