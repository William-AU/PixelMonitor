package solo.pixelmonitor.logic.imaging;

import org.springframework.stereotype.Service;
import solo.pixelmonitor.ui.imageCompareScene.ImageComparisonMode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

@Service
public class ImageComparisonService {
    private static final int TRANSPARENT_PIXEL = 0x00000000;
    private static final int BLACK_PIXEL = 0xFF000000;

    /**
     * Does a pixel-wise comparison of a list of images, using the given mode.
     * This comparison will show the pixel wise difference between the images. Showing either all the pixels that are
     * UNIQUE between the images, or the pixels which are SHARED between the images
     *
     * @param mode   The comparison mode (either showing unique pixels, or shared pixels)
     * @param images Any number of buffered images to compare.
     * @return The resulting image after the comparison, it will always be the same size as the largest image.
     */
    public BufferedImage compareImages(ImageComparisonMode mode, List<BufferedImage> images) {
        return switch (mode) {
            case SAME_PIXELS -> compareImagesSamePixels(images);
            case UNIQUE_PIXELS -> compareImagesUniquePixels(images);
        };
    }

    private BufferedImage findLargestImage(List<BufferedImage> images) {
        if (images.isEmpty()) {
            throw new IllegalArgumentException("Cannot find largest image without any images.");
        }
        BufferedImage result = null;
        int size = Integer.MIN_VALUE;
        for (BufferedImage image : images) {
            int newSize = image.getHeight() * image.getWidth();
            if (newSize > size) {
                result = image;
                size = newSize;
            }
        }
        assert result != null;
        return result;
    }

    /**
     * {@link BufferedImage} does not guarantee bounds checking, and we wish to return an "empty" pixel if one is not
     * found in a map
     */
    private int getRGBFromImageWithBoundsCheck(BufferedImage image, int x, int y) {
        if (x >= image.getWidth() || y >= image.getHeight()) {
            return TRANSPARENT_PIXEL;
        }
        return image.getRGB(x, y);
    }

    private boolean isPixelIdenticalAcrossImages(List<BufferedImage> images, int x, int y) {
        int firstPixel = getRGBFromImageWithBoundsCheck(images.getFirst(), x, y);
        for (int i = 1; i < images.size(); i++) {
            if (getRGBFromImageWithBoundsCheck(images.get(i), x, y) != firstPixel) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllPixelsDistinctAtCoordinate(List<BufferedImage> images, int x, int y) {
        for (int i = 0; i < images.size(); i++) {
            int pixelI = getRGBFromImageWithBoundsCheck(images.get(i), x, y);
            for (int j = i + 1; j < images.size(); j++) {
                if (pixelI == getRGBFromImageWithBoundsCheck(images.get(j), x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private BufferedImage compareImagesSamePixels(List<BufferedImage> images) {
        BufferedImage largest = findLargestImage(images);
        int width = largest.getWidth();
        int height = largest.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isPixelIdenticalAcrossImages(images, x, y)) {
                    result.setRGB(x, y, getRGBFromImageWithBoundsCheck(images.getFirst(), x, y));
                } else {
                    result.setRGB(x, y, TRANSPARENT_PIXEL);
                }
            }
        }
        return result;
    }

    private BufferedImage compareImagesUniquePixels(List<BufferedImage> images) {
        BufferedImage largest = findLargestImage(images);
        int width = largest.getWidth();
        int height = largest.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (areAllPixelsDistinctAtCoordinate(images, x, y)) {
                    result.setRGB(x, y, BLACK_PIXEL);
                } else {
                    result.setRGB(x, y, TRANSPARENT_PIXEL);
                }
            }
        }
        return result;
    }
}
