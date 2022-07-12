package top.gcszhn.jvision;

import java.awt.geom.Area;
import java.awt.geom.Arc2D;

public class AngularRing extends Ring {
    public AngularRing(double x, double y, double width, double height, double widthBand, double heightBand, double startAngle, double arcAngle) {
        super(x, y, width, height, widthBand, heightBand);
        this.area.intersect(new Area(new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.PIE)));
    }
    public AngularRing(double centerX, double centerY, double radius, double band, double startAngle, double arcAngle) {
        this(centerX - radius, centerY - radius, radius * 2, radius * 2, band, band, startAngle, arcAngle);
    }
}
