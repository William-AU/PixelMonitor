package solo.pixelmonitor.logic.imaging.pictureInPicture;

import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class PictureInPictureService {
    /**
     * Attempts to find the given image in the source image.
     * @param targetImage The image to find
     * @param srcImage The image to search in
     * @return The resulting found image, as well as the position of the found image in the source image
     */
    public PictureInPictureResult findPictureExact(BufferedImage targetImage, BufferedImage srcImage) {
        // TODO: Implement
        return new PictureInPictureResult(
                srcImage,
                targetImage,
                targetImage,
                true,
                0,
                0,
                targetImage.getWidth(),
                targetImage.getHeight()
        );
    }
}
