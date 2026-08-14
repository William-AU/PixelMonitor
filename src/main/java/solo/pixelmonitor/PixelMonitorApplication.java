package solo.pixelmonitor;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import solo.pixelmonitor.ui.JavaFXApplication;

@SpringBootApplication
public class PixelMonitorApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        Application.launch(JavaFXApplication.class, args);
    }

}
