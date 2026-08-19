package solo.pixelmonitor.ui.pictureInPictureScene.options;

import lombok.Data;

@Data
public class ImageOptionsContext {
    private boolean enabled;
    private int numberOfGrayscaleColors;

    public ImageOptionsContext() {
        enabled = false;
        numberOfGrayscaleColors = 2;
    }
}
