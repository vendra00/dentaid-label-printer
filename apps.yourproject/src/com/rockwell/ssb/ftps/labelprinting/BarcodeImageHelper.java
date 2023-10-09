package com.rockwell.ssb.ftps.labelprinting;


import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.codabar.CodabarBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.datamatrix.DataMatrixBean;
import org.krysalis.barcode4j.impl.int2of5.Interleaved2Of5Bean;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

/**
 * BarcodeImageHelper class to support usage of Barcode4j barcode rendering in JasperReports
 *
 * Essentially the class supports rendering of correct readable and rotated barcodes.
 *
 * @author hott
 */
public class BarcodeImageHelper {

    private static final int DEFAULT_DPI = 150;
    public static final int TYPE_CODE128 = 1;
    public static final int TYPE_CODE39 = 2;
    public static final int TYPE_CODABAR = 3;
    public static final int TYPE_DATAMATRIX = 4;
    public static final int TYPE_INT2OF5 = 5;
    public static final int TYPE_PDF417 = 6;

    public static final int ROTATE_OFF = 0;
    public static final int ROTATE_90 = 90;
    public static final int ROTATE_180 = 180;
    public static final int ROTATE_270 = 270;

    private static final Log LOGGER = LogFactory.getLog(BarcodeImageHelper.class);

    private BarcodeImageHelper() {
        // not meant to be instantiated
    }

    /**
     * Method returns a BufferedImage object of the given type
     *  dpi defaults to 150
     * @param type - one of the supported types (TYPE_*), i.e. TYPE_CODE128
     * @param aText - value of the barcode
     * @param showText - shall the barcode render the human readable text as well
     * @param orientation - one of the supported orientation types (ROTATE_*), i.e. ROTATE_OFF
     * @return
     */
    public static BufferedImage getBarcodeImage(int type, Object aText, boolean showText, int orientation) {
        return getBarcodeImage(type, aText, showText, orientation, DEFAULT_DPI);
    }

    /**
     *  Method returns a BufferedImage object of the given type
     * @param type - one of the supported types (TYPE_*), i.e. TYPE_CODE128
     * @param aText - value of the barcode
     * @param showText - shall the barcode render the human readable text as well
     * @param orientation - one of the supported orientation types (ROTATE_*), i.e. ROTATE_OFF
     * @param dpi - the resultion of the barcode, i.e. 150
     * @return
     */
    public static BufferedImage getBarcodeImage(int type, Object aText, boolean showText, int orientation, int dpi) {

        String text = new StringBuilder().append(aText).toString();
        AbstractBarcodeBean bean = null;
        LOGGER.debug("getBarcodeImage parameters: type " + type + ", aText " + text + ", showText " + showText
                + ", orientation " + orientation + ", dpi " + dpi);

        // CHECKSTYLE:MagicNumber:off
        try {
            switch (type) {
            case TYPE_CODE128:
                bean = new Code128Bean();
                break;
            case TYPE_CODE39:
                bean = new Code39Bean();
                break;
            case TYPE_CODABAR:
                bean = new CodabarBean();
                break;
            case TYPE_DATAMATRIX:
                bean = new DataMatrixBean();
                break;
            case TYPE_INT2OF5:
                bean = new Interleaved2Of5Bean();
                break;
            case TYPE_PDF417:
                bean = new PDF417Bean();
                break;
            default:
                break;
            }

            if (null == bean) {
                throw new RuntimeException("getBarcodeImage failed, no barcode bean delivered. \nParameters: type "
                        + type + ", aText " + text + ", showText " + showText + ", orientation " + orientation
                        + ", dpi " + dpi);
            }

            // Configure the barcode generator
            bean.setModuleWidth(UnitConv.in2mm(1.0f / dpi)); // makes the narrow bar
            // width exactly one pixel
            bean.doQuietZone(true);
            bean.setMsgPosition(showText ? HumanReadablePlacement.HRP_BOTTOM : HumanReadablePlacement.HRP_NONE);

            // Set up the canvas provider for monochrome JPEG output
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(dpi, BufferedImage.TYPE_BYTE_BINARY, false,
                    orientation);

            // Generate the barcode
            bean.generateBarcode(canvas, text);

            // Signal end of generation
            canvas.finish();

            return canvas.getBufferedImage();
        } catch (Exception e) {
            LOGGER.error("got an exception when creating a barcode image that says: " + e.getMessage());
            // generate a runtime exception, invalid value passed.
            // the user must be notified if fail
            throw new RuntimeException(e.getMessage());
        }
    }
}
