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
package top.gcszhn.jvision.tools;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Arc2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import top.gcszhn.jvision.JvisionException;
import top.gcszhn.jvision.Stage;

public class DrawTool {

    public static void drawText(
            Graphics2D graphics2D,
            String label,
            double x,
            double y,
            String h_mode,
            String v_mode) {
        rotateText(graphics2D, label, x, y, 0, 0, h_mode, v_mode);
    }

    /**
     * Draw text with rotation
     * 
     * @param graphics2D   Graphics2D object， Used to draw text
     * @param text         Text to be drawn
     * @param centerX      X coordinate of the rotation center
     * @param centerY      Y coordinate of the rotation center
     * @param rotateDegree Rotation angle，in degrees
     * @param rotateR      Radius of the rotation circle
     * @param h_mode       Horizontal alignment mode，"l"：left，"m"：center，"r"：right
     * @param v_mode       Vertical alignment mode，"u"：top，"m"：middle，"d"：bottom
     * @param font         Font
     */
    public static void rotateText(
            Graphics2D graphics2D,
            String text,
            double centerX,
            double centerY,
            double rotateDegree,
            double rotateR,
            String h_mode,
            String v_mode) {
        rotateText(graphics2D, text, centerX, centerY, rotateDegree, rotateR, h_mode, v_mode, false);
    }

    /**
     * Draw text with rotation
     * 
     * @param graphics2D    Graphics2D object， Used to draw text
     * @param text          Text to be drawn
     * @param centerX       X coordinate of the rotation center
     * @param centerY       Y coordinate of the rotation center
     * @param rotateDegree  Rotation angle，in degrees
     * @param rotateR       Radius of the rotation circle
     * @param h_mode        Horizontal alignment mode，"l"：left，"m"：center，"r"：right
     * @param v_mode        Vertical alignment mode，"u"：top，"m"：middle，"d"：bottom
     * @param stringBalance Whether to balance the string，true：balance，false：not
     *                      balance. Balance means the text is kept horizontal
     * @param font          Font
     * @throws Exception
     */
    public static void rotateText(
            Graphics2D graphics2D,
            String text,
            double centerX,
            double centerY,
            double rotateDegree,
            double rotateR,
            String h_mode,
            String v_mode,
            boolean stringBalance) {
        FontMetrics metrics = graphics2D.getFontMetrics();
        float baseline_x;
        float baseline_y;
        int locat_x = (int) (centerX - rotateR), locat_y = (int) centerY;
        if (h_mode.equals("l")) {
            locat_x = (int) (centerX + rotateR);
        }
        switch (v_mode) {
            case "m": {
                baseline_y = locat_y * 1f - metrics.getHeight() / 2f + metrics.getAscent();
                break;
            }
            case "u": {
                baseline_y = locat_y + metrics.getAscent();
                break;
            }
            case "d": {
                baseline_y = locat_y - metrics.getHeight() + metrics.getAscent();
                break;
            }
            default:
                throw new RuntimeException("Ilegal mode symbol: " + v_mode);
        }
        switch (h_mode) {
            case "m": {
                baseline_x = locat_x * 1f - metrics.stringWidth(text) / 2f;
                break;
            }
            case "l": {
                baseline_x = locat_x;
                break;
            }
            case "r": {
                baseline_x = locat_x - metrics.stringWidth(text);
                break;
            }
            default:
                throw new RuntimeException("Ilegal mode symbol: " + h_mode);
        }
        if (rotateDegree != 0) {
            graphics2D.rotate(rotateDegree * Math.PI / 180, centerX, centerY);
            if (stringBalance)
                graphics2D.rotate(-rotateDegree * Math.PI / 180, locat_x, locat_y);
        }
        graphics2D.drawString(text, baseline_x, baseline_y);
        if (rotateDegree != 0) {
            if (stringBalance)
                graphics2D.rotate(rotateDegree * Math.PI / 180, locat_x, locat_y);
            graphics2D.rotate(-rotateDegree * Math.PI / 180, centerX, centerY);
        }
    }

    /**
     * 绘制连接曲线
     * 
     * @param x1   第一个点横坐标
     * @param y1   第一个点纵坐标
     * @param x2   第二个点横坐标
     * @param y2   第二个点纵坐标
     * @param ang  曲线调整角，单位角度
     * @param bond 曲线粗细
     * @param lc   曲线颜色
     * @param dash 曲线线型
     * @throws JvisionException
     */
    public static void drawConnectCurve(Graphics2D graphics, double x1, double y1, double x2, double y2, double ang,
            float bond, boolean solid, float dash) throws JvisionException {
        if (graphics == null)
            throw new JvisionException("Graphics object should not be null", null, Stage.GRAHPIC_PAINTING);
        if (x2 < x1) {// 保证（x1， y1）点在（x2，y2）左侧
            double tmp = x2;
            x2 = x1;
            x1 = tmp;
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        boolean flag = (x1 - x2) * (y1 - y2) < 0; // 判断是否为异号类型，即左下-右上类型
        if (flag) {// 异号类型交换纵标为同号类型画图，再通过下面图像变换为异号类型。
            double tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        double radian = Math.toRadians(ang);
        double w = (x2 - x1) / Math.cos(radian);
        double h = Math.abs(y2 - y1) / (1 - Math.sin(radian));
        double locat_x1 = x1 - w / 2;// 椭圆弧定位点横标
        double locat_y1 = y1;
        double locat_x2 = x2 - w / 2;
        double locat_y2 = y2 - h;
        /*
         * 二段圆弧，注意起止点是单向循环
         */
        Arc2D arc1 = new Arc2D.Double(locat_x1, locat_y1, w, h, 90, ang - 90, Arc2D.OPEN);
        Arc2D arc2 = new Arc2D.Double(locat_x2, locat_y2, w, h, 180 + ang, 90 - ang, Arc2D.OPEN);
        Path2D path = new Path2D.Double(arc1);
        path.append(arc2, true);// 将arc2与arc1收尾相连
        if (flag) {
            path.transform(AffineTransform.getScaleInstance(1d, -1d));// 图像横坐标缩放1倍，纵坐标缩放-1倍，即垂直对称
            path.transform(AffineTransform.getTranslateInstance(0, y1 + y2));// 对称图在画布上面，平移变换入画布
        }

        Stroke obs = graphics.getStroke();
        BasicStroke solidStroke = new BasicStroke(bond);
        BasicStroke dashStroke = new BasicStroke(bond, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
                new float[] { dash }, 0);
        graphics.setStroke(solid ? solidStroke : dashStroke);// 设置粗细
        graphics.draw(path);
        graphics.setStroke(obs);
    }

    /**
     * 绘制简单旋转矩形
     * 
     * @param rotateCenter_x 旋转中心横坐标
     * @param rotateCenter_y 旋转中心纵坐标
     * @param rotateDegree   旋转角度，单位为度，正数为顺时针旋转
     * @param rotateR        旋转半径
     * @param width          矩形宽度
     * @param length         矩形长度
     */
    public static void drawSimpleBar(Graphics2D graphics, double rotateCenter_x, double rotateCenter_y,
            double rotateDegree, double rotateR, int width, int length) {
        if (width % 2 != 0)
            width++;
        int baseX = (int) (rotateCenter_x - width / 2);
        int baseY = (int) (rotateCenter_y + rotateR);
        graphics.rotate(rotateDegree * Math.PI / 180, rotateCenter_x, rotateCenter_y);// 正数为顺时针转形状，也就是逆时针转画布, 旋转一次画一次
        graphics.fillRect(baseX, baseY, width, length);
        graphics.rotate(-rotateDegree * Math.PI / 180, rotateCenter_x, rotateCenter_y);// 因此转回来，方便统计总角度
    }
}
