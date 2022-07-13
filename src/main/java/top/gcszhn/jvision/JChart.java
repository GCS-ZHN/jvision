package top.gcszhn.jvision;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.Serializable;

/**
 * Abstract class for drawing charts.
 */
public abstract class JChart implements Serializable {
    /** Background color */
    public final static Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    /** Font color */
    public final static Color DEFAULT_FONT_COLOR = Color.BLACK;
    /** Font */
    public final static String DEFAULT_FONT_FAMILY = "Calibri";

    /**
     * Load data from file
     * @param file File path
     */
    public abstract void loadData(String file);

    /**
     * Draw chart to image file
     * @param file Image file name, such as png, jpg, pdf, etc.
     */
    public abstract void draw(String file);

    public static void drawText(
            Graphics2D graphics2D,
            String label,
            double x,
            double y,
            String h_mode,
            String v_mode,
            Font font) throws Exception {
        rotateText(graphics2D, label, x, y, 0, 0, h_mode, v_mode, font);
    }

    /**
     * Draw text with rotation
     * @param graphics2D Graphics2D object， Used to draw text
     * @param text Text to be drawn
     * @param centerX X coordinate of the rotation center
     * @param centerY Y coordinate of the rotation center
     * @param rotateDegree Rotation angle，in degrees
     * @param rotateR Radius of the rotation circle
     * @param h_mode Horizontal alignment mode，"l"：left，"m"：center，"r"：right
     * @param v_mode Vertical alignment mode，"u"：top，"m"：middle，"d"：bottom
     * @param font Font
     * @throws Exception
     */
    public static void rotateText(
            Graphics2D graphics2D,
            String text,
            double centerX,
            double centerY,
            double rotateDegree,
            double rotateR,
            String h_mode,
            String v_mode,
            Font font) throws Exception {
        rotateText(graphics2D, text, centerX, centerY, rotateDegree, rotateR, h_mode, v_mode, false,
                font);
    }

    /**
     * Draw text with rotation
     * @param graphics2D Graphics2D object， Used to draw text
     * @param text Text to be drawn
     * @param centerX X coordinate of the rotation center
     * @param centerY Y coordinate of the rotation center
     * @param rotateDegree Rotation angle，in degrees
     * @param rotateR Radius of the rotation circle
     * @param h_mode Horizontal alignment mode，"l"：left，"m"：center，"r"：right
     * @param v_mode Vertical alignment mode，"u"：top，"m"：middle，"d"：bottom
     * @param stringBalance Whether to balance the string，true：balance，false：not balance. Balance means the text is kept horizontal
     * @param font Font
     * @throws Exception
     */
    public static void rotateText(
            Graphics2D graphics2D,
            String text,
            double centerX,
            double centerY,
            double rotateDegree,
            double rotateR,
            String h_mode,
            String v_mode,
            boolean stringBalance,
            Font font) throws Exception {
        graphics2D.setFont(font);
        graphics2D.setColor(DEFAULT_FONT_COLOR);
        graphics2D.setBackground(DEFAULT_BACKGROUND_COLOR);
        FontMetrics metrics = graphics2D.getFontMetrics();
        float baseline_x;
        float baseline_y;
        int locat_x = (int) (centerX - rotateR), locat_y = (int) centerY;
        if (h_mode.equals("l")) {
            locat_x = (int) (centerX + rotateR);
        }
        switch (v_mode) {
            case "m": {
                baseline_y = locat_y * 1f - metrics.getHeight() / 2f + metrics.getAscent();
                break;
            }
            case "u": {
                baseline_y = locat_y + metrics.getAscent();
                break;
            }
            case "d": {
                baseline_y = locat_y - metrics.getHeight() + metrics.getAscent();
                break;
            }
            default:
                throw new Exception("Ilegal mode symbol: " + v_mode);
        }
        switch (h_mode) {
            case "m": {
                baseline_x = locat_x * 1f - metrics.stringWidth(text) / 2f;
                break;
            }
            case "l": {
                baseline_x = locat_x;
                break;
            }
            case "r": {
                baseline_x = locat_x - metrics.stringWidth(text);
                break;
            }
            default:
                throw new Exception("Ilegal mode symbol: " + h_mode);
        }
        if (rotateDegree != 0) {
            graphics2D.rotate(rotateDegree * Math.PI / 180, centerX, centerY);
            if (stringBalance) graphics2D.rotate(-rotateDegree * Math.PI / 180, locat_x, locat_y);
        }
        graphics2D.drawString(text, baseline_x, baseline_y);
        if (rotateDegree != 0) {
            if (stringBalance) graphics2D.rotate(rotateDegree * Math.PI / 180, locat_x, locat_y);
            graphics2D.rotate(-rotateDegree * Math.PI / 180, centerX, centerY);
        }
    }
}
