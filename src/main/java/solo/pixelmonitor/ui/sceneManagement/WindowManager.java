package solo.pixelmonitor.ui.sceneManagement;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import solo.pixelmonitor.common.SharedApplicationContext;
import solo.pixelmonitor.ui.listeners.ApplicationClosedListener;

import java.util.*;

@Slf4j
@Service
public class WindowManager implements ApplicationClosedListener {
    private final SharedApplicationContext sharedApplicationContext;
    Map<UUID, Stage> additionalStages;

    public WindowManager(SharedApplicationContext sharedApplicationContext) {
        additionalStages = new HashMap<>();
        this.sharedApplicationContext = sharedApplicationContext;
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

    /**
     * Closes a stage if it has not already been closed.
     * We explicitly allow the callee to pre-emptively close the stage (this also accounts for the user closing the window)
     */
    public void closeStage(UUID uuid) {
        if (!additionalStages.containsKey(uuid)) {
            return;
        }
        Stage stageToClose = additionalStages.get(uuid);
        additionalStages.remove(uuid);
        stageToClose.close();
    }

    public List<Scene> getAllActiveScenes() {
        List<Scene> result = new ArrayList<>();
        result.add(sharedApplicationContext.getPrimaryStage().getScene());
        additionalStages.values().forEach(stage -> result.add(stage.getScene()));
        return result;
    }
}
