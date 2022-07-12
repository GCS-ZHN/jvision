package top.gcszhn;

import org.junit.Test;

import top.gcszhn.jvision.RingDiagram;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void rangDiagramTest() throws Exception {
        float[] valueRange = new float[]{0.6358792f, 0.695f};
        RingDiagram ringDiagram = new RingDiagram(
            "Recall", 
            150, 
            150, 
            0.0f, 
            new float[]{26, 50}, 
            valueRange, 
            90, 
            -350, 
            (valueRange[1]-valueRange[0]) / 10);
        ringDiagram.loadData("data.csv");
        ringDiagram.draw("data.pdf");
    }
}
