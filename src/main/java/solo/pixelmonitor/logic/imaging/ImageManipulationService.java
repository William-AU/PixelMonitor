package solo.pixelmonitor.logic.imaging;

import org.springframework.stereotype.Service;

import java.awt.color.ColorSpace;
import java.awt.image.*;

/**
 * Service for generic image manipulation, for example turning images into grayscale.
 */
@Service
public class ImageManipulationService {
    /**
     * Creates a grayscale copy of the original image, each pixel is sorted into a bucket of size 255/numberOfColors.
     * @param original The original image to turn into grayscale
     * @param numberOfColors The number of colors (shades) allowed in the output image
     * @return The new grayscale image
     */
    public BufferedImage turnGrayscale(BufferedImage original, int numberOfColors) {
        if (numberOfColors < 2) {
            throw new IllegalArgumentException("numberOfColors must be at least 2");
        }
        if (numberOfColors > 256) {
            numberOfColors = 256;
        }

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp colorConvertOp = new ColorConvertOp(cs, null);
        BufferedImage grayImage = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        colorConvertOp.filter(original, grayImage);

        byte[] lookup = new byte[256];
        for (int i = 0; i < 256; i++) {
            int bucket = (i * numberOfColors) / 256;
            int grayValue = (bucket * 255) / (numberOfColors - 1);
            lookup[i] = (byte) grayValue;
        }


        ByteLookupTable lookupTable = new ByteLookupTable(0, lookup);
        LookupOp lookupOp = new LookupOp(lookupTable, null);

        return lookupOp.filter(grayImage, null);
    }
}
