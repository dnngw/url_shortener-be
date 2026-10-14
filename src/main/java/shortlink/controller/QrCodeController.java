package shortlink.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import shortlink.service.QrCodeService;
import shortlink.util.http.ApiResponse;
import shortlink.util.ParseQuery;
import shortlink.util.http.ImageResponse;
import shortlink.util.http.Response;

import java.io.IOException;
import java.util.Map;

public class QrCodeController implements HttpHandler {
    private final QrCodeService service;

    public QrCodeController(QrCodeService service) {
        this.service = service;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        /*
        * API Router
        *
        * "GET"
        * api/v1/qr?url=""&download=""
        *
        * */
        switch(method) {
            case "GET" :
               generateQrCode(exchange);
               break;

            default :
                Response.response(exchange, 405, null);
        }

    }



    public void generateQrCode(HttpExchange exchange) throws IOException {
        Map<String, String> params = ParseQuery.parse(exchange.getRequestURI().getRawQuery());
        String url = params.get("url");
        boolean download = "true".equals(params.get("download"));

        try {
            byte[] png = service.generateQrCode(url);
            ImageResponse.responseImage(exchange,200, png, download);

        }catch(IllegalArgumentException e){
            ApiResponse<String> res = new ApiResponse<>(400, e.getMessage(), null);
            Response.response(exchange, 400, res);

        }catch(Exception e){
            ApiResponse<String> res = new ApiResponse<>(500, "Internal server error", null);
            Response.response(exchange, 500, res);

        }
    }
}
