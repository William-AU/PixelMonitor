package solo.pixelmonitor.logic.imaging.pictureInPicture;

import org.springframework.stereotype.Service;
import solo.pixelmonitor.logic.imaging.pictureInPicture.strategies.ExactPictureStrategy;
import solo.pixelmonitor.logic.imaging.pictureInPicture.strategies.PictureInPictureStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class PictureInPictureService {
    private BufferedImage drawTargetOnSourceImage(BufferedImage target, BufferedImage src, Coordinate startCoordinate) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics2D g = result.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(3));
        g.drawRect(startCoordinate.x(), startCoordinate.y(), target.getWidth(), target.getHeight());
        g.dispose();
        return result;
    }

    /**
     * Attempts to find the given image in the source image.
     *
     * @param targetImage The image to find
     * @param srcImage    The image to search in
     * @return The resulting found image, as well as the position of the found image in the source image
     */
    public PictureInPictureResult findPictureExact(BufferedImage targetImage, BufferedImage srcImage) {
        return findPictureUsingStrategy(targetImage, srcImage, new ExactPictureStrategy());
    }

    private PictureInPictureResult findPictureUsingStrategy(BufferedImage targetImage, BufferedImage srcImage, PictureInPictureStrategy strategy) {
        PictureInPictureResult initialResult = strategy.findPicture(targetImage, srcImage);
        if (!initialResult.imageFound()) {
            return initialResult;
        }
        return new PictureInPictureResult(
                initialResult.original(),
                initialResult.targetImage(),
                drawTargetOnSourceImage(initialResult.targetImage(), initialResult.original(), new Coordinate(initialResult.x(), initialResult.y())),
                true,
                initialResult.x(),
                initialResult.y(),
                initialResult.foundWidth(),
                initialResult.foundHeight()
        );
    }


    private record Coordinate(int x, int y) {
    }
}
