package solo.pixelmonitor.ui.navigation;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class NavigationPanelFactory {
    public static HBox navigationPanel() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);

        Button mainSceneButton = new Button("Main");
        mainSceneButton.setDisable(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button configButton = new Button("Configuration");

        hbox.getChildren().addAll(mainSceneButton);
        hbox.getChildren().add(spacer);
        hbox.getChildren().add(configButton);
        return hbox;
    }
}
