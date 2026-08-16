package solo.pixelmonitor.logic.imaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

@Slf4j
@Service
public class PixelReaderService {
    private final Robot robot;
    private int previouslyUsedMonitorIndex;
    public PixelReaderService(Robot robot) {
        this.robot = robot;
        previouslyUsedMonitorIndex = 0;
    }

    /**
     * Get the current global mouse position, as defined by AWT
     */
    public Point getGlobalMousePosition() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        if (pointerInfo == null) {
            throw new IllegalStateException("Mouse pointer not available");
        }
        return pointerInfo.getLocation();
    }

    public Point getMonitorRelativeMousePosition() {
        Point globalPoint = getGlobalMousePosition();
        GraphicsDevice monitor = getMonitorAt(globalPoint);
        if (monitor == null) {
            throw new IllegalStateException("Mouse is not on any known screen");
        }

        Rectangle monitorBounds = monitor.getDefaultConfiguration().getBounds();
        return new Point(globalPoint.x - monitorBounds.x, globalPoint.y - monitorBounds.y);
    }

    /**
     * Gets a grid of pixels in radius around the given point.
     * The given grid will always be perfectly square, and we consider a single pixel to have radius = 0
     * For example, a grid with radius = 1 would be:
     *      X   X   X
     *      X   X   X
     *      X   X   X
     */
    public PixelGrid getPixelsAroundPoint(Point globalPoint, int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }

        int size = radius * 2 + 1;
        Rectangle captureRect = new Rectangle(
                globalPoint.x - radius,
                globalPoint.y - radius,
                size,
                size
        );

        BufferedImage screenCapture = robot.createScreenCapture(captureRect);

        LinkedHashMap<Integer, List<Color>> rowColors = new LinkedHashMap<>();
        for (int row = 0; row < size; row++) {
            List<Color> rowPixels = new ArrayList<>(size);
            for (int col = 0; col < size; col++) {
                int argb = screenCapture.getRGB(col, row);
                rowPixels.add(new Color(argb, true));
            }
            rowColors.put(row, rowPixels);
        }

        return new PixelGrid(rowColors, size);
    }

    private Color getPixelColor(int x, int y) {
       return robot.getPixelColor(x, y);
    }

    public Color getPixelColor(Point globalPoint) {
        return getPixelColor(globalPoint.x, globalPoint.y);
    }

    private GraphicsDevice getMonitorAt(Point globalPoint) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            if (bounds.contains(globalPoint)) {
                return gd;
            }
        }
        return null;
    }

    public int getNumberOfMonitors() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
    }

    public BufferedImage takeScreenshotUsingPreviousMonitorIndex() {
        return takeScreenshot(previouslyUsedMonitorIndex);
    }

    public BufferedImage takeScreenshot(int monitorIndex) {
        previouslyUsedMonitorIndex = monitorIndex;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            if (monitorIndex < 0 || monitorIndex >= screens.length) {
                System.err.println("Monitor index " + monitorIndex + " does not exist.");
                return null;
            }

            GraphicsDevice monitor = screens[monitorIndex];
            Rectangle bounds = monitor.getDefaultConfiguration().getBounds();

            return robot.createScreenCapture(bounds);
        } catch (java.awt.HeadlessException e) {
            log.error("Headless environment not expected", e);
            return null;
        }
    }
}
