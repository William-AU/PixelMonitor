package solo.pixelmonitor.imaging;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import solo.pixelmonitor.logic.imaging.ImageComparisonService;
import solo.pixelmonitor.ui.imageCompareScene.ImageComparisonMode;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static solo.pixelmonitor.imaging.ImagingTestTools.*;

@SpringBootTest(classes = ImageComparisonService.class)
public class ImageComparisonServiceTests {


    @Autowired
    private ImageComparisonService service;

    /**
     * The service explicitly does not support empty images, the caller is responsible for verifying this
     */
    @Test
    void compareImagesWithEmptyListThrowsException() {
        List<BufferedImage> empty = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
                () -> service.compareImages(ImageComparisonMode.SAME_PIXELS, empty));
        assertThrows(IllegalArgumentException.class,
                () -> service.compareImages(ImageComparisonMode.UNIQUE_PIXELS, empty));
    }

    /**
     * Using mode SAME_PIXELS, two identical images should keep original pixels
     */
    @Test
    void compareSamePixelsWhenAllIdenticalReturnsSameImage() {
        BufferedImage img1 = createSolidImage(3, 3, RED_PIXEL);
        BufferedImage img2 = createSolidImage(3, 3, RED_PIXEL);
        BufferedImage result = service.compareImages(ImageComparisonMode.SAME_PIXELS, List.of(img1, img2));

        assertImagesEqual(img1, result);
    }

    /**
     * Using mode SAME_PIXELS, the resulting image should contain all the pixels that are equivalent between the two images,
     * replacing any that differ with a transparent pixel
     */
    @Test
    void compareSamePixelsWithDifferenceReturnsOnlyCommonPixels() {
        BufferedImage img1 = createSolidImage(2, 2, RED_PIXEL);
        BufferedImage img2 = createSolidImage(2, 2, RED_PIXEL);
        // Different pixel at (1,1)
        img2.setRGB(1, 1, GREEN_PIXEL);

        BufferedImage expected = createSolidImage(2, 2, RED_PIXEL);
        // Expect different pixel replaced by transparent pixel
        expected.setRGB(1, 1, TRANSPARENT_PIXEL);

        BufferedImage result = service.compareImages(ImageComparisonMode.SAME_PIXELS, List.of(img1, img2));
        assertImagesEqual(expected, result);
    }

    /**
     * Using mode UNIQUE_PIXELS, comparing two identical images should result in a fully transparent output image
     */
    @Test
    void compareUniquePixelsWhenAllIdenticalReturnsTransparent() {
        BufferedImage img1 = createSolidImage(2, 2, ARBITRARY_PIXEL);
        BufferedImage img2 = createSolidImage(2, 2, ARBITRARY_PIXEL);
        BufferedImage result = service.compareImages(ImageComparisonMode.UNIQUE_PIXELS, List.of(img1, img2));

        BufferedImage expected = createSolidImage(2, 2, TRANSPARENT_PIXEL);
        assertImagesEqual(expected, result);
    }

    /**
     * Using mode UNIQUE_PIXELS comparing two completely different images should result in a fully black output imgae
     * (since all pixels are pairwise unique between the two images)
     */
    @Test
    void compareUniquePixelsWhenAllDistinctReturnsBlack() {
        BufferedImage img1 = createSolidImage(2, 2, RED_PIXEL);
        BufferedImage img2 = createSolidImage(2, 2, GREEN_PIXEL);
        BufferedImage result = service.compareImages(ImageComparisonMode.UNIQUE_PIXELS, List.of(img1, img2));

        BufferedImage expected = createSolidImage(2, 2, BLACK_PIXEL);
        assertImagesEqual(expected, result);
    }

    /**
     * Pixels must be unique between ALL images
     * If pixel is red in image 1 and 2, but green in image 3, this should result in a transparent pixel, exactly
     * as would be expected if all 3 pixels were different
     */
    @Test
    void compareUniquePixelsWithDuplicateAcrossImagesReturnsTransparent() {
        BufferedImage img1 = createSolidImage(2, 2, RED_PIXEL);
        BufferedImage img2 = createSolidImage(2, 2, RED_PIXEL);
        BufferedImage img3 = createSolidImage(2, 2, GREEN_PIXEL);
        BufferedImage result = service.compareImages(ImageComparisonMode.UNIQUE_PIXELS, List.of(img1, img2, img3));

        BufferedImage expected = createSolidImage(2, 2, TRANSPARENT_PIXEL);
        assertImagesEqual(expected, result);
    }

    /**
     * While we don't expect comparing images of different sizes is useful, it is technically supported.
     * When comparing images if different sizes, the output image should always use the dimensions of the largest image.
     * Using mode SAME_PIXELS, we consider all pixels that appear in the larger image to be different from the "pixels"
     * that would appear in the smaller image, if it was extended to match the size of the larger image.
     */
    @Test
    void compareImagesWithDifferentSizesUsesLargestDimensions() {
        BufferedImage smallerImage = createSolidImage(1, 1, RED_PIXEL);
        BufferedImage largerImage = createSolidImage(2, 2, RED_PIXEL);

        BufferedImage result = service.compareImages(ImageComparisonMode.SAME_PIXELS, List.of(smallerImage, largerImage));

        BufferedImage expected = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        // Only pixels at (0,0) should be equal between the images, since the smallest image is size 1x1
        expected.setRGB(0, 0, RED_PIXEL);
        expected.setRGB(1, 0, TRANSPARENT_PIXEL);
        expected.setRGB(0, 1, TRANSPARENT_PIXEL);
        expected.setRGB(1, 1, TRANSPARENT_PIXEL);

        assertImagesEqual(expected, result);
    }
}
