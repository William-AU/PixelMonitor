package solo.pixelmonitor.ui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import solo.pixelmonitor.PixelMonitorApplication;
import solo.pixelmonitor.ui.mainScene.MainScene;

public class JavaFXApplication extends Application {
    private ConfigurableApplicationContext applicationContext;



    @Override
    public void init() throws Exception {
        applicationContext = new SpringApplicationBuilder(PixelMonitorApplication.class).run();
    }

    @Override
    public void stop() throws Exception {
        applicationContext.close();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        //Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        MainScene mainScene = applicationContext.getBean(MainScene.class);
        mainScene.initialize(primaryStage);
    }
}
