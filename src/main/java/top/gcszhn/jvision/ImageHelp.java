package top.gcszhn.jvision;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import org.w3c.dom.Element;
import org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D;

/**
 * 通用图像处理工具，为具体业务提供图像IO等一般性功能。
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class ImageHelp {
    private static HashMap <String, HashMap<Integer, String>> systemFontMap = new HashMap<>();

    static {
        try {
            loadSystemFontMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 返回特定字体下字符串宽度
     * @param font 字体
     * @param string 字符串
     * @return 宽度
     */
    public static int getStringFontWidth(Font font, String string) {
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        return bi.createGraphics().getFontMetrics(font).stringWidth(string);
    }
    /**
     * 将RGB模式的image保存为jpg格式图片
     * @param image BufferedImage对象
     * @param filename 输出文件名，建议以jpg/jpeg结尾以便查看器查看
     * @param dpi 图片水平/垂直分辨率，单位为像素/英寸
     * @throws IOException 输入输出异常
     */
    public static void saveAsJPEG(BufferedImage image, String filename, int dpi) throws IOException {
        saveAsJPEG(image, new FileOutputStream(filename), dpi);
    }
    /**
     * 将RGB模式的image保存为jpg格式图片
     * @param image 图片对象
     * @param fos 文件输出流
     * @param dpi 图像DPI，仅限于jpg
     * @throws FileNotFoundException 文件缺失异常
     * @throws IOException 输入输出异常
     */
    public static void saveAsJPEG(BufferedImage image, FileOutputStream fos, int dpi) throws FileNotFoundException, IOException {
        for (Iterator <ImageWriter> iw = ImageIO.getImageWritersBySuffix("jpg"); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            //调整图片质量
            writeParam.setCompressionQuality(1f);
            IIOMetadata data = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), writeParam);
            Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", "" + dpi);
            jfif.setAttribute("Ydensity", "" + dpi);
            jfif.setAttribute("resUnits", "1");// density is dots per inch，如果没有设置会无效
            data.setFromTree("javax_imageio_jpeg_image_1.0", tree);//将tree的内容保存回data，两者无映射关系需此操作，see: http://www.voidcn.com/article/p-zdkeyptk-bts.html
            //输出图片
            ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, data), writeParam);
            ios.close();
        }
    }
    /**
     * 将EpsGraphics2D绘图结果输出为EPS文档，为矢量图格式
     * @param eps 待输出对象
     * @param outputfile 输出文件名
     * @throws IOException 输入输出异常
     */
    public static void saveAsEPS(EpsGraphics2D eps, String outputfile) throws IOException {
        FileWriter fos = new FileWriter(outputfile);
        fos.write(eps.toString());
        fos.close();
    }
    /**
     * 将BuffferedImage对象输出为EPS文档，但不改变image本身的位图特征。
     * @param image 图像对象
     * @param outputfile 输出文件名
     * @throws IOException 输入输出异常
     */
    public static void saveAsEPS(BufferedImage image, String outputfile) throws IOException {
        EpsGraphics2D epsg2d = new EpsGraphics2D();
        epsg2d.drawImage(image, -1, -1, null);
        FileWriter fos = new FileWriter(outputfile);
        fos.write(epsg2d.toString());
        fos.close();
    
    }
    /**
     * 将RGB模式BufferImage输出文png，并设定dpi
     * @param image 待输出BufferImage对象
     * @param fos 文件输出流
     * @param dpi 输出dpi
     * @throws IIOInvalidTreeException IIOMeta对象解析异常
     * @throws IOException 输入输出异常
     */
    public static void saveAsPNG(BufferedImage image, FileOutputStream fos, int dpi) throws IIOInvalidTreeException, IOException {
        for (Iterator <ImageWriter> iw = ImageIO.getImageWritersByFormatName("png"); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly()||!metadata.isStandardMetadataFormatSupported()) continue;
            
            double inch2cm = 2.54;
            double dotsPerMilli = 1.0 *dpi/ 10 / inch2cm;
            IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");  
            horiz.setAttribute("value", Double.toString(dotsPerMilli));
            IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");  
            vert.setAttribute("value", Double.toString(dotsPerMilli));
            IIOMetadataNode dim = new IIOMetadataNode("Dimension");  
            dim.appendChild(horiz);
            dim.appendChild(vert);
            IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");  
            root.appendChild(dim);
            metadata.mergeTree("javax_imageio_1.0", root);
            
            ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
            writer.setOutput(ios);
            writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
            ios.close();
        }
    }
    /**
     * 获取系统所含所有字体的family名称
     * @return 代表所有字体family名称的字符串组
     */
    public static String[] getSystemFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getAvailableFontFamilyNames();
    }

    public static HashMap<String, HashMap<Integer, String>> getSystemFontMap() {
        return systemFontMap;
    }

    public static void loadSystemFontMap() throws FontFormatException, IOException, Exception {
        String fontdir = null;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            fontdir = "C:/Windows/Fonts";
        } else if (os.contains("linux")) {
            fontdir = "/usr/share/fonts/";
        } else {
            throw new Exception ("Unsupport Operation System");
        }
        File[] fontFiles = new File(fontdir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith("ttf");
            }
        });
        for (File fontfile: fontFiles) {
            String filename = fontfile.getAbsolutePath();
            Font font = Font.createFont(Font.TRUETYPE_FONT, fontfile);
            String family = font.getFamily();
            String fontname = font.getFontName().toLowerCase();
            systemFontMap.putIfAbsent(family, new HashMap<>());
            if (fontname.contains("bold") && fontname.contains("italic")) {
                systemFontMap.get(family).put(Font.BOLD + Font.ITALIC, filename);
            } else if (fontname.contains("bold")) {
                systemFontMap.get(family).put(Font.BOLD, filename);
            } else if (fontname.contains("italic")) {
                systemFontMap.get(family).put(Font.ITALIC, filename);
            } else {
                systemFontMap.get(family).put(Font.PLAIN, filename);
            }
        }
    }
    public static String getFontFileName(String family, int style) {
        if (systemFontMap.containsKey(family)) {
            return systemFontMap.get(family).get(style);
        } else {
            return null;
        }
    }
    public static Color getColor(int r, int g, int b) {
        return new Color(r, g, b);
    }
    public static Color getColor(String hex) {
        try {
            return new Color(
                Integer.parseInt(hex.substring(1, 3), 16), 
                Integer.parseInt(hex.substring(3, 5), 16), 
                Integer.parseInt(hex.substring(5, 7), 16));
        } catch (Exception e) {
            throw new RuntimeException("Invalid color hex string: " + hex);
        }

    }
}
