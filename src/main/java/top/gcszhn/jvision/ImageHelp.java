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
 * General-purpose image processing tools that provide general functions such as
 * image IO for specific businesses.
 * 
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class ImageHelp {
    /**
     * Font family in the system.
     */
    private static HashMap<String, HashMap<Integer, String>> systemFontMap = new HashMap<>();
    static {
        try {
            loadSystemFontMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the width of a string in a specific font
     * 
     * @param font   font which is used to calculate the width of the string
     * @param string string
     * @return width of the string in the font
     */
    public static int getStringFontWidth(Font font, String string) {
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
        return bi.createGraphics().getFontMetrics(font).stringWidth(string);
    }

    /**
     * Save RGB mode images as jpg images
     * 
     * @param image    buffered image object
     * @param filename jpg file name
     * @param dpi      Image DPI, unit is pixel/inch
     * @throws IOException
     */
    public static void saveAsJPEG(BufferedImage image, String filename, int dpi) throws IOException {
        saveAsJPEG(image, new FileOutputStream(filename), dpi);
    }

    /**
     * Save RGB mode images as jpg images
     * 
     * @param image buffered image object
     * @param fos   FileOutputStream object for output file
     * @param dpi   Image DPI, unit is pixel/inch
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveAsJPEG(BufferedImage image, FileOutputStream fos, int dpi)
            throws FileNotFoundException, IOException {
        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersBySuffix("jpg"); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            // 调整图片质量
            writeParam.setCompressionQuality(1f);
            IIOMetadata data = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), writeParam);
            Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", "" + dpi);
            jfif.setAttribute("Ydensity", "" + dpi);
            jfif.setAttribute("resUnits", "1");// density is dots per inch，如果没有设置会无效
            data.setFromTree("javax_imageio_jpeg_image_1.0", tree);// 将tree的内容保存回data，两者无映射关系需此操作，see:
                                                                   // http://www.voidcn.com/article/p-zdkeyptk-bts.html
            // 输出图片
            ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, data), writeParam);
            ios.close();
        }
    }

    /**
     * Export EpsGraphics2D drawing results as an EPS document in vector graphics
     * format
     * 
     * @param eps        EpsGraphics2D object
     * @param outputfile EPS document output file name.
     * @throws IOException
     */
    public static void saveAsEPS(EpsGraphics2D eps, String outputfile) throws IOException {
        FileWriter fos = new FileWriter(outputfile);
        fos.write(eps.toString());
        fos.close();
    }

    /**
     * Outputs the BuffferedImage object as an EPS document, but does not change the
     * bitmap characteristics of the image itself.
     * 
     * @param image      BufferedImage object to be outputted as an EPS document.
     * @param outputfile EPS document output file name.
     * @throws IOException IOException.
     */
    public static void saveAsEPS(BufferedImage image, String outputfile) throws IOException {
        EpsGraphics2D epsg2d = new EpsGraphics2D();
        epsg2d.drawImage(image, -1, -1, null);
        FileWriter fos = new FileWriter(outputfile);
        fos.write(epsg2d.toString());
        fos.close();

    }

    /**
     * Output the RGB mode BufferImage as png, and set the dpi.
     * 
     * @param image BufferImage object to be output
     * @param fos   FileOutputStream object to output
     * @param dpi   image dpi
     * @throws IIOInvalidTreeException
     * @throws IOException
     */
    public static void saveAsPNG(BufferedImage image, FileOutputStream fos, int dpi)
            throws IIOInvalidTreeException, IOException {
        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName("png"); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
                    .createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported())
                continue;

            double inch2cm = 2.54;
            double dotsPerMilli = 1.0 * dpi / 10 / inch2cm;
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
     * Get all available system font families.
     * 
     * @return A list of font families.
     */
    public static String[] getSystemFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getAvailableFontFamilyNames();
    }

    public static HashMap<String, HashMap<Integer, String>> getSystemFontMap() {
        return systemFontMap;
    }

    /**
     * Load the system fonts into a map.
     * 
     * @throws FontFormatException FontFormatException
     * @throws IOException         IOException
     * @throws Exception           Exception
     */
    public static void loadSystemFontMap() throws FontFormatException, IOException, Exception {
        String fontdir = null;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            fontdir = "C:/Windows/Fonts";
        } else if (os.contains("linux")) {
            fontdir = "/usr/share/fonts/";
        } else {
            throw new Exception("Unsupport Operation System");
        }
        File[] fontFiles = new File(fontdir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith("ttf");
            }
        });
        for (File fontfile : fontFiles) {
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

    /**
     * Get the font file name of the specified font family and style.
     * 
     * @param family Font family name.
     * @param style  Font style. It can be Font.PLAIN, Font.BOLD, Font.ITALIC,
     *               Font.BOLD + Font.ITALIC.
     * @return Font file name of the specified font family and style.
     *         Returns null if the font family is not found.
     */
    public static String getFontFileName(String family, int style) {
        if (systemFontMap.containsKey(family)) {
            return systemFontMap.get(family).get(style);
        } else {
            return null;
        }
    }

    /**
     * Get java.awt.Color object from RGB color code.
     * 
     * @param r Red color code.
     * @param g Green color code.
     * @param b Blue color code.
     * @return java.awt.Color object.
     */
    public static Color getColor(int r, int g, int b) {
        return new Color(r, g, b);
    }

    /**
     * Get java.awt.Color object from hex color string.
     * 
     * @param hex Hex color string.
     * @return java.awt.Color object.
     */
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
