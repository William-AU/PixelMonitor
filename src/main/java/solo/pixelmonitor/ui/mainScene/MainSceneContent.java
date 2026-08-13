package solo.pixelmonitor.ui.mainScene;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class MainSceneContentFactory {
    public static Node node() {
        // Create the inner BorderPane that will hold the main content structure
        BorderPane contentPane = new BorderPane();

        // Example: a toolbar at the top of the content area
        HBox contentToolbar = new HBox(10);
        contentToolbar.setPadding(new Insets(5));
        contentToolbar.getChildren().addAll(
                new Button("Refresh"),
                new Button("Export")
        );
        contentPane.setTop(contentToolbar);

        // Example: a status bar at the bottom
        Label statusLabel = new Label("Status: Ready");
        contentPane.setBottom(statusLabel);

        // Example: a central content node
        StackPane centralArea = new StackPane(new Label("Main Content Area"));
        contentPane.setCenter(centralArea);

        // Optional: add left/right panels as needed
        // contentPane.setLeft(...);
        // contentPane.setRight(...);

        return contentPane;
    }
}
