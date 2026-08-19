package solo.pixelmonitor.ui.configScene;

import jakarta.annotation.PostConstruct;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.common.SharedApplicationContext;
import solo.pixelmonitor.debug.DebugCSSHelper;
import solo.pixelmonitor.ui.factories.LabelFactory;
import solo.pixelmonitor.ui.sceneManagement.WindowManager;

import java.awt.*;

@Component
public class ConfigSceneContent {
    private final DebugCSSHelper debugCSSHelper;
    private final WindowManager windowManager;
    private boolean isDebug;
    @Getter
    private Node content;

    public ConfigSceneContent(DebugCSSHelper debugCSSHelper, SharedApplicationContext sharedApplicationContext, WindowManager windowManager) {
        this.isDebug = false;
        this.debugCSSHelper = debugCSSHelper;
        this.windowManager = windowManager;
    }

    private Node createTopPanel() {
        return LabelFactory.createTitleLabelInGrid("Configuration");
    }

    private void toggleDebugMode(Scene scene) {
        if (isDebug) {
            debugCSSHelper.detachDebugCss(scene);
        } else {
            debugCSSHelper.attachDebugCss(scene);
        }

        isDebug = !isDebug;
    }

    private Node createCenterPanel() {
        GridPane grid = new GridPane();
        Button debugButton = new Button("Toggle Debug Mode");
        debugButton.setOnAction(_ -> windowManager.getAllActiveScenes().forEach(this::toggleDebugMode));
        grid.add(debugButton, 0, 0);
        return grid;
    }

    @PostConstruct
    private void setupContent() {
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(createTopPanel());
        contentPane.setCenter(createCenterPanel());

        content = contentPane;
    }
}
