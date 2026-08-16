package solo.pixelmonitor.common;

import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import solo.pixelmonitor.ui.imageCompareScene.ImageComparisonMode;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context used by any application component to cleanly expose information that might be relevant for other components
 */
@Component
public class SharedApplicationContext {
    @Getter
    private List<BufferedImage> cachedScreenshots;
    @Getter @Setter
    private Map<ImageComparisonMode, BufferedImage> cachedOutputImages;
    @Getter @Setter
    private Stage primaryStage;

    public SharedApplicationContext() {
        cachedScreenshots = new ArrayList<>();
        cachedOutputImages = new HashMap<>();
    }

    public void setCachedScreenshots(List<BufferedImage> rawImages, int maximumImages) {
        List<BufferedImage> newImages = new ArrayList<>();
        int amountToCopy = Math.min(rawImages.size(), maximumImages);
        if (amountToCopy == 0) {
            return;
        }
        for (int i = 0; i < amountToCopy; i++) {
            newImages.add(rawImages.get(i));
        }
        cachedScreenshots = newImages;
    }
}
