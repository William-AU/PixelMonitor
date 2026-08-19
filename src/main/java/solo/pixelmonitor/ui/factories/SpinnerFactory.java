package solo.pixelmonitor.ui.factories;

import javafx.event.EventHandler;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.ScrollEvent;

public class SpinnerFactory {
    public static Spinner<Integer> getIntegerDefaultSpinner(int min, int max, int initialValue, int step) {
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setEditable(true);
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue, step);
        spinner.setValueFactory(valueFactory);
        spinner.getEditor().setTextFormatter(getIntegerOnlyFormatter());
        spinner.getEditor().addEventFilter(ScrollEvent.SCROLL, createSpinnerScrollHandler(spinner));

        return spinner;
    }

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
