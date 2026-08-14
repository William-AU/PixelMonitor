package solo.pixelmonitor.logic.listeners;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class GlobalMouseHook implements NativeMouseMotionListener {
    private final List<MouseMoveListener> mouseMoveListeners;

    @Autowired
    public GlobalMouseHook(List<MouseMoveListener> mouseMoveListeners) {
        this.mouseMoveListeners = mouseMoveListeners;
    }

    @PostConstruct
    public void start() throws NativeHookException {
        if (!GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.registerNativeHook();
        }
        GlobalScreen.addNativeMouseMotionListener(this);
    }

    @PreDestroy
    public void stop() {
        if (GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.removeNativeMouseMotionListener(this);
        }
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
        int x = nativeEvent.getX();
        int y = nativeEvent.getY();
        mouseMoveListeners.forEach(listener -> listener.onMouseMoved(x, y));
    }
}
