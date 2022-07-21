package top.gcszhn;

import java.awt.Graphics2D;
import java.io.FileInputStream;
import java.awt.Font;

import org.junit.Test;

import top.gcszhn.jvision.Constant;
import top.gcszhn.jvision.JvisionException;
import top.gcszhn.jvision.chart.CircularHistogram;
import top.gcszhn.jvision.chart.RingDiagram;
import top.gcszhn.jvision.tools.CreateGraphics;
import top.gcszhn.jvision.tools.DrawTool;
import top.gcszhn.jvision.tools.ImageTool;

/**
 * Unit test
 */
public class AppTest {

    @Test
    public void rangDiagramTest() throws Exception {
        float[] valueRange0 = new float[] { 0.9f, 1f };
        RingDiagram ringDiagram0 = new RingDiagram(
                "Precision",
                600,
                600,
                0.2f,
                new float[] { 80, 200 },
                valueRange0,
                90,
                -336,
                (valueRange0[1] - valueRange0[0]) / 15,
                true);
        ringDiagram0.setFontFamily("Calibri");
        ringDiagram0.setFontStyle(Font.PLAIN);
        ringDiagram0.loadData("sample/ring_diagram_data/data-precision-Wilcoxon.csv");
        ringDiagram0.draw("sample/ring_diagram_data/data-precision-Wilcoxon.pdf");
        ringDiagram0.draw("sample/ring_diagram_data/data-precision-Wilcoxon.png");
        ringDiagram0.draw("sample/ring_diagram_data/data-precision-Wilcoxon.eps");
    }

    @Test
    public void rotateTextTest() throws Exception {
        CreateGraphics cg = new CreateGraphics(500, 500, "pdf", "sample/rotate-text.pdf");
        Graphics2D graphics2d = cg.getGraphics();
        graphics2d.setFont(new Font(Constant.DEFAULT_FONT_FAMILY, Font.PLAIN, 500 * 5 / 150));
        DrawTool.rotateText(
                graphics2d,
                "TT",
                250,
                250,
                0,
                200,
                "m",
                "m",
                true);
        graphics2d.setFont(new Font(Constant.DEFAULT_FONT_FAMILY, Font.PLAIN, 500 * 5 / 150));
        DrawTool.rotateText(
                graphics2d,
                "TT",
                250,
                250,
                0,
                200,
                "m",
                "m",
                false);
        cg.saveToFile();
    }

    @Test
    public void readFontTest() {
        try {
            System.out.println(Font.createFont(Font.TRUETYPE_FONT,
                    new FileInputStream(ImageTool.getFontFileName("Times New Roman", Font.BOLD + Font.ITALIC))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void circularHistogramTest() throws JvisionException {
        CircularHistogram histogram = new CircularHistogram();
        histogram.setFontFamily("Courier New");
        histogram.loadData("sample/circular_histogram_data/sample.csv", true, 100, true);
        histogram.draw("sample/circular_histogram_data/sample.pdf");
        histogram.draw("sample/circular_histogram_data/sample.eps");
        histogram.draw("sample/circular_histogram_data/sample.png");
    }
}
