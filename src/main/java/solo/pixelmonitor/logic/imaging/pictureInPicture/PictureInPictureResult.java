package solo.pixelmonitor.logic.imaging.pictureInPicture;

import java.awt.image.BufferedImage;

public record PictureInPictureResult(
        BufferedImage original,
        BufferedImage targetImage,
        BufferedImage sourceImageWithTarget,
        boolean imageFound,
        int x,
        int y,
        int foundWidth,
        int foundHeight) {

}
