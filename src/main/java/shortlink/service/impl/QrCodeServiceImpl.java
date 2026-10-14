package shortlink.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import shortlink.service.QrCodeService;
import shortlink.util.validator.UrlValidator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QrCodeServiceImpl implements QrCodeService {

    /*
    * Size of width and heigh
    * */
    private final short SIZE = 1000;

    public byte[] generateQrCode(String url) throws IOException, WriterException {
        UrlValidator.validate(url);

        // Generate the QR Code
        BitMatrix matrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, SIZE, SIZE);

        // Convert to byte[] PNG
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", output);
        return output.toByteArray();

    }
}
