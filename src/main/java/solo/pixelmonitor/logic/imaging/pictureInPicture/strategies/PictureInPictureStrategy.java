package solo.pixelmonitor.logic.imaging.pictureInPicture.strategies;

import solo.pixelmonitor.logic.imaging.pictureInPicture.PictureInPictureResult;

import java.awt.image.BufferedImage;

public interface PictureInPictureStrategy {
    PictureInPictureResult findPicture(BufferedImage targetImage, BufferedImage srcImage);
}
