package solo.pixelmonitor.ui.factories;


import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class ImageFactory {

    public static StaticImageOutput createStaticImageViewWithScrollPaneAndCoordinateLabel(BufferedImage image) {
        Image outputFXImage = SwingFXUtils.toFXImage(image, null);
        ImageView outputImageView = new ImageView(outputFXImage);
        outputImageView.setPreserveRatio(false);
        outputImageView.setSmooth(false);
        outputImageView.setCache(false);

        ScrollPane scrollPane = new ScrollPane(outputImageView);
        scrollPane.setPannable(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        Label coordinateLabel = new Label("");
        outputImageView.setOnMouseMoved(event -> {
            int pixelX = (int) event.getX();
            int pixelY = (int) event.getY();
            coordinateLabel.setText("(" + pixelX + "," + pixelY + ")");
        });

        return new StaticImageOutput(
                scrollPane,
                coordinateLabel,
                outputImageView
        );
    }

    /**
     * Simple record for returning a static scrollable image view of a {@link java.awt.image.BufferedImage}
     * @param scrollPane A scroll pane including an {@link javafx.scene.image.ImageView} with a specified {@link BufferedImage}
     * @param coordinateLabel A dynamic label with the coordinates of the mouse over the {@link javafx.scene.image.ImageView}
     * @param outputImageView The view linked to the {@link ScrollPane}
     */
    public record StaticImageOutput(
            ScrollPane scrollPane,
            Label coordinateLabel,
            ImageView outputImageView
    ){}
}
