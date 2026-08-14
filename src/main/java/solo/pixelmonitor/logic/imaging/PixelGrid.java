package solo.pixelmonitor.logic.imaging;

import lombok.Data;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Custom pixel grid class optimised for row access
 * We require LinkedHashMap with correctly ordered rows
 */
@Data
public class PixelGrid {
    private int columns;
    private int rows;
    private LinkedHashMap<Integer, List<Color>> rowColors;

    public PixelGrid(LinkedHashMap<Integer, List<Color>> rowColors, int columns) {
        this.rowColors = rowColors;
        this.rows = rowColors.size();
        this.columns = columns;
        rowColors.values().forEach(list -> {
            if (list.size() != columns)
                throw new IllegalArgumentException("Given color list does not conform to expected number of columns");
        });
    }
}
