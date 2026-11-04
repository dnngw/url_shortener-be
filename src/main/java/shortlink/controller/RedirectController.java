package shortlink.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import shortlink.exception.NotFoundException;
import shortlink.model.Link;
import shortlink.service.LinkService;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.BrowserResponse;
import shortlink.util.http.Response;

import java.io.IOException;
import java.util.Locale;

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
                Link link = service.getForRedirect(code.toLowerCase(Locale.ROOT));

                if (link == null) {
                    throw new NotFoundException("Shortlink not found");
                }

                // Disabled by admin : 410 Gone, the link existed but is no longer available
                if (link.isDisabled()) {
                    if (BrowserResponse.wantsHtml(exchange)) {
                        BrowserResponse.linkUnavailable(exchange, "disabled", code);
                    } else {
                        Response.response(exchange, 410, new ApiResponse<>(410, "This link has been disabled", null));
                    }
                    return;
                }

                exchange.getResponseHeaders().set("Location", link.getOriginalUrl());
                // Redirect has no body (-1 = no content)
                exchange.sendResponseHeaders(302, -1);
                exchange.getResponseBody().close();

            }catch(NotFoundException e) {
                // Browser → friendly page of the frontend, API client → JSON
                if (BrowserResponse.wantsHtml(exchange)) {
                    BrowserResponse.linkUnavailable(exchange, "not-found", code);
                } else {
                    Response.response(exchange, 404, new ApiResponse<>(404, e.getMessage(), null));
                }

            }catch (Exception e) {
                if (BrowserResponse.wantsHtml(exchange)) {
                    BrowserResponse.linkUnavailable(exchange, "error", code);
                } else {
                    Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
                }
            }
        }

    }

}
