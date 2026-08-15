package solo.pixelmonitor.ui.factories;

import javafx.event.EventHandler;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.ScrollEvent;

public class SpinnerFactory {
    public static TextFormatter<Integer> getIntegerOnlyFormatter() {
       return new TextFormatter<>(
               change -> {
                   String newText = change.getControlNewText();
                   if (newText.matches("\\d*")) {
                       return change;
                   }
                   return null;
               }
       );
    }

    public static EventHandler<ScrollEvent> createSpinnerScrollHandler(Spinner<Integer> spinner) {
        return event -> {
            if (event.getDeltaY() > 0) {
                spinner.increment();
            } else if (event.getDeltaY() < 0) {
                spinner.decrement();
            }
            event.consume();
        };
    }
}
