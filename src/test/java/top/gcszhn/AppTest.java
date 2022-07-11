package top.gcszhn;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import top.gcszhn.jvision.CreateGraphics;

import java.awt.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws Exception {
        CreateGraphics cg = new CreateGraphics(100, 100, "pdf", "test.pdf");
        Graphics2D graphics2D = cg.getGraphics2D();
        graphics2D.drawArc(0, 0, 100, 100, 90, 180);
        cg.saveToFlie();
    }
}
