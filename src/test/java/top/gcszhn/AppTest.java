package top.gcszhn;

import java.awt.Graphics2D;
import java.io.FileInputStream;
import java.awt.Font;


import org.junit.Test;

import top.gcszhn.jvision.CreateGraphics;
import top.gcszhn.jvision.ImageHelp;
import top.gcszhn.jvision.JChart;
import top.gcszhn.jvision.RingDiagram;

/**
 * Unit test
 */
public class AppTest {

    @Test
    public void rangDiagramTest() throws Exception {
        float[] valueRange0 = new float[] { 0.5f, 1f };
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
        ringDiagram0.loadData("data\\data-precision-MAST.csv");
        ringDiagram0.draw("data\\data-precision-MAST.pdf");
    }
    @Test
    public void rotateTextTest() throws Exception {
        CreateGraphics cg = new CreateGraphics(500, 500, "pdf", "data/rotate-text.pdf");
        Graphics2D graphics2d = cg.getGraphics2D();
        JChart.rotateText(
                graphics2d,
                "TT",
                250,
                250,
                0,
                200,
                "m",
                "m",
                true,
                new Font(JChart.DEFAULT_FONT_FAMILY, Font.PLAIN, 500 * 5 / 150));
        JChart.rotateText(
                graphics2d,
                "TT",
                250,
                250,
                0,
                200,
                "m",
                "m",
                false,
                new Font(JChart.DEFAULT_FONT_FAMILY, Font.PLAIN, 500 * 5 / 150));
        cg.saveToFlie();
    }
    @Test
    public void readFontTest() {
        try {
            System.out.println(Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(ImageHelp.getFontFileName("Times New Roman", Font.BOLD + Font.ITALIC))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
