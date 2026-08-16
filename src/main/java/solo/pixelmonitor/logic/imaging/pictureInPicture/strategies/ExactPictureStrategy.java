package solo.pixelmonitor.logic.imaging.pictureInPicture.strategies;

import solo.pixelmonitor.logic.imaging.pictureInPicture.Coordinate;
import solo.pixelmonitor.logic.imaging.pictureInPicture.PictureInPictureResult;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExactPictureStrategy implements PictureInPictureStrategy {

    private PictureInPictureResult createEmptyResult(BufferedImage targetImage, BufferedImage srcImage) {
        return new PictureInPictureResult(
                srcImage,
                targetImage,
                null,
                false,
                0,
                0,
                targetImage.getWidth(),
                targetImage.getHeight()
        );
    }

    // TODO: Likely useful to move to a shared class
    private int[][] extractRows(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        int[][] rows = new int[height][width];
        for (int y = 0; y < height; y++) {
            image.getRGB(0, y, width, 1, rows[y], 0, width);
        }
        return rows;
    }

    /**
     * Although this strategy is looking for an "exact" match, some common color models don't support alpha values,
     * and it is quite likely that a user is comparing a screenshot made without alpha values, to one which might have
     * slightly different values.
     * Therefore, we strip the alpha channel. We can do this by masking off the alpha byte, the ARGB pixel is represented
     * by the 4 bytes:
     * 0x 00 (alpha) 00 (red) 00 (green) 00 (blue)
     *  The mask 0x 00 FF FF FF therefore only removes the alpha byte
     */
    private int convertARGBtoRGB(int ARGBPixel) {
        return ARGBPixel & 0x00FFFFFF;
    }

    // TODO: Likely useful to move to a shared class
    /**
     * Checks whether the target image matches the source image at the given start coordinate.
     */
    private boolean matchesAt(BufferedImage source, int[][] targetRows, int startX, int startY) {
        int targetHeight = targetRows.length;
        int targetWidth = targetRows[0].length;

        for (int row = 0; row < targetHeight; row++) {
            int[] sourceRow = new int[targetWidth];
            source.getRGB(startX, startY + row, targetWidth, 1, sourceRow, 0, targetWidth);

            for (int col = 0; col < targetWidth; col++) {
                if (convertARGBtoRGB(sourceRow[col]) != convertARGBtoRGB(targetRows[row][col])) {
                    return false;
                }
            }
        }
        return true;
    }

    private PictureInPictureResult findPictureUsingStartingPoints(List<Coordinate> potentialStartingPoints,
                                                                  BufferedImage targetImage, BufferedImage srcImage) {

        int[][] targetRows = extractRows(targetImage);
        Optional<Coordinate> match = potentialStartingPoints.parallelStream()
                .filter(coordinate -> matchesAt(srcImage, targetRows, coordinate.x(), coordinate.y()))
                // TODO: If we ever want to find multiple occurrences, it *should* simply be changing this last pipe method to a collector
                .findAny();

        if (match.isEmpty()) {
            return createEmptyResult(targetImage, srcImage);
        }

        Coordinate matchingCoordinate = match.get();
        return new PictureInPictureResult(
                srcImage,
                targetImage,
                null,
                true,
                matchingCoordinate.x(),
                matchingCoordinate.y(),
                targetImage.getWidth(),
                targetImage.getHeight()
        );
    }


    @Override
    public PictureInPictureResult findPicture(BufferedImage targetImage, BufferedImage srcImage) {
        int targetWidth = targetImage.getWidth();
        int targetHeight = targetImage.getHeight();
        int firstPixel = targetImage.getRGB(0, 0);

        int maxX = srcImage.getWidth() - targetWidth;
        int maxY = srcImage.getHeight() - targetHeight;

        List<Coordinate> potentialStartingPoints = new ArrayList<>();
        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                if (srcImage.getRGB(x, y) == firstPixel) {
                    potentialStartingPoints.add(new Coordinate(x, y));
                }
            }
        }

        if (potentialStartingPoints.isEmpty()) {
            return createEmptyResult(targetImage, srcImage);
        }

        return findPictureUsingStartingPoints(potentialStartingPoints, targetImage, srcImage);
    }
}
