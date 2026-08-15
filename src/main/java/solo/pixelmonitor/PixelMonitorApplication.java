package solo.pixelmonitor;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import solo.pixelmonitor.ui.JavaFXApplication;

@EnableScheduling
@SpringBootApplication
public class PixelMonitorApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        Application.launch(JavaFXApplication.class, args);
    }

}
