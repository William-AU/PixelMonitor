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

    /**
     * Tracks a new stage to ensure it is closed correctly when the application is closed, as well as keeping all open
     * stages in a single location.
     * @param uuid The desired id of the stage for later retrieval
     * @param stage The stage to be tracked
     * @return True if stage was added successfully, false if a stage with that ID is already tracked
     */
    public boolean addStage(UUID uuid, Stage stage) {
        if (additionalStages.containsKey(uuid)) {
            return false;
        }
        additionalStages.put(uuid, stage);
        stage.setOnHiding(_ -> additionalStages.remove(uuid));
        stage.show();
        return true;
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

    public void closeStage(UUID uuid) {
        if (!additionalStages.containsKey(uuid)) {
            return;
        }
        Stage stageToClose = additionalStages.get(uuid);
        additionalStages.remove(uuid);
        stageToClose.close();
    }
}
