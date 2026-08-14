package solo.pixelmonitor.config;

import org.springframework.context.annotation.Bean;

import java.awt.*;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    public Robot robot() throws AWTException {
        return new Robot();
    }
}
