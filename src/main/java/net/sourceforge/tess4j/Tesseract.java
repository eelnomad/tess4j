/**
 * Copyright @ 2012 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sourceforge.tess4j;

import net.sourceforge.tess4j.util.ImageIOHelper;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.Rectangle;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.*;
import javax.imageio.IIOImage;
import net.sourceforge.tess4j.TessAPI.TessResultRenderer;
import net.sourceforge.tess4j.util.Utils;

/**
 * An object layer on top of <code>TessAPI</code>, provides character
 * recognition support for common image formats, and multi-page TIFF images
 * beyond the uncompressed, binary TIFF format supported by Tesseract OCR
 * engine. The extended capabilities are provided by the
 * <code>Java Advanced Imaging Image I/O Tools</code>. <br /><br /> Support for
 * PDF documents is available through <code>Ghost4J</code>, a <code>JNA</code>
 * wrapper for <code>GPL Ghostscript</code>, which should be installed and
 * included in system path. <br /><br /> Any program that uses the library will
 * need to ensure that the required libraries (the <code>.jar</code> files for
 * <code>jna</code>, <code>jai-imageio</code>, and <code>ghost4j</code>) are in
 * its compile and run-time <code>classpath</code>.
 */
public class Tesseract implements ITesseract {

    private static Tesseract instance;
    private String language = "eng";
    private String datapath = ".";
    private RenderedFormat renderedFormat = RenderedFormat.TEXT;
    private int psm = TessAPI.TessPageSegMode.PSM_AUTO;
    private int pageNum;
    private int ocrEngineMode = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
    private final Properties prop = new Properties();

    private TessAPI api;
    private TessAPI.TessBaseAPI handle;

    private final static Logger logger = Logger.getLogger(Tesseract.class.getName());

    /**
     * Private constructor.
     */
    private Tesseract() {
        System.setProperty("jna.encoding", "UTF8");
    }

    /**
     * Gets an instance of the class library.
     *
     * @return instance
     */
    public static synchronized Tesseract getInstance() {
        if (instance == null) {
            instance = new Tesseract();
        }

        return instance;
    }

    /**
     * Sets tessdata path.
     *
     * @param datapath the tessdata path to set
     */
    @Override
    public void setDatapath(String datapath) {
        this.datapath = datapath;
    }

    /**
     * Sets language for OCR.
     *
     * @param language the language code, which follows ISO 639-3 standard.
     */
    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Sets OCR engine mode.
     *
     * @param ocrEngineMode the OcrEngineMode to set
     */
    @Override
    public void setOcrEngineMode(int ocrEngineMode) {
        this.ocrEngineMode = ocrEngineMode;
    }

    /**
     * Sets page segmentation mode.
     *
     * @param mode the page segmentation mode to set
     */
    @Override
    public void setPageSegMode(int mode) {
        this.psm = mode;
    }

    /**
     * Sets rendered format.
     *
     * @param renderedFormat the renderedFormat to set
     */
    @Override
    public void setRenderedFormat(RenderedFormat renderedFormat) {
        this.renderedFormat = renderedFormat;
//        if (renderedFormat == RenderedFormat.HOCR) {
//            prop.setProperty("tessedit_create_hocr", "1");
//        } else if (renderedFormat == RenderedFormat.PDF) {
//            prop.setProperty("tessedit_create_pdf", "1");
//            prop.setProperty("tessedit_pageseg_mode", "1");
//        } else if (renderedFormat == RenderedFormat.UNLV) {
//            prop.setProperty("tessedit_write_unlv", "1");
//        } else if (renderedFormat == RenderedFormat.BOX) {
//            prop.setProperty("tessedit_create_boxfile", "1");
//        }
    }

    /**
     * Set the value of Tesseract's internal parameter.
     *
     * @param key variable name, e.g., <code>tessedit_create_hocr</code>,
     * <code>tessedit_char_whitelist</code>, etc.
     * @param value value for corresponding variable, e.g., "1", "0",
     * "0123456789", etc.
     */
    @Override
    public void setTessVariable(String key, String value) {
        prop.setProperty(key, value);
    }

    /**
     * Performs OCR operation.
     *
     * @param imageFile an image file
     * @return the recognized text
     * @throws TesseractException
     */
    @Override
    public String doOCR(File imageFile) throws TesseractException {
        return doOCR(imageFile, null);
    }

    /**
     * Performs OCR operation.
     *
     * @param imageFile an image file
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    @Override
    public String doOCR(File imageFile, Rectangle rect) throws TesseractException {
        try {
            return doOCR(ImageIOHelper.getIIOImageList(imageFile), rect);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new TesseractException(e);
        }
    }

    /**
     * Performs OCR operation.
     *
     * @param bi a buffered image
     * @return the recognized text
     * @throws TesseractException
     */
    @Override
    public String doOCR(BufferedImage bi) throws TesseractException {
        return doOCR(bi, null);
    }

    /**
     * Performs OCR operation.
     *
     * @param bi a buffered image
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    @Override
    public String doOCR(BufferedImage bi, Rectangle rect) throws TesseractException {
        try {
            return doOCR(ImageIOHelper.getIIOImageList(bi), rect);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new TesseractException(e);
        }
    }

    /**
     * Performs OCR operation.
     *
     * @param imageList a list of <code>IIOImage</code> objects
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    @Override
    public String doOCR(List<IIOImage> imageList, Rectangle rect) throws TesseractException {
        init();
        setTessVariables();

        try {
            StringBuilder sb = new StringBuilder();

            for (IIOImage oimage : imageList) {
                pageNum++;
                try {
                    setImage(oimage.getRenderedImage(), rect);
                    sb.append(getOCRText());
                } catch (IOException ioe) {
                    // skip the problematic image
                    logger.log(Level.SEVERE, ioe.getMessage(), ioe);
                }
            }

            if (renderedFormat == RenderedFormat.HOCR) {
                sb.insert(0, htmlBeginTag).append(htmlEndTag);
            }

            return sb.toString();
        } finally {
            dispose();
        }
    }

    /**
     * Performs OCR operation. Use <code>SetImage</code>, (optionally)
     * <code>SetRectangle</code>, and one or more of the <code>Get*Text</code>
     * functions.
     *
     * @param xsize width of image
     * @param ysize height of image
     * @param buf pixel data
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @param bpp bits per pixel, represents the bit depth of the image, with 1
     * for binary bitmap, 8 for gray, and 24 for color RGB.
     * @return the recognized text
     * @throws TesseractException
     */
    @Override
    public String doOCR(int xsize, int ysize, ByteBuffer buf, Rectangle rect, int bpp) throws TesseractException {
        init();
        setTessVariables();

        try {
            setImage(xsize, ysize, buf, rect, bpp);
            return getOCRText();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new TesseractException(e);
        } finally {
            dispose();
        }
    }

    /**
     * Initializes Tesseract engine.
     */
    private void init() {
        pageNum = 0;
        api = TessAPI.INSTANCE;
        handle = api.TessBaseAPICreate();
        api.TessBaseAPIInit2(handle, datapath, language, ocrEngineMode);
        api.TessBaseAPISetPageSegMode(handle, psm);
    }

    /**
     * Sets Tesseract's internal parameters.
     */
    private void setTessVariables() {
        Enumeration<?> em = prop.propertyNames();
        while (em.hasMoreElements()) {
            String key = (String) em.nextElement();
            api.TessBaseAPISetVariable(handle, key, prop.getProperty(key));
        }
    }

    /**
     * A wrapper for {@link #setImage(int, int, ByteBuffer, Rectangle, int)}.
     */
    private void setImage(RenderedImage image, Rectangle rect) throws IOException {
        setImage(image.getWidth(), image.getHeight(), ImageIOHelper.getImageByteBuffer(image), rect, image.getColorModel().getPixelSize());
    }

    /**
     * Sets image to be processed.
     *
     * @param xsize width of image
     * @param ysize height of image
     * @param buf pixel data
     * @param rect the bounding rectangle defines the region of the image to be
     * recognized. A rectangle of zero dimension or <code>null</code> indicates
     * the whole image.
     * @param bpp bits per pixel, represents the bit depth of the image, with 1
     * for binary bitmap, 8 for gray, and 24 for color RGB.
     */
    private void setImage(int xsize, int ysize, ByteBuffer buf, Rectangle rect, int bpp) {
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(xsize * bpp / 8.0);
        api.TessBaseAPISetImage(handle, buf, xsize, ysize, bytespp, bytespl);

        if (rect != null && !rect.isEmpty()) {
            api.TessBaseAPISetRectangle(handle, rect.x, rect.y, rect.width, rect.height);
        }
    }

    /**
     * Gets recognized text.
     */
    private String getOCRText() {
        Pointer utf8Text = renderedFormat == RenderedFormat.HOCR ? api.TessBaseAPIGetHOCRText(handle, pageNum - 1) : api.TessBaseAPIGetUTF8Text(handle);
        String str = utf8Text.getString(0);
        api.TessDeleteText(utf8Text);
        return str;
    }

    /**
     * Creates renderers for given formats.
     *
     * @param formats
     * @return
     */
    private TessResultRenderer createRenderers(List<RenderedFormat> formats) {
        TessResultRenderer renderer = null;

        for (RenderedFormat format : formats) {
            switch (format) {
                case TEXT:
                    if (renderer == null) {
                        renderer = api.TessTextRendererCreate();
                    } else {
                        api.TessResultRendererInsert(renderer, api.TessTextRendererCreate());
                    }
                    break;
                case HOCR:
                    if (renderer == null) {
                        renderer = api.TessHOcrRendererCreate();
                    } else {
                        api.TessResultRendererInsert(renderer, api.TessHOcrRendererCreate());
                    }
                    break;
                case PDF:
                    String dataPath = api.TessBaseAPIGetDatapath(handle);
                    if (renderer == null) {
                        renderer = api.TessPDFRendererCreate(dataPath);
                    } else {
                        api.TessResultRendererInsert(renderer, api.TessPDFRendererCreate(dataPath));
                    }
                    break;
                case BOX:
                    if (renderer == null) {
                        renderer = api.TessBoxTextRendererCreate();
                    } else {
                        api.TessResultRendererInsert(renderer, api.TessBoxTextRendererCreate());
                    }
                    break;
                case UNLV:
                    if (renderer == null) {
                        renderer = api.TessUnlvRendererCreate();
                    } else {
                        api.TessResultRendererInsert(renderer, api.TessUnlvRendererCreate());
                    }
                    break;
            }
        }

        return renderer;
    }

    /**
     * Creates documents for given renderers.
     *
     * @param imageFilename input image
     * @param outputPrefix filename without extension
     * @param outputFolder output folder
     * @param formats types of renderers
     * @throws IOException
     */
    @Override
    public void createDocuments(String imageFilename, String outputPrefix, String outputFolder, List<RenderedFormat> formats) throws IOException {
        Map<String, byte[]> map = getRendererOutput(imageFilename, formats);
        
        for (Map.Entry<String, byte[]> entry : map.entrySet()) {
            String key = entry.getKey();
            byte[] value = entry.getValue();
            File file = new File(outputFolder, outputPrefix + "." + key);
            try {
                Utils.writeFile(value, file);
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     * Gets renderer output in form of byte arrays.
     *
     * @param imageFilename input image
     * @param formats types of renderers
     * @return output byte arrays
     */
    @Override
    public Map<String, byte[]> getRendererOutput(String imageFilename, List<RenderedFormat> formats) {
        init();
        setTessVariables();

        TessResultRenderer renderer = createRenderers(formats);

        int result = api.TessBaseAPIProcessPages1(handle, imageFilename, null, 0, renderer);

//        if (result != TessAPI.FALSE) {
//            System.err.println("Error during processing.");
//            return;
//        }
        
        Map<String, byte[]> map = new HashMap<String, byte[]>();

        for (; renderer != null; renderer = api.TessResultRendererNext(renderer)) {
            String ext = api.TessResultRendererExtention(renderer).getString(0);

            PointerByReference data = new PointerByReference();
            IntByReference dataLength = new IntByReference();

            result = api.TessResultRendererGetOutput(renderer, data, dataLength);
            if (result != TessAPI.FALSE) {
                int length = dataLength.getValue();
                byte[] bytes = data.getValue().getByteArray(0, length);
                map.put(ext, bytes);
            }
        }

        api.TessDeleteResultRenderer(renderer);
        dispose();

        return map;
    }

    /**
     * Releases all of the native resources used by this instance.
     */
    private void dispose() {
        api.TessBaseAPIDelete(handle);
    }
}
