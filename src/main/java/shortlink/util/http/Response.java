package shortlink.util.http;

import com.sun.net.httpserver.HttpExchange;
import shortlink.util.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/*
* Send response
*
* This is template for send back the response to client from controller
*
* Since the HttpServer not create automaticlly the response template so we need built it manually
*
* */
public class Response {

    public static void response(HttpExchange exchange, int status, ApiResponse<?> response) throws IOException {
        String JSONBody = JsonUtil.toJson(response);
        byte[] bytes = JSONBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try(OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void noContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
        exchange.getResponseBody().close();
    }
}
