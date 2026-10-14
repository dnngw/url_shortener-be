package shortlink.util.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

/*
* Send Image Response
* */
public class ImageResponse {
    public static void responseImage(HttpExchange exchange, int status, byte[] data, boolean download) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "image/png");

        if (download) {
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"qrcode.png\"");
        }

        exchange.sendResponseHeaders(status, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }
}
