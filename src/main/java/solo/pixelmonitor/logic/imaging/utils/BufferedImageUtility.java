package solo.pixelmonitor.logic.imaging.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedImageUtility {
    public static BufferedImage copyImage(BufferedImage source){
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = b.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }
}
