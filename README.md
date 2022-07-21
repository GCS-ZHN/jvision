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
            <version>1.2</version>
        </dependency>
    </dependencies>
```
同时github的maven registry要求[登录认证](https://cwiki.apache.org/confluence/display/MAVEN/DependencyResolutionException)，即只允许github用户下载，不像maven中央仓库无需注册即可下载。具体配置有[官方文档](https://docs.github.com/cn/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)，主要是在`settings.xml`中配置server，注意token不是登录密码，需要自行创建，[快捷链接](https://github.com/settings/tokens)。
```xml
  <servers>
      <server>
      <id>github</id>
      <username>你的github账号</username>
      <password>你的github创建的具有下载package权限的token</password>
    </server>
   </servers>
 ```
# 使用
目前支持两种图形类型
1. 绘制示例环形统计图
```java
import top.gcszhn.jvision.chart.RingDiagram;
public class Test {
    public static void main(String[] args) {
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
}
```
![ring diagram](sample/ring_diagram_data/data-precision-Wilcoxon.png)
2. 绘制环形直方图
```java
import top.gcszhn.jvision.chart.CircularHistogram;
public class Test {
    public static void main(String[] args) {
        CircularHistogram histogram = new CircularHistogram();
        histogram.setFontFamily("Courier New");
        histogram.loadData("sample/circular_histogram_data/sample.csv", true, 100, true);
        histogram.draw("sample/circular_histogram_data/sample.pdf");
        histogram.draw("sample/circular_histogram_data/sample.eps");
        histogram.draw("sample/circular_histogram_data/sample.png");
    }
}
```
![CircularHistogram](sample/circular_histogram_data/sample.png)

# 更新预告
- 支持flowChart
- 支持多样本进展时间线
