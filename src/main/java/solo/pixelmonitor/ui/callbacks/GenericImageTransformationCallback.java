package solo.pixelmonitor.ui.callbacks;

import solo.pixelmonitor.ui.pictureInPictureScene.transformations.ImageTransformation;

/**
 * A simple callback for methods intended to create {@link solo.pixelmonitor.ui.pictureInPictureScene.transformations.ImageTransformation}.
 */
public interface GenericImageTransformationCallback {
    void onImageTransformChanged(ImageTransformation newTransform);
}
