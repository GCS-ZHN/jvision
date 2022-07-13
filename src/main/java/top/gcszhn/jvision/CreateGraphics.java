package top.gcszhn.jvision;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

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
    private Graphics2D graphics;
    /**
     * 输出文件类型，暂时支持pdf、eps、jpg
     */
    private final String fileType;
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
    public CreateGraphics(int width, int height, String type, String outfilename) throws DocumentException, IOException, Exception {
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
                final BaseFont PDF_DEFAULT_FONT = BaseFont.createFont();
                FontMapper fontMapper = new FontMapper() {
                    @Override
                    public BaseFont awtToPdf(Font font) {
                        try {
                            String pdfFontName = ImageHelp.getFontFileName(font.getFamily(), font.getStyle());
                            if (pdfFontName==null) return PDF_DEFAULT_FONT;
                            BaseFont baseFont = BaseFont.createFont(pdfFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                            return baseFont;
                        } catch (Exception ex) {
                            return PDF_DEFAULT_FONT;
                        }
                    }
                    @Override
                    public Font pdfToAwt(BaseFont bf, int i) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                };
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputFileName));
                document.open();
                PdfContentByte pcb = writer.getDirectContent();
                graphics = pcb.createGraphics(width, height, fontMapper);
                break;
            }
            case "eps":{
                graphics = new EpsGraphics2D();
                break;
            }
            
            default:throw new Exception("Unsupport File Type: "+ type);
        }
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);//文字抗锯齿
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//画图抗锯齿
        
    }
    /**
     * 获取构建的Graphics2D对象
     * @return 构建的Graphics2D对象
     */
    public Graphics2D getGraphics2D() {
        return graphics;
    }
    /**
     * 设置jpg输出格式的dpi
     * @param value jpg输出格式的dpi
     */
    public void setJpegDPI(int value) {
        dpi = value;
    }
    /**
     * 将绘图结果输出到文件
     * @throws IOException 输入输出异常
     * @throws Exception 其他异常
     */
    public void saveToFlie() throws IOException, Exception {
        switch(fileType) {
            case "jpg": {
                graphics.dispose();
                ImageHelp.saveAsJPEG(image, outputFileName, dpi);
                break;
            }
            case "png": {
                graphics.dispose();
                ImageHelp.saveAsPNG(image, new FileOutputStream(outputFileName), dpi);
                break;
            }
            case "pdf": {
                graphics.dispose();
                document.close();
                break;
            }
            case "eps": {
                ImageHelp.saveAsEPS((EpsGraphics2D) graphics, outputFileName);
                break;
            }
            default:throw new Exception("Unsupport File Type: "+ fileType);
        }
    }
}
