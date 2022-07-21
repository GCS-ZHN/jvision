/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package top.gcszhn.jvision.shape;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Area;


/**
 * A class to create a ring.
 * @author GCS-ZHN
 * @version 1.0
 */
public class Ring implements Shape {
    /**
     * The area object used to draw the ring.
     */
    protected Area area;
    /**
     * Draw circle ring.
     * @param centerX the center x coordinate
     * @param centerY the center y coordinate
     * @param radius the radius of the ring
     * @param band the band of the ring
     */
    public Ring(double centerX, double centerY, double radius, double band) {
        this(centerX - radius, centerY - radius, radius * 2, radius * 2, band, band);
    }
    /**
     * Draw elliptical ring
     * @param centerX the center x coordinate
     * @param centerY the center y coordinate
     * @param width the width of the ring
     * @param height the height of the ring
     * @param widthBand the width band of the ring
     * @param heightBand the height band of the ring
     */
    public Ring(double centerX, double centerY, double width, double height, double widthBand, double heightBand) {
        Ellipse2D.Double outEllipse2D = new Ellipse2D.Double(centerX, centerY, width, height);
        Ellipse2D.Double inEllipse2D = new Ellipse2D.Double(centerX + widthBand, centerY + heightBand, width - 2 * widthBand, height - 2 * heightBand);
        this.area = new Area(outEllipse2D);
        this.area.subtract(new Area(inEllipse2D));
    }

    @Override
    public Rectangle getBounds() {
        return this.area.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        return this.area.getBounds2D();
    }

    @Override
    public boolean contains(double x, double y) {
        return this.area.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
        return this.area.contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return this.area.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return this.area.intersects(r);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return this.area.contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return this.area.contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return this.area.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return this.area.getPathIterator(at, flatness);
    }
    
}
