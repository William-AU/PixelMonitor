package solo.pixelmonitor.imaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import solo.pixelmonitor.logic.imaging.ImageManipulationService;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ImageManipulationService.class)
public class ImageManipulationServiceTests {

    @Autowired
    private ImageManipulationService imageManipulationService;

    /**
     * Creating the grayscale image should not change the dimensions of the image
     */
    @Test
    void turnGrayscaleReturnsImageWithSameDimensions() {
        BufferedImage original = new BufferedImage(5, 4, BufferedImage.TYPE_INT_ARGB);

        BufferedImage result = imageManipulationService.turnGrayscale(original, 8);

        assertNotNull(result);
        assertEquals(5, result.getWidth());
        assertEquals(4, result.getHeight());
    }

    /**
     * turnGrayscale should not allow inputs lower than 2
     */
    @Test
    void turnGrayscaleThrowsWhenNumberOfColorsLessThanTwo() {
        BufferedImage original = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        assertThrows(IllegalArgumentException.class, () -> imageManipulationService.turnGrayscale(original, 1));
        assertThrows(IllegalArgumentException.class, () -> imageManipulationService.turnGrayscale(original, 0));
    }

    /**
     * turnGrayscale should clamp values above 256 (maximum value for the grayscale band)
     */
    @Test
    void turnGrayscaleClampsNumberOfColorsGreaterThan256() {
        BufferedImage original = createGrayImage(1, 1, 128);

        BufferedImage result = imageManipulationService.turnGrayscale(original, 300);

        assertEquals(128, result.getRaster().getSample(0, 0, 0));
    }

    /**
     * With two input colors, we expect buckets:
     * {0 to 127} => {0}  and {128 to 256} => {255}
     * Therefore, we test the boundary pixels (127) and (128) are sorted into the correct buckets
     */
    @Test
    void turnGrayscaleQuantizesToTwoShadesCorrectly() {
        BufferedImage original = createGrayImage(4, 1, 0, 127, 128, 255);

        BufferedImage result = imageManipulationService.turnGrayscale(original, 2);

        int[] expected = {0, 0, 255, 255};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.getRaster().getSample(i, 0, 0));
        }
    }

    /**
     * Test that an image only output pixels from a bucket.
     * Each bucket is defined as 'bucket = (i * numberOfColors) / 256'.
     * The output value for each bucket is defined as 'grayValue = (bucket * 255) / (numberOfColors - 1)'.
     * This gives valid output values for noOfColors=8: {0, 36, 72, 109, 145, 182, 218, 255}.
     * Output colors need to be chosen in this way (instead of the naïve approach of 'value = (bucket * 255) / 8') to
     * ensure that both 0 and 255 are valid grayscale values, which is the expected behaviour.
     */
    @Test
    void turnGrayscaleProducesGrayscalePixelsWithinAllowedShades() {
        BufferedImage original = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        Color[] colors = {
                Color.RED, Color.GREEN, Color.BLUE,
                Color.WHITE, Color.BLACK, Color.YELLOW,
                Color.CYAN, Color.MAGENTA, new Color(123, 45, 67)
        };

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                original.setRGB(x, y, colors[y * 3 + x].getRGB());
            }
        }

        BufferedImage result = imageManipulationService.turnGrayscale(original, 8);

        Set<Integer> allowedShades = new HashSet<>(Arrays.asList(0, 36, 72, 109, 145, 182, 218, 255));
        assertEquals(1, result.getRaster().getNumBands());

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int gray = result.getRaster().getSample(x, y, 0);
                assertTrue(allowedShades.contains(gray), "Unexpected shade: " + gray);
            }
        }
    }

    private BufferedImage createGrayImage(int width, int height, int... grayValues) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.getRaster().setSample(x, y, 0, grayValues[index++]);
            }
        }
        return image;
    }
}