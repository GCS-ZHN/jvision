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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import lombok.Getter;
import top.gcszhn.jvision.JvisionException;
import top.gcszhn.jvision.Stage;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D;

/**
 * 创建通用绘图画布的业务实现类，服务于其他具体绘图业务
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class CreateGraphics {
    /**
     * 用于绘图的Graphics 2D对象
     */
    private @Getter Graphics2D graphics;
    /**
     * 输出文件类型，暂时支持pdf、eps、jpg
     */
    private @Getter final String fileType;
    /**
     * 输出文件名
     */
    private final String outputFileName;
    /**
     * 输出DPI，仅对jpg格式有效
     */
    private int dpi = 360;
    /**
     * BufferedImage对象，仅在输出jpg时使用
     */
    private BufferedImage image = null;
    /**
     * Document对象，仅在输出pdf时使用
     */
    private Document document = null;
    /**
     * 构造方法创建CreateGraphics对象
     * @param width 画布宽度，单位像素
     * @param height 画布高度，单位像素
     * @param type 输出类型，仅支持pdf/eps/jpg/png
     * @param outfilename 输出文件名
     * @throws DocumentException pdf文档异常
     * @throws IOException 输入输出异常
     * @throws Exception 其他异常
     */
    public CreateGraphics(int width, int height, String type, String outfilename) throws JvisionException {
        fileType = type;
        outputFileName = outfilename;
        switch (fileType) {
            case "jpg":{
                //RGBA模式不可以编码jpeg，https://community.oracle.com/message/5387869
                image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                graphics = image.createGraphics();
                break;
            }
            case "png":{
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                graphics = image.createGraphics();
                //下面实现绘制透明背景png
                image = graphics.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
                graphics.dispose();
                graphics = image.createGraphics();
                break;
            }
            case "pdf":{
                document = new Document(new Rectangle(width, height));
                BaseFont PDF_DEFAULT_FONT;
                try {
                    PDF_DEFAULT_FONT = BaseFont.createFont();
                } catch (DocumentException|IOException e) {
                    throw new JvisionException("Create pdf font failed", e, Stage.GRAHPIC_INITIALIZATION);
                }
                FontMapper fontMapper = new FontMapper() {
                    @Override
                    public BaseFont awtToPdf(Font font) {
                        try {
                            String pdfFontName = ImageTool.getFontFileName(font.getFamily(), font.getStyle());
                            if (pdfFontName==null) return PDF_DEFAULT_FONT;
                            BaseFont baseFont = BaseFont.createFont(pdfFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                            return baseFont;
                        } catch (Exception ex) {
                            return PDF_DEFAULT_FONT;
                        }
                    }
                    @Override
                    public Font pdfToAwt(BaseFont bf, int i) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
                PdfWriter writer;
                try {
                    writer = PdfWriter.getInstance(document, new FileOutputStream(outputFileName));
                } catch (DocumentException|IOException e) {
                    throw new JvisionException("Create pdf writer failed", e, Stage.GRAHPIC_INITIALIZATION);
                }
                document.open();
                PdfContentByte pcb = writer.getDirectContent();
                graphics = pcb.createGraphics(width, height, fontMapper);
                break;
            }
            case "eps":{
                graphics = new EpsGraphics2D();
                break;
            }
            
            default:throw new JvisionException("Unsupport File Type: "+ type, null, Stage.GRAHPIC_INITIALIZATION);
        }
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);//文字抗锯齿
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//画图抗锯齿
        
    }
    /**
     * 设置jpg输出格式的dpi
     * @param dpi jpg输出格式的dpi
     */
    public void setJpegDPI(int dpi) {
        this.dpi = dpi;
    }
    /**
     * 将绘图结果输出到文件
     * @throws IOException 输入输出异常
     * @throws Exception 其他异常
     */
    public void saveToFile() throws JvisionException {
        try {
            switch(fileType) {
                case "jpg": {
                    graphics.dispose();
                    ImageTool.saveAsJPEG(image, outputFileName, dpi);
                    break;
                }
                case "png": {
                    graphics.dispose();
                    ImageTool.saveAsPNG(image, new FileOutputStream(outputFileName), dpi);
                    break;
                }
                case "pdf": {
                    graphics.dispose();
                    document.close();
                    break;
                }
                case "eps": {
                    ImageTool.saveAsEPS((EpsGraphics2D) graphics, outputFileName);
                    break;
                }
            }
        } catch (Exception e) {
            throw new JvisionException("Save graphic to file failed", e, Stage.GRAHPIC_SERIALIZATION);
        }

    }
}
