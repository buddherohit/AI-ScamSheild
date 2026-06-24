package com.scamshield.qr.service.impl;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.scamshield.qr.service.QRParser;
import com.scamshield.qr.service.QrParsingException;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@Service
public class ZXingQRParserImpl implements QRParser {
    @Override
    public String parse(byte[] qrImageBytes) throws QrParsingException {
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(qrImageBytes));
            if (bufferedImage == null) {
                throw new QrParsingException("Invalid or corrupt image file");
            }
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(bufferedImage)));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            throw new QrParsingException("Failed to decode QR code image: " + e.getMessage(), e);
        }
    }
}
