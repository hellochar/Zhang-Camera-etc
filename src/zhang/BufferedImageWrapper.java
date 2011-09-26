/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zhang;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 *
 * @author Owner
 */
public class BufferedImageWrapper implements Serializable {

    final public int width;
    final public int height;
    final public int[] pixels;
    final public int size;
    final public int imageType;

    public BufferedImageWrapper(BufferedImage bi) {
        width = bi.getWidth();
        height = bi.getHeight();
        size = width * height;
        pixels = new int[size];
        imageType = bi.getType();
        bi.getRGB(0, 0, width, height, pixels, 0, width);
    }

    public BufferedImage getImage() {
        BufferedImage bi = new BufferedImage(width, height, imageType);
        bi.setRGB(0, 0, width, height, pixels, 0, width);
        return bi;
    }
}