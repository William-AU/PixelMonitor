package solo.pixelmonitor.logic.listeners;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeLibraryLocator;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Component
public class GlobalMouseHook implements NativeMouseMotionListener {

    private final List<MouseMoveListener> mouseMoveListeners;
    private Path extractedLib;

    @Autowired
    public GlobalMouseHook(List<MouseMoveListener> mouseMoveListeners) {
        this.mouseMoveListeners = mouseMoveListeners;
    }

    @PostConstruct
    public void start() throws Exception {
        extractNativeLibrary();
        GlobalScreen.registerNativeHook();
    }

    @PreDestroy
    public void stop() throws Exception {
        GlobalScreen.unregisterNativeHook();
        if (extractedLib != null) {
            Files.deleteIfExists(extractedLib);
        }
    }

    /**
     * Extract the correct native library for the current platform and
     * configure JNativeHook to use it via a custom locator.
     */
    private void extractNativeLibrary() throws Exception {
        String libName = getNativeLibraryName();
        String resourcePath = "/com/github/kwhat/jnativehook/lib/" + getOsFolder() + "/" + libName;

        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Native library not found: " + resourcePath);
            }
            Path tempFile = Files.createTempFile("jnativehook-", libName);
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            extractedLib = tempFile;

            System.setProperty("jnativehook.lib.path", tempFile.toAbsolutePath().toString());
            System.setProperty("jnativehook.lib.locator", TempFileLibraryLocator.class.getName());
        }
    }

    private String getNativeLibraryName() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "JNativeHook.dll";
        } else if (os.contains("mac")) {
            return "libJNativeHook.dylib";
        } else {
            return "libJNativeHook.so";
        }
    }

    private String getOsFolder() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        String osDir;
        if (os.contains("win")) {
            osDir = "windows";
        } else if (os.contains("mac")) {
            osDir = "mac";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            osDir = "linux";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        String archDir;
        if (arch.contains("64") || arch.contains("amd64") || arch.contains("x86_64")) {
            archDir = "x86_64";
        } else if (arch.contains("arm64") || arch.contains("aarch64")) {
            archDir = "aarch64";
        } else if (arch.contains("x86") || arch.contains("i386")) {
            archDir = "x86";
        } else {
            throw new UnsupportedOperationException("Unsupported architecture: " + arch);
        }

        return osDir + "/" + archDir;
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
        int x = nativeEvent.getX();
        int y = nativeEvent.getY();
        mouseMoveListeners.forEach(listener -> listener.onMouseMoved(x, y));
    }

    /**
     * Custom locator that returns the extracted native library file.
     * The path is taken from the system property "jnativehook.lib.path".
     */
    public static class TempFileLibraryLocator implements NativeLibraryLocator {
        @Override
        public Iterator<File> getLibraries() {
            String path = System.getProperty("jnativehook.lib.path");
            if (path != null) {
                File file = new File(path);
                if (file.exists() && file.isFile() && file.canRead()) {
                    return List.of(file).iterator();
                }
            }
            return Collections.emptyIterator();
        }
    }
}
