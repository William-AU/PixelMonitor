package solo.pixelmonitor.ui.pictureInPictureScene.options;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import solo.pixelmonitor.ui.pictureInPictureScene.transformations.ImageTransformation;
import solo.pixelmonitor.ui.pictureInPictureScene.transformations.ImageTransformationType;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context which keeps track of the order to apply image transformations.
 * This order is customizable by the user, as the image transformations are lossy, this ordering can matter.
 */
@Slf4j
public class ImageTransformationOrderContext {
    @Setter
    private BufferedImage originalImage;
    private final Map<ImageTransformationType, ImageTransformation> imageTransformations;
    @Setter @Getter
    private List<ImageTransformationType> orderedImageTransformations;

    public ImageTransformationOrderContext() {
        this.imageTransformations = new HashMap<>();
        this.orderedImageTransformations = new ArrayList<>();
    }

    public void addTransformation(ImageTransformationType transformationType, ImageTransformation transformation) {
        if (!orderedImageTransformations.contains(transformationType)) {
            orderedImageTransformations.add(transformationType);
        }
        imageTransformations.put(transformationType, transformation);
    }

    public boolean isEmpty() {
        if (originalImage == null) {
            return true;
        }
        return orderedImageTransformations.isEmpty();
    }

    public BufferedImage applyAllTransformations() {
        BufferedImage resultingImage = originalImage;
        for (ImageTransformationType transformationType : orderedImageTransformations) {
            ImageTransformation transformation = imageTransformations.get(transformationType);
            resultingImage = transformation.transform(resultingImage);
        }
        return resultingImage;
    }
}
