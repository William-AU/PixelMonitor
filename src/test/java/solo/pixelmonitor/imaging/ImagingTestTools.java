package solo.pixelmonitor.imaging;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImagingTestTools {
    protected static final int TRANSPARENT_PIXEL = 0x00000000;
    protected static final int BLACK_PIXEL = 0xFF000000;
    protected static final int RED_PIXEL = 0xFFFF0000 ;
    protected static final int GREEN_PIXEL = 0xFF00FF00;
    protected static final int ARBITRARY_PIXEL = 0xFF123456;

    protected static BufferedImage createSolidImage(int width, int height, int argb) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, argb);
            }
        }
        return img;
    }

    protected static void assertImagesEqual(BufferedImage expected, BufferedImage actual) {
        assertEquals(expected.getWidth(), actual.getWidth());
        assertEquals(expected.getHeight(), actual.getHeight());
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                assertEquals(expected.getRGB(x, y), actual.getRGB(x, y),
                        "Pixel mismatch at (" + x + "," + y + ")");
            }
        }
    }
}
