package solo.pixelmonitor.ui.mainScene;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import solo.pixelmonitor.ui.navigation.NavigationPanel;

@Service
public class MainScene {
    private final MainSceneContent mainSceneContent;
    private final NavigationPanel navigationPanel;

    @Autowired
    public MainScene(MainSceneContent mainSceneContent, NavigationPanel navigationPanel) {
        this.mainSceneContent = mainSceneContent;
        this.navigationPanel = navigationPanel;
    }

    public void initialize(Stage primaryStage) {
        BorderPane border = new BorderPane();
        HBox topPanel = navigationPanel.navigationPanel();
        border.setTop(topPanel);
        Node content = mainSceneContent.getContent();
        border.setCenter(content);

        primaryStage.setScene(new Scene(border, 800, 600));
        primaryStage.show();
    }
}
