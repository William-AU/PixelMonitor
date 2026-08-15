package solo.pixelmonitor.logic.imaging;

import lombok.Getter;
import org.springframework.stereotype.Service;
import solo.pixelmonitor.ui.imageCompareScene.ImageComparisonMode;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageComparisonService {
    /**
     * Does a pixel-wise comparison of a list of images, using the given mode.
     * This comparison will show the pixel wise difference between the images. Showing either all of the pixels that are
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
        if (x > image.getWidth() || y > image.getHeight()) {
            return toARGB(0, 0, 0, 0);
        }
        return image.getRGB(x, y);
    }

    private ComparisonImage createGenericComparisonImage(List<BufferedImage> images) {
        BufferedImage largestImage = findLargestImage(images);
        ComparisonImage result = new ComparisonImage(largestImage.getWidth(), largestImage.getHeight());
        int width = largestImage.getWidth();
        int height = largestImage.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (BufferedImage image : images) {
                    result.addPixel(new ComparisonCoordinate(x, y), getRGBFromImageWithBoundsCheck(image, x, y));
                }
            }
        }
        return result;
    }

    private BufferedImage createCombinedImage(Map<ComparisonCoordinate, Integer> pixelMap, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        pixelMap.forEach((coordinate, pixel) -> result.setRGB(coordinate.x, coordinate.y, pixel));
        return result;
    }

    private BufferedImage compareImagesSamePixels(List<BufferedImage> images) {
        ComparisonImage comparisonImage = createGenericComparisonImage(images);
        return createCombinedImage(
                comparisonImage.applyOperator(pixel -> {
                    List<Integer> pixels = pixel.rgbPixels();
                    if (pixels.isEmpty()) {
                        return toARGB(0, 0, 0, 0);
                    }

                    int firstPixel = pixels.getFirst();
                    for (Integer p : pixels) {
                        if (!p.equals(firstPixel)) {
                            return toARGB(0, 0, 0, 0);
                        }
                    }
                    return firstPixel;
                }),
                comparisonImage.getWidth(),
                comparisonImage.getHeight());
    }

    private BufferedImage compareImagesUniquePixels(List<BufferedImage> images) {
        ComparisonImage comparisonImage = createGenericComparisonImage(images);
        return createCombinedImage(
                comparisonImage.applyOperator(pixel -> {
                    List<Integer> pixels = pixel.rgbPixels();
                    if (pixels.isEmpty()) {
                        return toARGB(0, 0, 0, 0);
                    }

                    int firstPixel = pixels.getFirst();
                    for (Integer p : pixels) {
                        if (!p.equals(firstPixel)) {
                            return toARGB(255, 0, 0, 0);
                        }
                    }
                    return toARGB(0, 0, 0, 0);
                }),
                comparisonImage.getWidth(),
                comparisonImage.getHeight());
    }

    private static int toARGB(int alpha, int r, int g, int b) {
        return (alpha & 0xFF) << 24
                | (r & 0xFF) << 16
                | (g & 0xFF) << 8
                | (b & 0xFF);
    }

    private record ComparisonPixel(List<Integer> rgbPixels) {
    }

    private record ComparisonCoordinate(int x, int y) {
    }

    private interface ComparisonOperation {
        int comparePixels(ComparisonPixel pixel);
    }

    private static class ComparisonImage {
        private final Map<ComparisonCoordinate, ComparisonPixel> pixelMap;
        @Getter
        private final int width;
        @Getter
        private final int height;

        public ComparisonImage(int width, int height) {
            this.width = width;
            this.height = height;
            pixelMap = new HashMap<>();
        }

        public void addPixel(ComparisonCoordinate coordinate, Integer pixel) {
            if (!pixelMap.containsKey(coordinate)) {
                pixelMap.put(coordinate, new ComparisonPixel(new ArrayList<>()));
            }
            pixelMap.get(coordinate).rgbPixels.add(pixel);
        }

        /**
         * Applies an operator to the underlying pixelmap, without mutating the underlying map representation.
         * The operator applied must collapse a pixel representation (list of pixels) into a single pixel following
         * the single integer RGB representation of {@link BufferedImage}
         *
         * @param op The operator that should be applied to the entire map
         * @return The new map containing single pixel values.
         */
        public Map<ComparisonCoordinate, Integer> applyOperator(ComparisonOperation op) {
            Map<ComparisonCoordinate, Integer> resultMap = new HashMap<>();
            pixelMap.forEach((coordinate, pixel) -> resultMap.put(coordinate, op.comparePixels(pixel)));
            return resultMap;
        }
    }
}
