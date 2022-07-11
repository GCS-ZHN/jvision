package top.gcszhn.jvision;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * 绘制流动连接图的业务实现类
 * @version 2.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class FlowChart {
    /**
     * 图像等比放大比例
     */
    private static int ratio = 1;
    /**
     * 曲线粗细，单位像素
     */
    private float[] curveBond = {6 *ratio};
    /**
     * 曲张调控角
     */
    private double[] curveAngle = {30}; //必须在（-90,90）开区间内，单位为度
    /**
     * 曲线透明度A
     */
    private int[] curveAlpha = {180};//必须在0~255
    /**
     * 画布
     */
    private Graphics2D graphics = null;
    /**
     * 字体指标，用于对字体绘图进行定位
     */
    private FontMetrics metrics = null;
    /**
     * 默认字体大小
     */
    private int fontSize = 28 * ratio;
    /**
     * 默认字体类型
     */
    private String fontName = "Courier New";
    /**
     * 默认字体颜色
     */
    private Color fontColor = Color.WHITE;
    /**
     * 画布宽度
     */
    private final int figwidth;
    /**
     * 画布高度
     */
    private final int figheight;
    /**
     * 以方块节点ID名为K，NodeBox对象为V的哈希表
     */
    private HashMap<String, NodeBox> nodeMap = new HashMap<>();
    /**
     * 以节点深度为K，该深度下所有节点ID名为V的树状表
     */
    private HashMap<Integer, ArrayList<String>> nodeTree = new HashMap<>();
    /**
     * 表示不同深度的方块节点的尺寸大小以及不同深度的间距
     */
    private int[][] sizePerLayer = new int[][]{
            {36 *ratio, 72 * ratio, 162 *ratio},//KNN~
            {36 *ratio, 72 * ratio, 162 *ratio},//KNN~
            {36 *ratio, 90 * ratio, 198 *ratio},//KNN~
            {36 *ratio, 108 * ratio, 144 *ratio},//KNN~
            {36 *ratio, 108 * ratio, 144 *ratio},//KNN~
            {36 *ratio, 108 * ratio, 144 *ratio}//KNN~
    };
    /**
     * 画图左边距或者旋转垂直后上边距
     */
    private int xlocation = 200 * ratio;
    /**
     * 构造方法
     * @param width 画图宽度
     * @param height 画图高度
     * @param inputfile 输入文件名
     * @throws IOException 输入输出异常
     * @throws Exception 其他异常
     */
    public FlowChart (int width, int height, String inputfile) throws IOException, Exception {
        figwidth = width *ratio;
        figheight = height *ratio;
        loadData(inputfile, true);
    }

    /**
     * 将十六进制的RGB或RGBA参数转为颜色
     * @param nm 十六进制的RGB或RGBA参数
     * @return Color对象
     */
    private Color decodeRGBA(String nm) {
        Color color = Color.decode(nm.substring(0, 7));
        if (nm.length() == 9) {
            int a = Integer.parseInt(nm.substring(7), 16);
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
        }
        return color;
    }
    /**
     * 调整画图的放大比例
     * @param rt 新的放大比例
     */
    public static void setRatio(int rt) {
        if (rt > 0) ratio = rt;
    }
    /**
     * 设置曲线属性
     * @param angles 曲线调整角，在-90~90°之间，开区间
     * @param alphas 透明度，在0~255之间，闭区间
     * @param bonds 曲线粗细，单位像素
     */
    public void setCurveProperty(double[] angles, int[] alphas, float[] bonds) {
        boolean flag = true;
        for (double angle:angles) {
            flag = -90 < angle && angle < 90;
            if (!flag) break;
        }
        if (flag) {
            curveAngle = angles;
        } else flag = true;
        for (int alpha: alphas) {
            flag = 0 <= alpha && alpha <= 255;
            if (!flag) break;
        }
        if (flag) {
            curveAlpha = alphas;
        } else flag = true;
        for (int i = 0; i < bonds.length; i++) {
            flag = bonds[i] > 0;
            bonds[i] *= ratio;
            if (!flag) break;
        }
        if (flag) {
            curveBond = bonds;
        }
    }
    /**
     * 加载节点数据
     * @param filename 输入文件名称
     * @param head 输入是否包含标题
     * @throws FileNotFoundException 文件缺失异常
     * @throws IOException 输入输出异常
     */
    private void loadData (String filename, boolean head) throws FileNotFoundException, IOException {
        try (LineNumberReader lnr = BasicTool.BufferRead(filename)) {
            String line;
            while ((line = lnr.readLine()) != null) {
                if (head && lnr.getLineNumber() == 1) continue;
                String info[] = line.split("\t");//node name node deepth node name node deepth
                String nodeID = info[0];
                String[] downstreamNodes = info[1].split(";");
                String[] upstreamNodes = info[2].split(";");
                int deepth = Integer.parseInt(info[3]);
                String nodeLabel = info[4];
                Color filledColor = decodeRGBA(info[5]);
                Color bondColor = decodeRGBA(info[6]);
				/*
				如果没有，放入当前nodeMap
				*/
                nodeMap.putIfAbsent(nodeID, new NodeBox(deepth, filledColor, bondColor));
                NodeBox nb = nodeMap.get(nodeID);
                nb.setDeepth(deepth);
                nb.setColors(filledColor, bondColor);
                nb.setLabel(nodeLabel);
                nb.setID(nodeID);
                int dindex = deepth;
                while (dindex > sizePerLayer.length - 1) dindex -= sizePerLayer.length;
                nb.setSize(sizePerLayer[dindex][0], sizePerLayer[dindex][1]);
                nodeTree.putIfAbsent(deepth, new ArrayList<>());
                nodeTree.get(deepth).add(nodeID);//更新节点网络树
                for (String dsnodeID: downstreamNodes) {
                    if (dsnodeID.equals("-")) break;
                    nodeMap.putIfAbsent(dsnodeID, new NodeBox(0));
                    NodeBox dsnode = nodeMap.get(dsnodeID);
                    nb.addDownstreamNode(dsnode);
                    dsnode.addUpstreamNode(nb);
                }
                if (!info[2].equals("-")) nb.clearUpstreamNode();
                for (String usnodeID: upstreamNodes) {
                    if (usnodeID.equals("-")) break;
                    nodeMap.putIfAbsent(usnodeID, new NodeBox(0));
                    NodeBox usnode = nodeMap.get(usnodeID);
                    nb.addUpstreamNode(usnode);
                }
            }
        }
    }
    /**
     * 配置绘图字体
     * @param font 字体
     */
    private void setFont(Font font) {
        if (graphics == null) return;
        graphics.setFont(font);
        metrics = graphics.getFontMetrics();
    }
    /**
     * 绘制流程图
     * @param outputfile 输出文件
     * @param isRotate 是否旋转顺时针90度最终图像，即逆时针旋转90度画布
     * @throws IOException 输入输出异常
     * @throws Exception 其他异常
     */
    public void paintFlowChart(String outputfile, boolean isRotate) throws IOException, Exception {
        String[] tmp = outputfile.split("\\.");
        CreateGraphics cg = new CreateGraphics(figwidth, figheight, tmp[tmp.length - 1], outputfile);
        graphics = cg.getGraphics2D();
        if (isRotate) {
            graphics.rotate(Math.toRadians(90), figwidth/2, figheight/2);
            xlocation += (figwidth - figheight)/2;
        }
        setFont(new Font(fontName,  Font.BOLD, fontSize));
        paintFlowChart();
        cg.saveToFlie();
    }
    /**
     * 绘制流程图
     */
    private void paintFlowChart () throws Exception {
        if (graphics == null || nodeTree.isEmpty() || metrics == null) return;
        int  ydis = 30 * ratio;
        //第一轮循环确定各个节点坐标
        for (int deepth = 0; nodeTree.containsKey(deepth); deepth ++) {
            int dindex = deepth;
            while (dindex > sizePerLayer.length - 1) dindex -= sizePerLayer.length;
            int xdis = sizePerLayer[dindex][2];
            int ylocation = (figheight - nodeTree.get(deepth).size() * (ydis + sizePerLayer[dindex][1]) + ydis) / 2;
            for (String nodeID: nodeTree.get(deepth)) {
                NodeBox nb = nodeMap.get(nodeID);
                nb.setLocation(xlocation, ylocation);
                int[] nbsize = nb.getSize();
                ylocation += (nbsize[1] + ydis);
            }
            xlocation += (sizePerLayer[dindex][0] + xdis);
        }


        //第二轮循环绘制连接曲线
        for (int deepth = 0; nodeTree.containsKey(deepth); deepth ++) {
            for (String nodeID: nodeTree.get(deepth)) {
                NodeBox nb = nodeMap.get(nodeID);
                int[] nbsize = nb.getSize();
                int[] nbloc = nb.getLocation();
                for (int i=0; i < nb.downstreamNodes.size(); i++) {
                    NodeBox dsnb = nb.downstreamNodes.get(i);
                    int dsindex = dsnb.upstreamNodes.indexOf(nb);
                    if (dsindex == -1) continue;
                    int cbindex = deepth, caindex = deepth, calindex = deepth;
                    while (cbindex >= curveBond.length) cbindex -= curveBond.length;
                    while (caindex >= curveAngle.length) caindex -= curveAngle.length;
                    while (calindex >= curveAlpha.length) calindex -= curveAlpha.length;
                    float bond = curveBond[cbindex];//对NON2节点粗细进行特别调整
                    int[] dsnbsize = dsnb.getSize();
                    int[] dsnbloc = dsnb.getLocation();
                    double modify_for_center1 = (nbsize[1] - nb.downstreamNodes.size() * curveBond[cbindex])/2;
                    double modify_for_center2 = (dsnbsize[1] - dsnb.upstreamNodes.size() * bond)/2;
                    double x1 = nbloc[0] + nbsize[0];
                    double y1 = nbloc[1] + (0.5 + i) * curveBond[cbindex] + modify_for_center1;
                    double x2 = dsnbloc[0];
                    double y2 = dsnbloc[1] + (0.5 + dsindex) * bond + modify_for_center2;
                    Color cl = nb.getColors()[0];
                    Color lineColor = new Color(cl.getRed(), cl.getGreen(), cl.getBlue(), curveAlpha[calindex]);
                    double Adangle = curveAngle[caindex];

                    drawConnectCurve(x1, y1, x2, y2, Adangle, bond,
                            lineColor,
                            true
                    );
                }
            }
        }
        //第三轮循环绘制方块与标签
        graphics.setStroke(new BasicStroke(2f));
        for (int deepth = 0; nodeTree.containsKey(deepth); deepth ++) {
            for (String nodeID: nodeTree.get(deepth)) {
                NodeBox nb = nodeMap.get(nodeID);
                nb.paint(graphics, true);
                String labeltext = nb.getLabel();
                double[] centerloc = nb.getCenter();
                int fontsize = (int) (nb.getSize()[0] * 0.618 + 1);
                setFont(new Font(fontName,  Font.BOLD, fontsize));
                drawSimpleLabel(labeltext, centerloc[0], centerloc[1], -90, 0, "m", "m", fontColor);
            }
        }
    }
    /**
     * 绘制连接曲线
     * @param x1 第一个点横坐标
     * @param y1 第一个点纵坐标
     * @param x2 第二个点横坐标
     * @param y2 第二个点纵坐标
     * @param ang 曲线调整角，单位角度
     * @param bond 曲线粗细
     * @param lc 曲线颜色
     */
    private void drawConnectCurve(double x1, double y1, double x2, double y2, double ang, float bond, Color lc, boolean solid) {
        if (graphics == null) return;
        if (x2 < x1) {//保证（x1， y1）点在（x2，y2）左侧
            double tmp = x2;
            x2 = x1;
            x1 = tmp;
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        boolean flag = (x1 - x2)*(y1 -y2) < 0; //判断是否为异号类型，即左下-右上类型
        if (flag) {//异号类型交换纵标为同号类型画图，再通过下面图像变换为异号类型。
            double tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        double radian = Math.toRadians(ang);
        double w = (x2 - x1) / Math.cos(radian);
        double h = Math.abs(y2 - y1) / (1 - Math.sin(radian));
        double locat_x1 = x1 - w/2;//椭圆弧定位点横标
        double locat_y1 = y1;
        double locat_x2 = x2 - w/2;
        double locat_y2 = y2 - h;
		/*
		二段圆弧，注意起止点是单向循环
		*/
        Arc2D arc1 = new Arc2D.Double(locat_x1, locat_y1, w, h, 90, ang-90, Arc2D.OPEN);
        Arc2D arc2 = new Arc2D.Double(locat_x2, locat_y2, w, h, 180 + ang, 90-ang, Arc2D.OPEN);
        Path2D path = new Path2D.Double(arc1);
        path.append(arc2, true);//将arc2与arc1收尾相连
        if (flag) {
            path.transform(AffineTransform.getScaleInstance(1d,  -1d));//图像横坐标缩放1倍，纵坐标缩放-1倍，即垂直对称
            path.transform(AffineTransform.getTranslateInstance(0, y1 + y2));//对称图在画布上面，平移变换入画布
        }
        Color olc = graphics.getColor();
        graphics.setColor(lc);
        Stroke obs = graphics.getStroke();
        BasicStroke solidStroke = new BasicStroke(bond);
        BasicStroke dashStroke = new BasicStroke(bond, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9 * ratio}, 0);
        graphics.setStroke(solid?solidStroke:dashStroke);//设置粗细
        graphics.draw(path);
        graphics.setStroke(obs);
        graphics.setColor(olc);

    }
    /**
     * 绘制文本标签
     * @param label 标签内容
     * @param rotateCenter_x 标签定位横坐标（从左上角为0）
     * @param rotateCenter_y 标签定位纵坐标（从左上角为0）
     * @param h_mode 水平对齐模式符，m为水平居中对齐，l为水平左对齐，r为水平右对齐
     * @param v_mode 垂直对齐模式符，m为垂直居中对齐，u为顶端对齐，d为底端对齐
     * @param FontColor 文本标签字体颜色
     * @throws Exception 异常
     */
    private void drawSimpleLabel (String label, double rotateCenter_x, double rotateCenter_y, double rotateDegree, double rotateR, String h_mode, String v_mode, Color FontColor) throws Exception {
        if (graphics == null) return;
        float baseline_x;
        float baseline_y;
        int locat_x = (int) (rotateCenter_x - rotateR), locat_y = (int) rotateCenter_y;
        if (h_mode.equals("l")) {
            locat_x = (int) (rotateCenter_x + rotateR);
        }
        switch	 (v_mode) {//垂直对齐方式
            case "m":{baseline_y =locat_y*1f - metrics.getHeight()/2f + metrics.getAscent();break;}
            case "u":{baseline_y =locat_y + metrics.getAscent();break;}
            case "d":{baseline_y =locat_y - metrics.getHeight() + metrics.getAscent();break;}
            default: throw new Exception("Ilegal mode symbol: "+v_mode);
        }
        switch (h_mode) {//水平对齐方式
            case "m":{baseline_x =locat_x*1f - metrics.stringWidth(label)/2f;break;}
            case "l":{baseline_x =locat_x;break;}
            case "r": {baseline_x =locat_x - metrics.stringWidth(label);break;}
            default: throw new Exception("Ilegal mode symbol: "+h_mode);
        }
        graphics.setColor(FontColor);//设置字体颜色
        graphics.rotate(rotateDegree*Math.PI/180, rotateCenter_x, rotateCenter_y);
        graphics.drawString(label, baseline_x, baseline_y);
        graphics.rotate(-rotateDegree*Math.PI/180, rotateCenter_x, rotateCenter_y);
    }
}
/**
 * 定义表示流程图矩形节点类
 */
class NodeBox {
    /**
     * 矩形宽度与圆角直径的比例关系
     */
    private int widthToD = 6;
    /**
     * 节点对象形状宽度
     */
    private int width;
    /**
     * 节点对象形状高度
     */
    private int height;
    /**
     * 节点对象横坐标
     */
    private int box_x;
    /**
     * 节点对象纵坐标
     */
    private int box_y;
    /**
     * 节点对象在节点树中深度
     */
    private int deepth;
    /**
     * 节点对象标签
     */
    private String label = "";
    /**
     * 节点对象ID
     */
    private String id = null;
    /**
     * 节点对象填充色
     */
    private Color filledColor;
    /**
     * 节点对象边框色
     */
    private Color sideColor;
    /**
     * 下游节点列表
     */
    protected final ArrayList <NodeBox> upstreamNodes = new ArrayList<>();
    /**
     * 上游节点列表
     */
    protected final ArrayList <NodeBox> downstreamNodes = new ArrayList<>();
    /**
     * 节点对象构造函数
     * @param x 节点横坐标
     * @param y 节点纵坐标
     * @param width 节点宽度
     * @param height 节点高度
     * @param deepth 节点深度
     * @param filledColor 节点填充色
     * @param sideColor  节点边框色
     */
    public NodeBox(int x, int y, int width, int height, int deepth, Color filledColor, Color sideColor) {
        box_x = x;
        box_y = y;
        this.width = width;
        this.height = height;
        this.deepth = deepth;
        this.filledColor = filledColor;
        this.sideColor = sideColor;
    }
    /**
     * 节点对象构造函数， 构造默认宽高深的节点对象
     * @param x 节点横坐标
     * @param y 节点纵坐标
     * @param filledColor 节点填充色
     * @param sideColor  节点边框色
     */
    public NodeBox(int x, int y, Color filledColor, Color sideColor) {
        this(x, y, 40, 30, 0, filledColor, sideColor);
    }
    /**
     * 节点对象构造函数，构造默认坐标、尺寸的节点对象
     * @param deepth 节点深度
     * @param filledColor 节点填充色
     * @param sideColor 节点背景色
     */
    public NodeBox(int deepth, Color filledColor, Color sideColor) {
        this(0, 0, 40, 30, deepth, filledColor, sideColor);
    }
    /**
     * 节点对象构造函数，构造默认定位点坐标、节点尺寸、颜色的节点对象
     * @param deepth 节点深度
     */
    public NodeBox(int deepth) {
        this(0, 0, 40, 30, deepth, Color.BLUE, Color.BLUE);
    }
    /**
     * 设置当前节点形状的宽度与高度
     * @param width 宽度
     * @param height 高度
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    /**
     * 获取当前节点形状的宽度与高度
     * @return 宽度、高度按次序组成的整型数组
     */
    public int[] getSize() {
        return new int[] {width, height};
    }
    /**
     * 设置当前节点的定位点坐标
     * @param x 横坐标
     * @param y 纵坐标
     */
    public void setLocation (int x, int y) {
        box_x = x;
        box_y = y;
    }
    /**
     * 获取当前节点的定位点坐标
     * @return 横纵坐标组成的整型数组
     */
    public int [] getLocation() {
        return new int[] {box_x, box_y};
    }
    /**
     * 设置当前节点在节点树中的深度
     * @param deepth 深度
     */
    public void setDeepth(int deepth) {
        this.deepth = deepth;
    }
    /**
     * 获取当前节点在节点树中的深度
     * @return 代表深度的整型值
     */
    public int getDeepth() {
        return deepth;
    }
    /**
     * 设置当前节点的填充色与边框色
     * @param filledColor 填充色
     * @param sideColor 边框色
     */
    public void setColors (Color filledColor, Color sideColor) {
        this.filledColor = filledColor;
        this.sideColor = sideColor;
    }
    /**
     * 获取当前节点的填充色与边框色
     * @return  填充色、边框色的Color对象数组
     */
    public Color[] getColors () {
        return new Color[] {filledColor, sideColor};
    }
    /**
     * 添加当前节点的上游节点
     * @param nodebox 上游节点对象
     */
    public void addUpstreamNode(NodeBox nodebox) {
        if (upstreamNodes.contains(nodebox)) return;
        upstreamNodes.add(nodebox);
    }
    /**
     * 添加当前节点的下游节点
     * @param nodeBox 下游节点对象
     */
    public void addDownstreamNode(NodeBox nodeBox) {
        if (downstreamNodes.contains(nodeBox)) return;
        downstreamNodes.add(nodeBox);
    }
    /**
     * 清空当前节点的上游节点列表
     */
    public void clearUpstreamNode() {
        upstreamNodes.clear();
    }
    /**
     * 移除当前节点上游节点列表中指定节点
     * @param nodeBox 指定的节点
     * @return 移除成功与否
     */
    public boolean removeUpstreamNodeItem(NodeBox nodeBox) {
        return upstreamNodes.remove(nodeBox);
    }
    /**
     * 清空当前节点的下游节点列表
     */
    public void clearDownstreamNode() {
        downstreamNodes.clear();
    }
    /**
     * 移除当前节点下游节点列表中指定节点
     * @param nodeBox 指定的节点
     * @return 移除成功与否
     */
    public boolean removeDownstreamNodeItem(NodeBox nodeBox) {
        return downstreamNodes.remove(nodeBox);
    }
    /**
     * 设置节点标签
     * @param label 节点标签
     */
    public void setLabel(String label) {
        this.label = label;
    }
    /**
     * 获取当前节点标签
     * @return 节点标签值
     */
    public String getLabel() {
        return label;
    }
    /**
     * 设置当前节点ID
     * @param id 节点ID
     */
    public void setID(String id) {
        this.id = id;
    }
    /**
     * 获取当前节点ID
     * @return 节点ID
     */
    public String getID() {
        return id;
    }
    /**
     * 获取当前节点中心定位
     * @return 节点中心定位数组，第一个为横标，第二个为纵标
     */
    public double[] getCenter() {
        return new double[] {box_x + width/2, box_y + height/2};
    }
    /**
     * 绘制当前节点图形到指定Graphics2D对象。
     * @param graphics 待绘制的Graphics2D对象
     * @param filled 填充还是描边，true为填充
     */
    public void paint(Graphics2D graphics, boolean filled) {
        Color olc = graphics.getColor();
        if (filled) {
            graphics.setColor(filledColor);
            if (filledColor.getAlpha() == 0) return;
            graphics.fillRoundRect(box_x, box_y, width, height, width/widthToD, width/widthToD);
        } else {
            graphics.setColor(sideColor);
            if (sideColor.getAlpha() == 0) return;
            graphics.drawRoundRect(box_x, box_y, width, height, width/widthToD, width/widthToD);
        }
        graphics.setColor(olc);
    }

}
