# 概述
该包包含了一些可视化功能，可以添加下列maven依赖使用它
```xml
    <repositories>
        <repository>
            <id>github</id>
            <url>https://maven.pkg.github.com/gcs-zhn/jvision</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>top.gcszhn</groupId>
            <artifactId>jvision</artifactId>
        </dependency>
    </dependencies>
```
例如下面是绘制示例环形统计图
```java
import top.gcszhn.jvision.RingDiagram;
public class Test {
    public static void main(String[] args) {
        float[] valueRange0 = new float[]{0.5f, 1f};
        RingDiagram ringDiagram0 = new RingDiagram(
            "Precision", 
            150, 
            150, 
            0.0f, 
            new float[]{26, 50}, 
            valueRange0, 
            90, 
            -350, 
            (valueRange0[1]-valueRange0[0]) / 10);
        ringDiagram0.loadData("sample/data-precision-MAST.csv");
        ringDiagram0.draw("sample/data-precision-MAST.png");
    }
}
```
![ring diagram](sample\data-precision-MAST.png)
CircularHistogram
![CircularHistogram](sample\Circular_histogram.png)

