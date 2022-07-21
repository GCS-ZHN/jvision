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
