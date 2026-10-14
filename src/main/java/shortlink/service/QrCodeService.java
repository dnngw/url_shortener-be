package shortlink.service;

import com.google.zxing.WriterException;

import java.io.IOException;

public interface QrCodeService {

    public byte[] generateQrCode(String url) throws IOException, WriterException;
}
