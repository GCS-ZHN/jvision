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
import java.awt.Font;
import java.awt.Graphics2D;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.csv.CSVRecord;

import lombok.Getter;
import lombok.Setter;
import top.gcszhn.jvision.Constant;
import top.gcszhn.jvision.JvisionException;
import top.gcszhn.jvision.Stage;
import top.gcszhn.jvision.tools.BasicTool;
import top.gcszhn.jvision.tools.CreateGraphics;
import top.gcszhn.jvision.tools.DrawTool;

/**
 * 绘制环状柱形图的业务实现类
 * 
 * @version 2.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class CircularHistogram implements JChart {
    private static final long serialVersionUID = 202207132326054L;
    private @Getter @Setter String fontFamily = Constant.DEFAULT_FONT_FAMILY;
    private @Getter @Setter int fontStyle = Constant.DEFAULT_FONT_STYLE;
    private @Getter int width;
    private @Getter int height;
    /** Background color */
    private Color bgColor = Constant.DEFAULT_BACKGROUND_COLOR;
    /**
     * 字体颜色，默认为黑色（0,0,0）
     */
    private Color fontColor = Constant.DEFAULT_FONT_COLOR;
    /**
     * 总角度为340°, 但应满足totalAngle + 2*angle 不超过360，否则会对totalAngle自动调整
     */
    private double totalAngle = 340;
    /**
     * 颜色对象数组，用于对不同标准的上色
     */
    private @Getter Color[] barColorSet = {
            new Color(128, 0, 128), // 紫色 #800080，最外圈颜色
            new Color(251, 188, 5), // 黄色 #FBBC05，次外圈颜色
            new Color(66, 133, 244), // 蓝色 #4285F4
            new Color(234, 67, 53), // 红色 #EA4335
            new Color(189, 183, 107), // 深卡其布 #BDB76B
            new Color(107, 142, 35), // 深绿 #6B8E23
            new Color(135, 206, 250), // 淡蓝色 #87CEFA
            new Color(186, 186, 186)// 灰色 #BABABA
    };
    /**
     * 图例文本字符串组，第一个元素为图例标题，其余为各项文本
     */
    private String[] legendTextSet = null;
    /**
     * max value of the data set
     */
    private double maxValue = 40;
    /**
     * 2D绘图对象，用于绘制图形
     */
    private Graphics2D graphics = null;
    /**
     * 输入数据存储ArrayList，元素类型为Object[]
     * Object[]对象length固定为6，依次为String, int, int, int, int, int
     */
    private ArrayList<Object[]> criteriaList = new ArrayList<>();
    /**
     * 对jpg图像进行dpi设置，单位为像素/英寸
     */
    private int dpi = 360;

    /**
     * 设置jpg图像的输出dpi，单位为像素/英寸，包括纵向和横向dpi
     * 
     * @param dpi dpi值，要求为正整数
     */
    public void setDpi(Integer dpi) {
        if (dpi <= 0) {
            System.err.println("dpi should be more than 0.");
            return;
        }
        this.dpi = dpi;
    }

    /**
     * 设置图例文本
     * 
     * @param args 新的图例文本字符串组
     * @throws JvisionException
     */
    public void setLegendTextSet(Iterator<String> args, int length) throws JvisionException {
        if (legendTextSet == null) {
            try {
                legendTextSet = new String[length];
                for (int i = 0; i < length; i++) {
                    legendTextSet[i] = args.next();
                }
            } catch (NoSuchElementException e) {
                throw new JvisionException("Load legend failed", e, Stage.DATA_LOADING);
            }
        }
    }
    /**
     * 设置图例文件
     * 
     * @param length 输入未定义图例时自定设置图例文本
     */
    public void setLegendTextSet(Integer length) {
        if (legendTextSet == null) {
            legendTextSet = new String[length];
            legendTextSet[0] = "Label";
            for (int i = 1; i < length; i++) {
                legendTextSet[i] = "Label " + i;
            }
        }
    }

    /**
     * 设置柱形最大长度所表示的特征值，至少是特征值的最大值，当设定值小于输入的最大值，会自动变为最大值而使得设置失效
     * 
     * @param max 柱形最大长度所表示的新特征值，要求是正数
     */
    public void setMaxValue(Double max) {
        if (max <= 0) {
            System.err.println("Max value should be more than 0.0.");
            return;
        }
        maxValue = max;
    }

    /**
     * 自定义设置总角度，单位为度
     * 
     * @param ag 新的总角度值，要求在0.0-360.0之间，但应满足totalAngle + 2*angle 不超过360，
     *           否则会对totalAngle自动调整，其中angle指的是旋转步长。这在cutoff值较小或输入数量较小时会发生
     */
    public void setTotalAngle(Double ag) {
        if (ag <= 0 || ag > 360) {
            System.err.println("Total angle should between 0.0 (not included) and 360.0 degree.");
            return;
        }
        totalAngle = ag;
    }

    /**
     * 设置背景颜色
     * 
     * @param color 十六进制颜色
     */
    public void setBackgroundColor(String color) {
        bgColor = Color.decode(color);
    }

    /**
     * 设置字体颜色
     * 
     * @param color 十六进制颜色
     */
    public void setFontColor(String color) {
        fontColor = Color.decode(color);
    }

    /**
     * 对图配色进行自定义设置
     * 
     * @param colorSet 输入的颜色字符串组，格式为#CC00FF
     */
    public void setBarColorSet(String[] colorSet) {
        try {
            for (int i = 0; i < barColorSet.length & i < colorSet.length; i++)
                barColorSet[i] = Color.decode(colorSet[i]);
        } catch (NumberFormatException ex) {
            System.err.println("Ilegal hexadecimal string! it should be like \"#CC00FF\"");
        }
    }

    /**
     * 对数组进行排序，仅对内部数组使用
     * 
     * @param arraylist 待排序数组
     * @param order     boolean值，true为升序，false为降序
     * @return 排序后产生的ArrayList
     */
    private static ArrayList<Object[]> sorted(ArrayList<Object[]> arraylist, boolean order) {
        ArrayList<Object[]> res = new ArrayList<Object[]>();
        @SuppressWarnings("unchecked")
        ArrayList<Object[]> list = (ArrayList<Object[]>) arraylist.clone();
        int length = list.size();
        if (length > 0) {
            for (int index = 0; index < length; index++) {
                Object[] value = list.get(0);
                for (Object[] e : list) {
                    if (order) {
                        if ((double) value[1] > (double) e[1])
                            value = e;
                    } else {
                        if ((double) value[1] < (double) e[1])
                            value = e;
                    }
                }
                res.add(value);
                list.remove(list.indexOf(value));
            }
        }
        return res;
    }

    @Override
    public void loadData(String file) throws JvisionException {
        loadData(file, true, null, false);
    }

    /**
     * 加载数据
     * 
     * @param file     输入文件名
     * @param hasTitle 是否包含标题
     * @param cutoff   筛选值，取前cutoff名保留
     * @throws FileNotFoundException 文件缺失异常
     * @throws IOException           输入输出异常
     */
    public void loadData(String file, Boolean hasTitle, Integer cutoff, boolean isSorted) throws JvisionException {
        if (cutoff <= 1) throw new JvisionException("Cutoff should be more than 1.", null, Stage.DATA_LOADING);
        Iterable<CSVRecord> records;
        try {
            records = BasicTool.readCSV(file);
        } catch (IOException e) {
            throw new JvisionException("Read CSV file error.", e, Stage.DATA_LOADING);
        }
        for (CSVRecord record: records) {
            if (record.size() < 2) {
                throw new JvisionException("Input data should at least two columns.", null, Stage.DATA_LOADING);

            }
            if (hasTitle) {
                hasTitle = false;
                setLegendTextSet(record.iterator(), record.size());
                continue;
            }
            setLegendTextSet(record.size());
            String label = record.get(0);
            double Ct = 0.0;
            Object[] criteria = new Object[record.size() + 1];
            criteria[0] = label;
            for (int i = 1; i < record.size(); i++) {
                try {
                    double curr = Double.parseDouble(record.get(i));
                    Ct += curr;
                    criteria[i + 1] = curr;
                } catch (NumberFormatException ex) {
                    throw new JvisionException("Input data should be numeric.", null, Stage.DATA_LOADING);
                }
            }
            criteria[1] = Ct;
            criteriaList.add(criteria);
        }

        if (isSorted)
            criteriaList = sorted(criteriaList, false);
        if (cutoff != null) {
            if (cutoff > criteriaList.size()) {
                System.err.println("cutoff value is more than all data and will be reset as " + criteriaList.size());
                cutoff = criteriaList.size();
            }
            ArrayList<Object[]> selectList = new ArrayList<>();
            for (int index = 0; index < cutoff && index < criteriaList.size(); index++)
                selectList.add(criteriaList.get(index));
            criteriaList = selectList;
        }
    }

    /**
     * 对Graphics2D对象进行绘制环形柱状图，Grraphics2D类不同子类绘制结果类型不同
     * 
     * @param baseR    绘制图最内环半径，为基础半径
     * @param fontSize 标签字体字号
     * @param width    画布宽度
     * @param height   画布高度
     * @param angle    旋转绘制角度步长，单位为度，正数为顺时针旋转
     * @throws Exception 相关异常
     */
    private void draw(int baseR, int fontSize, int barWidth, int width, int height, double angle) {
        int center_x = width - height / 2;
        int center_y = height / 2;
        double baseAngle = 450 - totalAngle;
        /*
         * clear graphics
         */
        graphics.setBackground(bgColor);
        graphics.clearRect(0, 0, width, height);
        /*
         * add multiple layers circle histogram
         */
        int r = baseR;
        Object[] obj = criteriaList.get(0);
        for (int i = 0; i < obj.length - 2; i++) {
            for (int index = 0; index < criteriaList.size(); index++) {
                obj = criteriaList.get(index);
                double currentValue = (double) obj[obj.length - 1 - i];
                int length = (int) (currentValue * baseR / maxValue);
                if (length > baseR) {
                    System.err.println(length + "\t" + currentValue);
                    length = baseR;
                }
                int colorindex = obj.length - 3 - i;// 最大圈颜色用索引最小值
                while (colorindex >= barColorSet.length)
                    colorindex -= barColorSet.length;
                graphics.setColor(barColorSet[colorindex]);
                DrawTool.drawSimpleBar(
                        graphics,
                        center_x,
                        center_y,
                        baseAngle + index * angle,
                        r,
                        barWidth,
                        length);
            }
            r += baseR;
        }
        /*
         * add center white circle
         */
        int ir = (int) (Math.sqrt(1.0 * baseR * baseR + barWidth * barWidth / 4.0) + 0.5);
        graphics.setColor(bgColor);
        graphics.fillOval(center_x - ir, center_y - ir, 2 * (int) ir, 2 * ir);
        /*
         * add sample label
         */
        r += baseR / 10;// 定义外围标签与图间距为r/50
        for (int index = 0; index < criteriaList.size(); index++) {
            double currentAngle = 360 - totalAngle + index * angle;
            graphics.setColor(fontColor);
            DrawTool.rotateText(
                    graphics,
                    (String) criteriaList.get(index)[0],
                    center_x,
                    center_y,
                    currentAngle, // (currentAngle <= 90 || currentAngle>=270)?currentAngle:currentAngle - 180,
                    r, 
                    "r",
                    "m", // (currentAngle <= 90 || currentAngle>=270)?"r":"l",
                    false);
        }
        /*
         * add legend
         */
        int legendX = width / 25;
        int legendY = height / 15;
        fontSize = height / 50;
        Font legendFont = new Font(getFontFamily(), getFontStyle(), fontSize);// 用默认字体
        graphics.setFont(legendFont);
        graphics.setColor(fontColor);
        DrawTool.drawText(graphics, legendTextSet[0], legendX, legendY, "l", "m");
        graphics.setFont(new Font(getFontFamily(), getFontStyle(), fontSize * 2 / 3));
        for (int i = 0; i < obj.length - 2; i++) {
            int colorIndex = i;
            while (colorIndex >= barColorSet.length)
                colorIndex -= barColorSet.length;
            graphics.setColor(barColorSet[colorIndex]);
            DrawTool.drawSimpleBar(
                    graphics,
                    legendX,
                    legendY + (i + 1) * fontSize * 5 / 4,
                    -90,
                    0,
                    fontSize,
                    fontSize);
            DrawTool.drawText(
                    graphics,
                    legendTextSet[i + 1],
                    legendX + fontSize * 3 / 2,
                    legendY + (i + 1) * fontSize * 5 / 4,
                    "l",
                    "m");
        }
    }

    /**
     * 绘制环形柱状图
     * 
     * @param file     输出文件基础名，与维度，类型一起构成完整文件名
     * @param autoSize 是否自动调整尺寸，仅对jpg格式有效
     * @throws JvisionException
     * @throws IOException      输入输出异常
     * @throws Exception        其他异常
     */
    public void draw(String file, Boolean autoSize) throws JvisionException {
        String type = BasicTool.getFileExtName(file);
        int cutoff = criteriaList.size();
        double angle = totalAngle / (cutoff - 1);
        if (2 * angle + totalAngle > 360)
            System.err.println(
                    "Current total angle will be adjusted automatically because of \"2×step angle + total angle > 360\".");
        while (2 * angle + totalAngle > 360) {// 对总角度进行自适应，使其能够直观看到起止位置
            totalAngle = totalAngle - 10;
            angle = totalAngle / (criteriaList.size() - 1);
        }
        int fontSize = 56;
        int barWidth = fontSize / 2;
        double widthPerHeight = 4.0 / 3;
        int heightPerR = 2 * criteriaList.get(0).length + 3;
        int r = (int) Math.round(barWidth * 180 / (1.5 * angle * Math.PI));
        int height = heightPerR * r;
        int width = (int) (height * widthPerHeight);
        if (width > 23000 || !autoSize) {
            width = 23000;
            height = (int) (width / widthPerHeight);
            r = height / heightPerR;
            barWidth = (int) Math.round(Math.PI / 180 * angle * r * 1.5);
            fontSize = barWidth * 2;
        }
        type = type.toLowerCase();
        if (!type.equals("jpg")) {
            width = 2000;
            height = (int) (width / widthPerHeight);
            r = height / heightPerR;
            barWidth = (int) Math.round(Math.PI / 180 * angle * r * 1.5);
            fontSize = barWidth * 2;
        }
        if (barWidth % 2 != 0)
            barWidth++;
        if (fontSize < 1)
            fontSize = 1;// 保证最小字号
        if (fontSize > 20)
            fontSize = 20;// 防止字号过大溢出
        if (barWidth < 1)
            barWidth = 1;// 保证至少一像素宽度
        /*
         * 实例化当前类，配置字体
         */

        Font awtFont = new Font(getFontFamily(), getFontStyle(), fontSize);
        this.width = width;
        this.height = height;
        CreateGraphics cg = new CreateGraphics(width, height, type, file);
        graphics = cg.getGraphics();
        graphics.setFont(awtFont);
        draw(r, fontSize, barWidth, width, height, angle);
        cg.setJpegDPI(dpi);
        cg.saveToFile();
    }

    @Override
    public void draw(String file) throws JvisionException {
        draw(file, false);
    }
}
