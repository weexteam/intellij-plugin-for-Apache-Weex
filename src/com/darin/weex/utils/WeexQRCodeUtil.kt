package com.darin.weex.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

import java.awt.image.BufferedImage
import java.util.Hashtable

/**
 * Created by darin on 5/25/16.
 */
object WeexQRCodeUtil {

    /**
     * use zxing to transform String to QRcode

     * @param content src String
     * *
     * @return Qrcode BufferedImage
     * *
     * @throws WriterException
     */
    fun Encode_QR_CODE(content: String): BufferedImage? {
        val width = 430
        val height = 430

        val hints = Hashtable<EncodeHintType, Any>()
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8")
        hints.put(EncodeHintType.MARGIN, 1)

        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(content,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints)
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }

        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }
}
