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

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.tess4j.util.Utils;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.*;
import java.nio.*;
import java.util.Arrays;
import net.sourceforge.tess4j.TessAPI.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TessAPITest {

    private final String datapath = "src/main/resources";
    private final String testResourcesDataPath = "src/test/resources/test-data";
    String language = "eng";
    String expOCRResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";

    TessAPI api;
    TessAPI.TessBaseAPI handle;
    
    public TessAPITest() {
        System.setProperty("jna.encoding", "UTF8");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        api = new TessDllAPIImpl().getInstance();
        handle = api.TessBaseAPICreate();
    }

    @After
    public void tearDown() {
        api.TessBaseAPIDelete(handle);
    }

    /**
     * Test of TessBaseAPIRect method, of class TessDllLibrary.
     */
    @Test
    public void testTessBaseAPIRect() throws Exception {
        System.out.println("TessBaseAPIRect");
        String expResult = expOCRResult;
        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        File tiff = new File(filename);
        BufferedImage image = ImageIO.read(tiff); // require jai-imageio lib to read TIFF
        ByteBuffer buf = ImageIOHelper.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
        api.TessBaseAPIInit3(handle, datapath, language);
        api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_AUTO);
        Pointer utf8Text = api.TessBaseAPIRect(handle, buf, bytespp, bytespl, 90, 50, 862, 614);
        String result = utf8Text.getString(0);
        api.TessDeleteText(utf8Text);
        System.out.println(result);
        assertTrue(result.startsWith(expResult));
    }

    /**
     * Test of TessBaseAPIGetUTF8Text method, of class TessDllLibrary.
     */
    @Test
    public void testTessBaseAPIGetUTF8Text() throws Exception {
        System.out.println("TessBaseAPIGetUTF8Text");
        String expResult = expOCRResult;
        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        File tiff = new File(filename);
        BufferedImage image = ImageIO.read(new FileInputStream(tiff)); // require jai-imageio lib to read TIFF
        ByteBuffer buf = ImageIOHelper.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
        api.TessBaseAPIInit3(handle, datapath, language);
        api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_AUTO);
        api.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
        api.TessBaseAPISetRectangle(handle, 90, 50, 862, 614);
        Pointer utf8Text = api.TessBaseAPIGetUTF8Text(handle);
        String result = utf8Text.getString(0);
        api.TessDeleteText(utf8Text);
        System.out.println(result);
        assertTrue(result.startsWith(expResult));
    }

    /**
     * Test of TessVersion method, of class TessAPI.
     */
    @Test
    public void testTessVersion() {
        System.out.println("TessVersion");
        String expResult = "3.03";
        String result = api.TessVersion();
        System.out.println(result);
        assertTrue(result.startsWith(expResult));
    }

    /**
     * Test of TessBaseAPICreate method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPICreate() {
        System.out.println("TessBaseAPICreate");
        TessAPI api = new TessDllAPIImpl().getInstance();
        TessAPI.TessBaseAPI handle = api.TessBaseAPICreate();
        assertNotNull(handle);
        api.TessBaseAPIDelete(handle);
    }

    /**
     * Test of TessBaseAPIDelete method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIDelete() {
        System.out.println("TessBaseAPIDelete");
        TessAPI api = new TessDllAPIImpl().getInstance();
        TessAPI.TessBaseAPI handle = api.TessBaseAPICreate();
        api.TessBaseAPIDelete(handle);
    }

    /**
     * Test of TessBaseAPISetInputName method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPISetInputName() {
        System.out.println("TessBaseAPISetInputName");
        String name = "eurotext.tif";
        api.TessBaseAPISetInputName(handle, name);
    }

    /**
     * Test of TessBaseAPISetOutputName method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPISetOutputName() {
        System.out.println("TessBaseAPISetOutputName");
        String name = "out";
        api.TessBaseAPISetOutputName(handle, name);
    }

    /**
     * Test of TessBaseAPISetVariable method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPISetVariable() {
        System.out.println("TessBaseAPISetVariable");
        String name = "tessedit_create_hocr";
        String value = "1";
        int expResult = 1;
        int result = api.TessBaseAPISetVariable(handle, name, value);
        assertEquals(expResult, result);
    }

    /**
     * Test of TessBaseAPIGetBoolVariable method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIGetBoolVariable() {
        System.out.println("TessBaseAPIGetBoolVariable");
        String name = "tessedit_create_hocr";
        api.TessBaseAPISetVariable(handle, name, "1");
        IntBuffer value = IntBuffer.allocate(1);
        int result = -1;
        if (api.TessBaseAPIGetBoolVariable(handle, "tessedit_create_hocr", value) == TessAPI.TRUE) {
            result = value.get(0);
        }
        int expResult = 1;
        assertEquals(expResult, result);
    }

    /**
     * Test of TessBaseAPIPrintVariables method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIPrintVariablesToFile() throws Exception {
        System.out.println("TessBaseAPIPrintVariablesToFile");
        String var = "tessedit_char_whitelist";
        String value = "0123456789";
        api.TessBaseAPISetVariable(handle, var, value);
        String filename = "printvar.txt";
        api.TessBaseAPIPrintVariablesToFile(handle, filename);  // will crash if not invoked after some method
        File file = new File(filename);
        BufferedReader input = new BufferedReader(new FileReader(file));
        StringBuilder strB = new StringBuilder();
        String line;
        String EOL = System.getProperty("line.separator");
        while ((line = input.readLine()) != null) {
            strB.append(line).append(EOL);
        }
        input.close();
        file.delete();
        assertTrue(strB.toString().contains(var + "\t" + value));
    }

    /**
     * Test of TessBaseAPIInit1 method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIInit1() {
        System.out.println("TessBaseAPIInit1");
        int oem = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
        PointerByReference configs = null;
        int configs_size = 0;
        int expResult = 0;
        int result = api.TessBaseAPIInit1(handle, datapath, language, oem, configs, configs_size);
        assertEquals(expResult, result);
    }

    /**
     * Test of TessBaseAPIInit2 method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIInit2() {
        System.out.println("TessBaseAPIInit2");
        int oem = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
        int expResult = 0;
        int result = api.TessBaseAPIInit2(handle, datapath, language, oem);
        assertEquals(expResult, result);
    }

    /**
     * Test of TessBaseAPIInit3 method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIInit3() {
        System.out.println("TessBaseAPIInit3");
        int expResult = 0;
        int result = api.TessBaseAPIInit3(handle, datapath, language);
        assertEquals(expResult, result);
    }

    /**
     * Test of TessBaseAPIInit4 method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIInit4() {
        System.out.println("TessBaseAPIInit4");
        int oem = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
        PointerByReference configs = null;
        int configs_size = 0;
        int expResult = 0;
        int result = api.TessBaseAPIInit4(handle, datapath, language, oem, configs, configs_size, null, null, new NativeSize(), TessAPI.FALSE);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of TessBaseAPIGetInitLanguagesAsString method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIGetInitLanguagesAsString() {
        System.out.println("TessBaseAPIGetInitLanguagesAsString");
        String expResult = "";
        String result = api.TessBaseAPIGetInitLanguagesAsString(handle);
        assertEquals(expResult, result);
    }

    /**
     * Test of TessBaseAPIGetLoadedLanguagesAsVector method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIGetLoadedLanguagesAsVector() {
        System.out.println("TessBaseAPIGetLoadedLanguagesAsVector");
        api.TessBaseAPIInit3(handle, datapath, language);
        String[] expResult = { "eng" };
        String[] result = api.TessBaseAPIGetLoadedLanguagesAsVector(handle).getPointer().getStringArray(0);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of TessBaseAPIGetAvailableLanguagesAsVector method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIGetAvailableLanguagesAsVector() {
        System.out.println("TessBaseAPIGetAvailableLanguagesAsVector");
        api.TessBaseAPIInit3(handle, datapath, language);
        String[] expResult = { "eng" };
        String[] result = api.TessBaseAPIGetAvailableLanguagesAsVector(handle).getPointer().getStringArray(0);
        assertTrue(Arrays.asList(result).containsAll(Arrays.asList(expResult)));
    }

    /**
     * Test of TessBaseAPISetPageSegMode method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPISetPageSegMode() {
        System.out.println("TessBaseAPISetPageSegMode");
        int mode = TessAPI.TessPageSegMode.PSM_AUTO;
        api.TessBaseAPISetPageSegMode(handle, mode);
    }

    /**
     * Test of TessBaseAPIGetPageSegMode method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIGetPageSegMode() {
        System.out.println("TessBaseAPIGetPageSegMode");
        api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_SINGLE_CHAR);
        int expResult = TessAPI.TessPageSegMode.PSM_SINGLE_CHAR;
        int result = api.TessBaseAPIGetPageSegMode(handle);
        assertEquals(expResult, result);
    }

    /**
     * Test of TessBaseAPISetImage method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPISetImage() {
        System.out.println("TessBaseAPISetImage");
        ByteBuffer imagedata = null;
        int width = 0;
        int height = 0;
        int bytes_per_pixel = 0;
        int bytes_per_line = 0;
        api.TessBaseAPISetImage(handle, imagedata, width, height, bytes_per_pixel, bytes_per_line);
    }

    /**
     * Test of TessBaseAPISetRectangle method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPISetRectangle() {
        System.out.println("TessBaseAPISetRectangle");
        int left = 0;
        int top = 0;
        int width = 0;
        int height = 0;
        api.TessBaseAPISetRectangle(handle, left, top, width, height);
    }

    /**
     * Test of TessBaseAPIProcessPages method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIProcessPages() {
        System.out.println("TessBaseAPIProcessPages");
        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        String retry_config = null;
        int timeout_millisec = 0;
        api.TessBaseAPIInit3(handle, datapath, language);
        String expResult = expOCRResult;
        Pointer utf8Text = api.TessBaseAPIProcessPages(handle, filename, retry_config, timeout_millisec);
        String result = utf8Text.getString(0);
        api.TessDeleteText(utf8Text);
        assertTrue(result.startsWith(expResult));
    }

    /**
     * Test of TessBaseAPIProcessPages1 method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIProcessPages1() {
        System.out.println("TessBaseAPIProcessPages1");
        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        String retry_config = null;
        int timeout_millisec = 0;
        TessResultRenderer renderer = api.TessTextRendererCreate();
        api.TessBaseAPIInit3(handle, datapath, language);
        String expResult = expOCRResult;
        api.TessBaseAPIProcessPages1(handle, filename, retry_config, timeout_millisec, renderer);
        PointerByReference data = new PointerByReference();
        IntByReference dataLength = new IntByReference();
        api.TessResultRendererGetOutput(renderer, data, dataLength);
        int length = dataLength.getValue();
        String result = data.getValue().getString(0);
        assertTrue(result.startsWith(expResult));
//        api.TessDeleteResultRenderer(renderer);
    }
    
    /**
     * Test of TessBaseAPIGetHOCRText method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIGetHOCRText() throws Exception {
        System.out.println("TessBaseAPIGetHOCRText");
        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        File tiff = new File(filename);
        BufferedImage image = ImageIO.read(new FileInputStream(tiff)); // require jai-imageio lib to read TIFF
        ByteBuffer buf = ImageIOHelper.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
        api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_AUTO);
        api.TessBaseAPIInit3(handle, datapath, language);
        api.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
        int page_number = 0;
        Pointer utf8Text = api.TessBaseAPIGetHOCRText(handle, page_number);
        String result = utf8Text.getString(0);
        api.TessDeleteText(utf8Text);
        assertTrue(result.contains("<div class='ocr_page'"));
    }

    /**
     * Test of Orientation and script detection (OSD).
     */
    @Test
    public void testOSD() throws Exception {
        System.out.println("OSD");
        int expResult = TessAPI.TessPageSegMode.PSM_AUTO_OSD;
        IntBuffer orientation = IntBuffer.allocate(1);
        IntBuffer direction = IntBuffer.allocate(1);
        IntBuffer order = IntBuffer.allocate(1);
        FloatBuffer deskew_angle = FloatBuffer.allocate(1);
        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        File tiff = new File(filename);
        BufferedImage image = ImageIO.read(new FileInputStream(tiff)); // require jai-imageio lib to read TIFF
        ByteBuffer buf = ImageIOHelper.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
        api.TessBaseAPIInit3(handle, datapath, language);
        api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_AUTO_OSD);
        int actualResult = api.TessBaseAPIGetPageSegMode(handle);
        System.out.println("PSM: " + Utils.getConstantName(actualResult, TessAPI1.TessPageSegMode.class));
        api.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
        int success = api.TessBaseAPIRecognize(handle, null);
        if (success == 0) {
            TessAPI.TessPageIterator pi = api.TessBaseAPIAnalyseLayout(handle);
            api.TessPageIteratorOrientation(pi, orientation, direction, order, deskew_angle);
            System.out.println(String.format("Orientation: %s\nWritingDirection: %s\nTextlineOrder: %s\nDeskew angle: %.4f\n",
                Utils.getConstantName(orientation.get(), TessOrientation.class), 
                Utils.getConstantName(direction.get(), TessWritingDirection.class), 
                Utils.getConstantName(order.get(), TessTextlineOrder.class), 
                deskew_angle.get()));
        }
        
        assertEquals(expResult, actualResult);
    }

    /**
     * Test of ResultIterator and PageIterator.
     *
     * @throws Exception
     */
    @Test
    public void testResultIterator() throws Exception {
        System.out.println("TessBaseAPIGetIterator");
        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        File tiff = new File(filename);
        BufferedImage image = ImageIO.read(new FileInputStream(tiff)); // require jai-imageio lib to read TIFF
        ByteBuffer buf = ImageIOHelper.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
        api.TessBaseAPIInit3(handle, datapath, language);
        api.TessBaseAPISetPageSegMode(handle, TessAPI.TessPageSegMode.PSM_AUTO);
        api.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
        api.TessBaseAPIRecognize(handle, null);
        TessAPI.TessResultIterator ri = api.TessBaseAPIGetIterator(handle);
        TessAPI.TessPageIterator pi = api.TessResultIteratorGetPageIterator(ri);
        api.TessPageIteratorBegin(pi);
        System.out.println("Bounding boxes:\nchar(s) left top right bottom confidence font-attributes");

//        int height = image.getHeight();
        do {
            Pointer ptr = api.TessResultIteratorGetUTF8Text(ri, TessAPI.TessPageIteratorLevel.RIL_WORD);
            String word = ptr.getString(0);
            api.TessDeleteText(ptr);
            float confidence = api.TessResultIteratorConfidence(ri, TessAPI.TessPageIteratorLevel.RIL_WORD);
            IntBuffer leftB = IntBuffer.allocate(1);
            IntBuffer topB = IntBuffer.allocate(1);
            IntBuffer rightB = IntBuffer.allocate(1);
            IntBuffer bottomB = IntBuffer.allocate(1);
            api.TessPageIteratorBoundingBox(pi, TessAPI.TessPageIteratorLevel.RIL_WORD, leftB, topB, rightB, bottomB);
            int left = leftB.get();
            int top = topB.get();
            int right = rightB.get();
            int bottom = bottomB.get();
            System.out.print(String.format("%s %d %d %d %d %f", word, left, top, right, bottom, confidence));
//            System.out.println(String.format("%s %d %d %d %d", str, left, height - bottom, right, height - top)); // training box coordinates
            
            IntBuffer boldB = IntBuffer.allocate(1);
            IntBuffer italicB = IntBuffer.allocate(1);
            IntBuffer underlinedB = IntBuffer.allocate(1);
            IntBuffer monospaceB = IntBuffer.allocate(1);
            IntBuffer serifB = IntBuffer.allocate(1);
            IntBuffer smallcapsB = IntBuffer.allocate(1);
            IntBuffer pointSizeB = IntBuffer.allocate(1);
            IntBuffer fontIdB = IntBuffer.allocate(1);
            String fontName = api.TessResultIteratorWordFontAttributes(ri, boldB, italicB, underlinedB,
                    monospaceB, serifB, smallcapsB, pointSizeB, fontIdB);
            boolean bold = boldB.get() == TessAPI.TRUE;
            boolean italic = italicB.get() == TessAPI.TRUE;
            boolean underlined = underlinedB.get() == TessAPI.TRUE;
            boolean monospace = monospaceB.get() == TessAPI.TRUE;
            boolean serif = serifB.get() == TessAPI.TRUE;
            boolean smallcaps = smallcapsB.get() == TessAPI.TRUE;
            int pointSize = pointSizeB.get();
            int fontId = fontIdB.get();
            System.out.println(String.format("  font: %s, size: %d, font id: %d, bold: %b," +
                       " italic: %b, underlined: %b, monospace: %b, serif: %b, smallcap: %b", 
                    fontName, pointSize, fontId, bold, italic, underlined, monospace, serif, smallcaps));
        } while (api.TessPageIteratorNext(pi, TessAPI.TessPageIteratorLevel.RIL_WORD) == TessAPI.TRUE);
        
        assertTrue(true);
    }

    /**
     * Test of ChoiceIterator.
     *
     * @throws Exception
     */
    @Test
    public void testChoiceIterator() throws Exception {
        System.out.println("TessResultIteratorGetChoiceIterator");
        String filename = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        File tiff = new File(filename);
        BufferedImage image = ImageIO.read(new FileInputStream(tiff)); // require jai-imageio lib to read TIFF
        ByteBuffer buf = ImageIOHelper.convertImageData(image);
        int bpp = image.getColorModel().getPixelSize();
        int bytespp = bpp / 8;
        int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);
        api.TessBaseAPIInit3(handle, datapath, language);
        api.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);
        api.TessBaseAPISetVariable(handle, "save_blob_choices", "T");
        api.TessBaseAPISetRectangle(handle, 37, 228, 548, 31);
        api.TessBaseAPIRecognize(handle, null);
        TessAPI.TessResultIterator ri = api.TessBaseAPIGetIterator(handle);
        int level = TessAPI.TessPageIteratorLevel.RIL_SYMBOL;

        if (ri != null) {
            do {
                Pointer symbol = api.TessResultIteratorGetUTF8Text(ri, level);
                float conf = api.TessResultIteratorConfidence(ri, TessAPI.TessPageIteratorLevel.RIL_WORD);
                if (symbol != null) {
                    System.out.println(String.format("symbol %s, conf: %f", symbol.getString(0), conf));
                    boolean indent = false;
                    TessAPI.TessChoiceIterator ci = api.TessResultIteratorGetChoiceIterator(ri);
                    do {
                        if (indent) System.out.print("\t");
                        System.out.print("\t- ");
                        String choice = api.TessChoiceIteratorGetUTF8Text(ci);
                        System.out.println(String.format("%s conf: %f", choice, api.TessChoiceIteratorConfidence(ci)));
                        indent = true;
                    } while (api.TessChoiceIteratorNext(ci) == TessAPI1.TRUE);
                    api.TessChoiceIteratorDelete(ci);
                }
                System.out.println("---------------------------------------------");
                api.TessDeleteText(symbol);
            } while (api.TessResultIteratorNext(ri, level) == TessAPI1.TRUE);
        }
        
        assertTrue(true);
    }    

    /**
     * Test of ResultRenderer method, of class TessAPI.
     */
    @Test
    public void testResultRenderer() throws Exception {
        System.out.println("TessResultRenderer");
        String image = String.format("%s/%s", this.testResourcesDataPath, "eurotext.tif");
        String output = "capi-test.txt";
        String outFile = "renderer";
        String folder = "target/test-classes/test-results";
        int set_only_init_params = TessAPI.FALSE;
        int oem = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
        PointerByReference configs = null;
        int configs_size = 0;
        
        String confs[] = {"load_system_dawg", "tessedit_char_whitelist"};
        String vals[] = {"F", ""}; //0123456789-.IThisalotfpnex
        PointerByReference pbrc = new PointerByReference();
        PointerByReference pbrv = new PointerByReference();
        pbrc.setPointer(new StringArray(confs));
        pbrv.setPointer(new StringArray(vals));
        NativeSize conf_size = new NativeSize(confs.length);

        api.TessBaseAPISetOutputName(handle, output);

        int rc = api.TessBaseAPIInit4(handle, datapath, language,
                              oem, configs, configs_size, pbrc, pbrv, conf_size, set_only_init_params);
        
        if (rc != 0) {
            api.TessBaseAPIDelete(handle);
            System.err.println("Could not initialize tesseract.");
            return;
        }

        TessResultRenderer renderer = api.TessHOcrRendererCreate();
        api.TessResultRendererInsert(renderer, api.TessBoxTextRendererCreate());
        api.TessResultRendererInsert(renderer, api.TessTextRendererCreate());
        String dataPath = api.TessBaseAPIGetDatapath(handle);
        api.TessResultRendererInsert(renderer, api.TessPDFRendererCreate(dataPath));

        int result = api.TessBaseAPIProcessPages1(handle, image, null, 0, renderer);
        
//        if (result != TessAPI.FALSE) {
//            System.err.println("Error during processing.");
//            return;
//        }
        
        for (; renderer != null; renderer = api.TessResultRendererNext(renderer)) {
            String typeName = api.TessResultRendererTypename(renderer).getString(0);
            String ext = api.TessResultRendererExtention(renderer).getString(0);
            System.out.println(String.format("TessResultRendererTypename: %s\nTessResultRendererExtention: %s\nTessResultRendererTitle: %s\nTessResultRendererImageNum: %d", 
                    typeName,
                    ext,
                    api.TessResultRendererTitle(renderer).getString(0),
                    api.TessResultRendererImageNum(renderer)));
   
            PointerByReference data = new PointerByReference();
            IntByReference dataLength = new IntByReference();
        
            result = api.TessResultRendererGetOutput(renderer, data, dataLength);
            if (result != TessAPI.FALSE) {
                int length = dataLength.getValue();
                byte[] bytes = data.getValue().getByteArray(0, length);
                FileOutputStream bw = null;
                try {
                    File file = new File(folder, outFile + "." + ext);
                    bw = new FileOutputStream(file);
                    bw.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bw != null) {
                        bw.close();
                    }
                }
            }
        }
        
        api.TessDeleteResultRenderer(renderer);
        assertTrue(new File(folder, outFile + ".pdf").exists());
    }

    /**
     * Test of TessBaseAPIClear method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIClear() {
        System.out.println("TessBaseAPIClear");
        api.TessBaseAPIClear(handle);
    }

    /**
     * Test of TessBaseAPIEnd method, of class TessAPI.
     */
    @Test
    public void testTessBaseAPIEnd() {
        System.out.println("TessBaseAPIEnd");
        api.TessBaseAPIEnd(handle);
    }

    public class TessDllAPIImpl implements TessAPI {

        public TessAPI getInstance() {
            return INSTANCE;
        }

        public void TessDllEndPage() {
        }

        public void TessDllRelease() {
        }

        public boolean SetVariable(String variable, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void SimpleInit(String datapath, String language, boolean numeric_mode) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int Init(String datapath, String outputbase, String configfile, boolean numeric_mode, int argc, String[] argv) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int InitWithLanguage(String datapath, String outputbase, String language, String configfile, boolean numeric_mode, int argc, String[] argv) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int InitLangMod(String datapath, String outputbase, String language, String configfile, boolean numeric_mode, int argc, String[] argv) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void SetInputName(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TesseractRect(ByteBuffer imagedata, int bytes_per_pixel, int bytes_per_line, int left, int top, int width, int height) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TesseractRectBoxes(ByteBuffer imagedata, int bytes_per_pixel, int bytes_per_line, int left, int top, int width, int height, int imageheight) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TesseractRectUNLV(ByteBuffer imagedata, int bytes_per_pixel, int bytes_per_line, int left, int top, int width, int height) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void ClearAdaptiveClassifier() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void End() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void DumpPGM(String filename) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Image GetTesseractImage() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int OtsuStats(int histogram, int H_out, int omega0_out) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int IsValidWord(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TessVersion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessDeleteText(String text) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessDeleteIntArray(IntBuffer arr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessBaseAPI TessBaseAPICreate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIDelete(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPISetInputName(TessBaseAPI handle, String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPISetOutputName(TessBaseAPI handle, String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPISetVariable(TessBaseAPI handle, String name, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIGetIntVariable(TessBaseAPI handle, String name, IntBuffer value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIGetBoolVariable(TessBaseAPI handle, String name, IntBuffer value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIGetDoubleVariable(TessBaseAPI handle, String name, DoubleBuffer value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TessBaseAPIGetStringVariable(TessBaseAPI handle, String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIPrintVariablesToFile(TessBaseAPI handle, String filename) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIInit1(TessBaseAPI handle, String datapath, String language, int oem, PointerByReference configs, int configs_size) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIInit2(TessBaseAPI handle, String datapath, String language, int oem) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIInit3(TessBaseAPI handle, String datapath, String language) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIInit4(TessBaseAPI handle, String datapath, String language, int oem, PointerByReference configs, int configs_size, PointerByReference vars_vec, PointerByReference vars_values, NativeSize vars_vec_size, int set_only_non_debug_params) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIInitLangMod(TessBaseAPI handle, String datapath, String language) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIInitForAnalysePage(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIReadConfigFile(TessBaseAPI handle, String filename, int init_only) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPISetPageSegMode(TessBaseAPI handle, int mode) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIGetPageSegMode(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessBaseAPIRect(TessBaseAPI handle, ByteBuffer imagedata, int bytes_per_pixel, int bytes_per_line, int left, int top, int width, int height) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIClearAdaptiveClassifier(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPISetImage(TessBaseAPI handle, ByteBuffer imagedata, int width, int height, int bytes_per_pixel, int bytes_per_line) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPISetRectangle(TessBaseAPI handle, int left, int top, int width, int height) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIDumpPGM(TessBaseAPI handle, String filename) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessPageIterator TessBaseAPIAnalyseLayout(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIRecognize(TessBaseAPI handle, ETEXT_DESC monitor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIRecognizeForChopTest(TessBaseAPI handle, ETEXT_DESC monitor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessResultIterator TessBaseAPIGetIterator(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessBaseAPIProcessPages(TessBaseAPI handle, String filename, String retry_config, int timeout_millisec) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public int TessBaseAPIProcessPages1(TessBaseAPI handle, String filename, String retry_config, int timeout_millisec, TessResultRenderer renderer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessBaseAPIGetUTF8Text(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessBaseAPIGetHOCRText(TessBaseAPI handle, int page_number) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessBaseAPIGetBoxText(TessBaseAPI handle, int page_number) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessBaseAPIGetUNLVText(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIMeanTextConf(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public IntByReference TessBaseAPIAllWordConfidences(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIAdaptToWordStr(TessBaseAPI handle, int mode, String wordstr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIClear(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIEnd(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIIsValidWord(TessBaseAPI handle, String word) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIGetTextDirection(TessBaseAPI handle, IntBuffer out_offset, FloatBuffer out_slope) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TessBaseAPIGetUnichar(TessBaseAPI handle, int unichar_id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TessBaseGetInitLanguagesAsString(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPISetMinOrientationMargin(TessBaseAPI handle, double margin) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessPageIteratorDelete(TessPageIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessPageIterator TessPageIteratorCopy(TessPageIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessPageIteratorBegin(TessPageIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessPageIteratorNext(TessPageIterator handle, int level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessPageIteratorIsAtBeginningOf(TessPageIterator handle, int level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessPageIteratorIsAtFinalElement(TessPageIterator handle, int level, int element) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessPageIteratorBoundingBox(TessPageIterator handle, int level, IntBuffer left, IntBuffer top, IntBuffer right, IntBuffer bottom) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessPageIteratorBlockType(TessPageIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessPageIteratorBaseline(TessPageIterator handle, int level, IntBuffer x1, IntBuffer y1, IntBuffer x2, IntBuffer y2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessPageIteratorOrientation(TessPageIterator handle, IntBuffer orientation, IntBuffer writing_direction, IntBuffer textline_order, FloatBuffer deskew_angle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessResultIteratorDelete(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessResultIterator TessResultIteratorCopy(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessPageIterator TessResultIteratorGetPageIterator(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessPageIterator TessResultIteratorGetPageIteratorConst(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessResultIteratorGetUTF8Text(TessResultIterator handle, int level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        public int TessResultIteratorNext(TessResultIterator handle, int level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public float TessResultIteratorConfidence(TessResultIterator handle, int level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TessResultIteratorWordFontAttributes(TessResultIterator handle, IntBuffer is_bold, IntBuffer is_italic, IntBuffer is_underlined, IntBuffer is_monospace, IntBuffer is_serif, IntBuffer is_smallcaps, IntBuffer pointsize, IntBuffer font_id) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultIteratorWordIsFromDictionary(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultIteratorWordIsNumeric(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultIteratorSymbolIsSuperscript(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultIteratorSymbolIsSubscript(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultIteratorSymbolIsDropcap(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TessBaseAPIGetInitLanguagesAsString(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public PointerByReference TessBaseAPIGetLoadedLanguagesAsVector(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public PointerByReference TessBaseAPIGetAvailableLanguagesAsVector(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPISetSourceResolution(TessBaseAPI handle, int ppi) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessDeleteText(Pointer text) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessDeleteTextArray(PointerByReference arr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIGetThresholdedImageScaleFactor(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessMutableIterator TessBaseAPIGetMutableIterator(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessResultRenderer TessTextRendererCreate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessResultRenderer TessHOcrRendererCreate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessResultRenderer TessPDFRendererCreate(String datadir) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessResultRenderer TessUnlvRendererCreate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessResultRenderer TessBoxTextRendererCreate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessDeleteResultRenderer(TessResultRenderer renderer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessResultRendererInsert(TessResultRenderer renderer, PointerByReference next) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public TessResultRenderer TessResultRendererNext(TessResultRenderer renderer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultRendererBeginDocument(TessResultRenderer renderer, String title) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultRendererBeginDocument(TessResultRenderer renderer, Pointer title) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultRendererAddImage(TessResultRenderer renderer, PointerByReference api) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultRendererAddError(TessResultRenderer renderer, PointerByReference api) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultRendererEndDocument(TessResultRenderer renderer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultRendererGetOutput(TessResultRenderer renderer, PointerByReference data, IntByReference data_len) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessResultRendererTypename(TessResultRenderer renderer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessResultRendererExtention(TessResultRenderer renderer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Pointer TessResultRendererTitle(TessResultRenderer renderer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessResultRendererImageNum(TessResultRenderer renderer) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TessBaseAPIGetInputName(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int TessBaseAPIGetSourceYResolution(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String TessBaseAPIGetDatapath(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessResultRendererInsert(TessResultRenderer renderer, TessResultRenderer next) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void TessBaseAPIClearPersistentCache(TessBaseAPI handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TessChoiceIterator TessResultIteratorGetChoiceIterator(TessResultIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        @Override
        public void TessChoiceIteratorDelete(TessChoiceIterator handle) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public int TessChoiceIteratorNext(TessChoiceIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String TessChoiceIteratorGetUTF8Text(TessChoiceIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float TessChoiceIteratorConfidence(TessChoiceIterator handle) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
