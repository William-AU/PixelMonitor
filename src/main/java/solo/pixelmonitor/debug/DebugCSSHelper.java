package solo.pixelmonitor.debug;

import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class DebugCSSHelper {
    private final Resource debugCssResource;

    public DebugCSSHelper(ResourceLoader resourceLoader) {
        debugCssResource = resourceLoader.getResource("classpath:styles/debug.css");
    }

    /**
     * Adds the debug CSS file to the scene if it is found on the classpath.
     *
     * @param scene the JavaFX scene to which the stylesheet should be added
     */
    public void attachDebugCss(Scene scene) {
        try {
            String cssUrl = debugCssResource.getURL().toExternalForm();
            scene.getStylesheets().add(cssUrl);
            log.debug("Debug CSS attached: {}", cssUrl);
        } catch (IOException e) {
            log.warn("Could not load debug.css from classpath", e);
        }
    }

    /**
     * Removes the debug CSS from the scene if it was added.
     *
     * @param scene the JavaFX scene from which the stylesheet should be removed
     */
    public void detachDebugCss(Scene scene) {
        try {
            String cssUrl = debugCssResource.getURL().toExternalForm();
            scene.getStylesheets().remove(cssUrl);
            log.debug("Debug CSS detached: {}", cssUrl);
        } catch (IOException e) {
            log.warn("Could not locate debug.css to remove", e);
        }
    }
}
