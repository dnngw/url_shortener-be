package shortlink.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import shortlink.service.LinkService;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.Response;

import java.io.IOException;

public class RedirectController implements HttpHandler {

    /*
    * Service
    * */
    private final LinkService service;

    /*
    * Constructor
    * */
    public RedirectController(LinkService service) {
        this.service = service;
    }


    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String code = path.substring(1);

        /*
        * Default Router "/"
        *
        * To recieve the shortlink /:code
        * */
        if(code.isEmpty()) {
            ApiResponse<String> res = new ApiResponse<>(200, "URL Shortener", null);
            Response.response(exchange, 200, res);
        } else {
            try {
                String originalUrl = service.findByShortCode(code);
                exchange.getResponseHeaders().set("location", originalUrl);
                Response.response(exchange, 302, null);

            }catch (Exception e) {
                ApiResponse<String> res = new ApiResponse<>(404, "Shortlink not found", null);
                Response.response(exchange, 404, res);
            }
        }

    }

}
