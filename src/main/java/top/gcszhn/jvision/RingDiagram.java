package top.gcszhn.jvision;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.geom.Arc2D;
import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

public class RingDiagram extends JChart {
    private ArrayList<String> legends = new ArrayList<>();
    private ArrayList<Float> values = new ArrayList<>();
    private ArrayList<Color> colors = new ArrayList<>();
    private String title;
    private int width;
    private int height;
    private float[] radiusRange;
    private float gapRatio;
    private float[] valueRange;
    private float startAngle;
    private float arcAngle;
    private float step;
    public RingDiagram(String title, int width, int height, float gapRatio, float[] radiusRange, float[] valueRange, float startAngle, float arcAngle, float step) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.radiusRange = radiusRange;
        this.gapRatio = gapRatio;
        this.valueRange = valueRange;
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
        this.step = step;
    }
    @Override
    public void loadData(String file) {
        for (CSVRecord record: BasicTool.readCSV(file)) {
            try {
                legends.add(record.get(0));
                values.add(Float.parseFloat(record.get(1)));
                colors.add(ImageHelp.getColor(record.get(2)));
            } catch (Exception e) {
                throw new RuntimeException("Data parse error at record: " + record.getRecordNumber(), e);
            }
        }
    }

    @Override
    public void draw(String file) {
        if (values.size() == 0) {
            throw new RuntimeException("No data loaded");
        }
        try {
            String type = BasicTool.getFileExtName(file);
            CreateGraphics cg = new CreateGraphics(width, height, type, file);
            Graphics2D graphics2D = cg.getGraphics2D();
            graphics2D.setBackground(getBgColor());
            float band = (radiusRange[1] - radiusRange[0]) / (values.size()) / (1 + gapRatio);
            float gap = band * gapRatio;
            float radius = radiusRange[0];
            for (int idx = 0; idx < values.size(); idx++) {
                radius +=  band + (idx>0?gap:0);
                float value = values.get(idx);
                float angle = (value - valueRange[0]) * this.arcAngle / (valueRange[1] - valueRange[0]);
                graphics2D.setColor(colors.get(idx));
                graphics2D.fill(new AngularRing(width / 2, height / 2, radius, band, startAngle, angle));
            }
            graphics2D.setColor(Color.BLACK);
            radius += gap > 0 ? gap : 0.5 * band;
            Arc2D.Double arc = new Arc2D.Double(width / 2 - radius, height / 2 - radius, radius * 2, radius * 2, startAngle, arcAngle, Arc2D.OPEN);
            graphics2D.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics2D.draw(arc);

            for (double scale = valueRange[0]; scale <= valueRange[1] + 1e-5; scale+=this.step) {
                double rotateDegree =  - (scale - valueRange[0]) * this.arcAngle / (valueRange[1] - valueRange[0]) - startAngle;
                rotateText(
                    graphics2D, 
                    String.format("%.1f%%", scale *100), 
                    width / 2, 
                    height / 2, 
                    rotateDegree, 
                    radius + 0.3 * band, 
                    "l", 
                    "m",
                    new Font(getFontFamily(), Font.PLAIN, 5));
            }
            drawText(
                graphics2D, 
                this.title, 
                width / 2, 
                height / 2, 
                "m", "m", 
                new Font(getFontFamily(), Font.BOLD, 15));
            cg.saveToFlie();

        } catch (Exception e) {
            throw new RuntimeException("Draw error", e);
        }
    }
    
}
