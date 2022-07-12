package top.gcszhn;

import org.junit.Test;

import top.gcszhn.jvision.RingDiagram;

/**
 * Unit test
 */
public class AppTest 
{

    @Test
    public void rangDiagramTest() throws Exception {
        float[] valueRange0 = new float[]{0f, 1f};
        RingDiagram ringDiagram0 = new RingDiagram(
            "Precision", 
            600, 
            600, 
            0.0f, 
            new float[]{100, 200}, 
            valueRange0, 
            90, 
            -350, 
            (valueRange0[1]-valueRange0[0]) / 10);
        ringDiagram0.loadData("sample/data-precision-MAST.csv");
        ringDiagram0.draw("sample/data-precision-MAST.png");
    }
}
