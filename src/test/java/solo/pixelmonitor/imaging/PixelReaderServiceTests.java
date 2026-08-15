package solo.pixelmonitor.imaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import solo.pixelmonitor.logic.imaging.PixelGrid;
import solo.pixelmonitor.logic.imaging.PixelReaderService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static solo.pixelmonitor.imaging.ImagingTestTools.*;

@SpringBootTest(classes = PixelReaderService.class)
public class PixelReaderServiceTests {
    @MockitoBean
    private Robot robot;

    @Autowired
    private PixelReaderService service;


    /**
     * Verify that the service gets the correct points with a given radius. There is not much to actually test here
     * as the underlying service hands over most of the selection logic to {@link Robot#createScreenCapture(Rectangle)}.
     * We know that the service defines the radius as {@code size = 2 * radius + 1}, and as long as we do the same here,
     * we won't actually catch any meaningful issues. Therefore, all we can do is to ensure that the method does not
     * do anything destructive with the data it gets from {@link Robot#createScreenCapture(Rectangle)}, and that
     * {@link Robot#createScreenCapture(Rectangle)} recieves the expected {@link Rectangle}.
     */
    @Test
    void getPixelsAroundPointReturnsCorrectGrid() {
        int radius = 1;
        int size = 2 * radius + 1;
        BufferedImage capture = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        int[][] argbValues = {
                {RED_PIXEL, GREEN_PIXEL, RED_PIXEL},
                {BLACK_PIXEL, ARBITRARY_PIXEL, RED_PIXEL},
                {BLACK_PIXEL, ARBITRARY_PIXEL, GREEN_PIXEL}
        };
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                capture.setRGB(col, row, argbValues[row][col]);
            }
        }

        when(robot.createScreenCapture(any(Rectangle.class))).thenReturn(capture);

        Point center = new Point(5, 5);
        PixelGrid grid = service.getPixelsAroundPoint(center, radius);

        assertNotNull(grid);
        assertEquals(size, grid.getRows());
        assertEquals(size, grid.getColumns());

        Map<Integer, List<Color>> rowColors = grid.getRowColors();
        assertEquals(size, rowColors.size());

        for (int row = 0; row < size; row++) {
            List<Color> pixels = rowColors.get(row);
            assertEquals(size, pixels.size());
            for (int col = 0; col < size; col++) {
                Color expected = new Color(argbValues[row][col], true);
                assertEquals(expected, pixels.get(col), "Pixel mismatch at row " + row + ", col " + col);
            }
        }
        // Ensure the correct rectangle was actually sent to the robot
        Rectangle expectedRect = new Rectangle(
                center.x - radius,
                center.y - radius,
                size,
                size
        );
        verify(robot).createScreenCapture(expectedRect);
    }

    @Test
    void getPixelsAroundPointWithNegativeRadiusThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getPixelsAroundPoint(new Point(0, 0), -1));
    }
}
