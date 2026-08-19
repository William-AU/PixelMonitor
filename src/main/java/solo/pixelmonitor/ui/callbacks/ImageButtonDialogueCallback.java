package solo.pixelmonitor.ui.callbacks;

import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * As {@link ImageButtonCallback} but intended for buttons that spawn dialogues, where the output {@link java.awt.image.BufferedImage}
 * is only returned after the dialogue is closed
 */
public interface ImageButtonDialogueCallback {
    void onDialogueClosed(BufferedImage selectedImage, UUID sceneID);
}
