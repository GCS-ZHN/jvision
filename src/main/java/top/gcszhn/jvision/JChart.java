package top.gcszhn.jvision;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import lombok.Getter;
import lombok.Setter;

public abstract class JChart {
    /**Background color */
    private @Setter @Getter Color bgColor = Color.WHITE;
    /**Font color */
    private @Setter @Getter Color fontColor = Color.BLACK;
    /**Font */
    private @Setter @Getter String fontFamily = "Calibri";
    public abstract void loadData(String file);
    public abstract void draw(String file);
    public void drawText (
        Graphics2D graphics2D,
        String label, 
        double x, 
        double y, 
        String h_mode, 
        String v_mode,
        Font font
        ) throws Exception {
            rotateText(graphics2D, label, x, y, 0, 0, h_mode, v_mode, font);
        }
    public void rotateText (
        Graphics2D graphics2D,
        String label, 
        double rotateCenter_x, 
        double rotateCenter_y, 
        double rotateDegree, 
        double rotateR, 
        String h_mode, 
        String v_mode,
        Font font) throws Exception {
        graphics2D.setFont(font);
        graphics2D.setColor(fontColor);
        graphics2D.setBackground(bgColor);
        FontMetrics metrics = graphics2D.getFontMetrics();
        float baseline_x;
        float baseline_y;
        int locat_x = (int) (rotateCenter_x - rotateR), locat_y = (int) rotateCenter_y;
        if (h_mode.equals("l")) {
            locat_x = (int) (rotateCenter_x + rotateR);
        }
        switch	 (v_mode) {
            case "m":{baseline_y =locat_y*1f - metrics.getHeight()/2f + metrics.getAscent();break;}
            case "u":{baseline_y =locat_y + metrics.getAscent();break;}
            case "d":{baseline_y =locat_y - metrics.getHeight() + metrics.getAscent();break;}
            default: throw new Exception("Ilegal mode symbol: "+v_mode);
        }
        switch (h_mode) {
            case "m":{baseline_x =locat_x*1f - metrics.stringWidth(label)/2f;break;}
            case "l":{baseline_x =locat_x;break;}
            case "r": {baseline_x =locat_x - metrics.stringWidth(label);break;}
            default: throw new Exception("Ilegal mode symbol: "+h_mode);
        }
        if (rotateDegree != 0) graphics2D.rotate(rotateDegree*Math.PI/180, rotateCenter_x, rotateCenter_y);
        graphics2D.drawString(label, baseline_x, baseline_y);
        if (rotateDegree != 0) graphics2D.rotate(-rotateDegree*Math.PI/180, rotateCenter_x, rotateCenter_y);
    }
}
