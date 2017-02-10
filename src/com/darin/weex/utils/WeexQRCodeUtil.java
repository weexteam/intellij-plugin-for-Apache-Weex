package com.darin.weex.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.util.Hashtable;

/**
 * Created by darin on 5/25/16.
 */
public class WeexQRCodeUtil {

    /**
     * use zxing to transform String to QRcode
     *
     * @param content src String
     * @return Qrcode BufferedImage
     * @throws WriterException
     */
    public static BufferedImage Encode_QR_CODE(String content) {
        int width = 430;
        int height = 430;

        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
