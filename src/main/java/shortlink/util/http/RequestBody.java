package shortlink.util.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/*
* Request Body
*
* Read the request body with a size limit. Without a limit a client can send a huge body
* and the server keeps all of it in memory.
* */
public final class RequestBody {

    /*
    * Largest accepted body. Every request of the API is a small JSON (longest field is a 2048 chars url)
    * */
    private static final int MAX_BYTES = 16 * 1024;

    private RequestBody() {}

    /*
    * Read body as UTF-8 text
    *
    * @Throw IllegalArgumentException when the body is bigger than MAX_BYTES (→ 400)
    * */
    public static String read(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            // Read one byte more than allowed, so a too large body is detected without reading all of it
            byte[] bytes = input.readNBytes(MAX_BYTES + 1);

            if (bytes.length > MAX_BYTES) {
                throw new IllegalArgumentException("Request body is too large");
            }

            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
