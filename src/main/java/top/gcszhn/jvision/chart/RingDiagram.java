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
package top.gcszhn.jvision.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

import lombok.Getter;
import lombok.Setter;
import top.gcszhn.jvision.shape.AngularRing;
import top.gcszhn.jvision.tools.BasicTool;
import top.gcszhn.jvision.tools.CreateGraphics;
import top.gcszhn.jvision.tools.DrawTool;
import top.gcszhn.jvision.tools.ImageTool;
import top.gcszhn.jvision.Constant;
import top.gcszhn.jvision.JvisionException;
import top.gcszhn.jvision.Stage;

public class RingDiagram implements JChart {
    private static final long serialVersionUID = 202207131807254L;
    private ArrayList<String> legends = new ArrayList<>();
    private ArrayList<Float> values = new ArrayList<>();
    private ArrayList<Color> colors = new ArrayList<>();
    private String title;
    private @Setter @Getter int width;
    private @Setter @Getter int height;
    private float[] radiusRange;
    private float gapRatio;
    private float[] valueRange;
    private float startAngle;
    private float arcAngle;
    private float step;
    private boolean balance;
    private @Setter @Getter String fontFamily = Constant.DEFAULT_FONT_FAMILY;
    private @Setter @Getter int fontStyle = Constant.DEFAULT_FONT_STYLE;

    public RingDiagram(String title, int width, int height, float gapRatio, float[] radiusRange, float[] valueRange,
            float startAngle, float arcAngle, float step, boolean balance) {
        this.title = title;
        this.radiusRange = radiusRange;
        this.gapRatio = gapRatio;
        this.valueRange = valueRange;
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
        this.step = step;
        this.balance = balance;
        setHeight(height);
        setWidth(width);
    }

    @Override
    public void loadData(String file) throws JvisionException {
        Iterable<CSVRecord> records;
        try {
            records = BasicTool.readCSV(file);
        } catch (IOException e1) {
            throw new JvisionException("Read CSV file failed", e1, Stage.DATA_LOADING);
        }
        for (CSVRecord record : records) {
            try {
                legends.add(record.get(0));
                values.add(Float.parseFloat(record.get(1)));
                colors.add(ImageTool.getColor(record.get(2)));
            } catch (Exception e) {
                throw new JvisionException("Data parse error at record: " + record.getRecordNumber(), e,
                        Stage.DATA_LOADING);
            }
        }
    }

    @Override
    public void draw(String file) throws JvisionException {
        if (values.size() == 0) {
            throw new JvisionException("No data loaded", null, Stage.DATA_LOADING);
        }

        String type = BasicTool.getFileExtName(file);
        CreateGraphics cg = new CreateGraphics(width, height, type, file);
        Graphics2D graphics2D = cg.getGraphics();
        graphics2D.setBackground(Constant.DEFAULT_BACKGROUND_COLOR);
        graphics2D.clearRect(0, 0, width, height);
        float band = (radiusRange[1] - radiusRange[0]) / (values.size()) / (1 + gapRatio);
        float gap = band * gapRatio;
        float radius = radiusRange[0];
        for (int idx = 0; idx < values.size(); idx++) {
            radius += band + (idx > 0 ? gap : 0);
            float value = values.get(idx);
            float angle = (value - valueRange[0]) * this.arcAngle / (valueRange[1] - valueRange[0]);
            graphics2D.setColor(colors.get(idx));
            graphics2D.fill(new AngularRing(width / 2, height / 2, radius, band, startAngle, angle));
        }
        graphics2D.setColor(Color.BLACK);
        radius += gap > 0 ? gap : 0.5 * band;
        Arc2D.Double arc = new Arc2D.Double(width / 2 - radius, height / 2 - radius, radius * 2, radius * 2,
                startAngle, arcAngle, Arc2D.OPEN);
        graphics2D.setStroke(new BasicStroke(0.5f * width / 150.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.draw(arc);
        double delta = this.step * Math.abs(this.arcAngle) / (valueRange[1] - valueRange[0]);
        int labelFontSize = (int) (radiusRange[1] * delta / 180);
        for (double scale = valueRange[0]; scale <= valueRange[1] + 1e-5; scale += this.step) {
            double rotateDegree = -(scale - valueRange[0]) * this.arcAngle / (valueRange[1] - valueRange[0])
                    - startAngle;
            graphics2D.setFont(new Font(getFontFamily(), getFontStyle(), labelFontSize));
            DrawTool.rotateText(
                    graphics2D,
                    String.format("%.1f%%", scale * 100),
                    width / 2,
                    height / 2,
                    balance ? rotateDegree + 180 : rotateDegree,
                    radius + (balance ? 1.6 : 0.8) * labelFontSize,
                    balance ? "m" : "l",
                    "m",
                    balance);
        }
        if (title != null) {
            graphics2D.setFont(new Font(getFontFamily(), Font.BOLD, 10 * width / 150));
            DrawTool.drawText(
                    graphics2D,
                    this.title,
                    width / 2,
                    height / 2,
                    "m", "m");
        }
        cg.saveToFile();
    }
}
