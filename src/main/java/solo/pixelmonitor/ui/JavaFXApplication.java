package solo.pixelmonitor.ui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import solo.pixelmonitor.PixelMonitorApplication;
import solo.pixelmonitor.ui.sceneManagement.SceneManager;

public class JavaFXApplication extends Application {
    private ConfigurableApplicationContext applicationContext;



    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(PixelMonitorApplication.class).run();
    }

    @Override
    public void stop() {
        applicationContext.close();
    }

    @Override
    public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        //Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        SceneManager sceneManager = applicationContext.getBean(SceneManager.class);
        sceneManager.initialize(primaryStage);
    }
}
