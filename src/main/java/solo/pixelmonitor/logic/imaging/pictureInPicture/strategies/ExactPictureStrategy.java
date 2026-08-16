package solo.pixelmonitor.logic.imaging.pictureInPicture.strategies;

import solo.pixelmonitor.logic.imaging.pictureInPicture.PictureInPictureResult;

import java.awt.image.BufferedImage;

public class ExactPictureStrategy implements PictureInPictureStrategy {
    @Override
    public PictureInPictureResult findPicture(BufferedImage targetImage, BufferedImage srcImage) {
        int targetWidth = targetImage.getWidth();
        int targetHeight = targetImage.getHeight();
        int firstPixel = targetImage.getRGB(0, 0);

        for (int y = 0; y <= srcImage.getHeight() - targetHeight; y++) {
            for (int x = 0; x <= srcImage.getWidth() - targetWidth; x++) {

                if (srcImage.getRGB(x, y) != firstPixel) {
                    continue;
                }

                boolean fullMatch = true;

                outer:
                for (int row = 0; row < targetHeight; row++) {
                    for (int col = 0; col < targetWidth; col++) {
                        int sourcePixel = srcImage.getRGB(x + col, y + row);
                        int targetPixel = targetImage.getRGB(col, row);

                        if (sourcePixel != targetPixel) {
                            fullMatch = false;
                            break outer;
                        }
                    }
                }

                if (fullMatch) {
                    return new PictureInPictureResult(
                            srcImage,
                            targetImage,
                            null,
                            true,
                            x,
                            y,
                            targetWidth,
                            targetHeight
                    );
                }
            }
        }

        return new PictureInPictureResult(
                srcImage,
                targetImage,
                null,
                false,
                0,
                0,
                targetWidth,
                targetHeight
        );
    }
}
