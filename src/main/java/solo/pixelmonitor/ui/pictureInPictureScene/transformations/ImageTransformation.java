package solo.pixelmonitor.ui.pictureInPictureScene.transformations;

import java.awt.image.BufferedImage;

public interface ImageTransformation {
    BufferedImage transform(BufferedImage inputImage);
}
