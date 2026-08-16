package solo.pixelmonitor.ui.sceneManagement;

import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import solo.pixelmonitor.ui.listeners.ApplicationClosedListener;

import java.util.*;

@Slf4j
@Service
public class WindowManager implements ApplicationClosedListener {
    Map<UUID, Stage> additionalStages;

    public WindowManager() {
        additionalStages = new HashMap<>();
    }

    public void addStage(UUID uuid, Stage stage) {
        additionalStages.put(uuid, stage);
        stage.setOnHiding(_ -> additionalStages.remove(uuid));
        stage.show();
        log.info("Current stages:");
        log.info(additionalStages.toString());
    }

    @Override
    public void onApplicationClose() {
        List<Stage> stagesToClose = new ArrayList<>(additionalStages.values());
        additionalStages.clear();

        stagesToClose.forEach(stage -> {
            try {
                if (stage != null && stage.isShowing()) {
                    stage.close();
                }
            } catch (Exception e) {
                log.error("Error closing additional stage", e);
            }
        });
    }
}
