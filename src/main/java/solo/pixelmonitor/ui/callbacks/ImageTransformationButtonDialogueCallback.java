package solo.pixelmonitor.ui.callbacks;

import solo.pixelmonitor.ui.pictureInPictureScene.transformations.ImageTransformation;

import java.util.UUID;

/**
 * As {@link ImageButtonCallback} but intended for buttons that spawn dialogues, where the output {@link java.awt.image.BufferedImage}
 * is only returned after the dialogue is closed
 */
public interface ImageTransformationButtonDialogueCallback {
    /**
     * Called when the dialogue window is closed, although we expect the scene calling this callback to close the scene
     * itself, we nonetheless give the scene ID for the callee to call {@link solo.pixelmonitor.ui.sceneManagement.SceneManager}.
     * This is so that the scene manager can do any further clean-up needed.
     * @param imageTransformation The transformation that needs to be applied to the image to achieve the desired output image
     * @param sceneID The unique ID of the scene which created the image
     */
    void onDialogueClosed(ImageTransformation imageTransformation, UUID sceneID);
}
