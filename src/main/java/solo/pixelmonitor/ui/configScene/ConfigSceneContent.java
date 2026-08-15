package solo.pixelmonitor.ui.configScene;

import jakarta.annotation.PostConstruct;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import lombok.Getter;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.ui.factories.LabelFactory;

@Component
public class ConfigSceneContent {
    @Getter
    private Node content;

    private Node createTopPanel() {
        return LabelFactory.createTitleLabelInGrid("Configuration");
    }

   private Node createCenterPanel() {
        return new HBox();
   }

    @PostConstruct
    private void setupContent() {
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(createTopPanel());
        contentPane.setCenter(createCenterPanel());

        content = contentPane;
    }
}
