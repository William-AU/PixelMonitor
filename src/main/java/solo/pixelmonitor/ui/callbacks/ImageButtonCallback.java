package solo.pixelmonitor.ui.callbacks;

import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * Generic button callback used for any scene that returns a new {@link BufferedImage} when the button is pressed.
 */
public interface ImageButtonCallback {
    /**
     * Button callback tied to a specific scene
     * @param selectedImage The selected image handled by this button (either by upload or creation)
     * @param sceneStaticID The ID associated with the scene that was opened by the button (if any). Used to close the
     *                      window again from the call site.
     */
    void onButtonClicked(BufferedImage selectedImage, UUID sceneStaticID);
}
