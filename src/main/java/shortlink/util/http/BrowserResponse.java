package shortlink.util.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/*
* Browser Response
*
* Short links are opened directly in a browser, where a JSON error looks broken to normal visitors.
* Browsers are sent to a page of the frontend instead, API clients keep the JSON response.
* */
public class BrowserResponse {

    /*
    * Frontend page that explains why a short link can not be opened
    * */
    private static final String LINK_UNAVAILABLE_PAGE = "/app/link-unavailable";

    /*
    * Check if the client is a browser asking for a page.
    * Browsers send "Accept: text/html,...", curl / API clients usually send "*\/*" or "application/json"
    * */
    public static boolean wantsHtml(HttpExchange exchange) {
        String accept = exchange.getRequestHeaders().getFirst("Accept");
        return accept != null && accept.contains("text/html");
    }

    /*
    * Redirect the browser to the "link unavailable" page of the frontend
    *
    * reason : not-found | disabled | error
    * code   : the short code that was opened, shown on the page
    * */
    public static void linkUnavailable(HttpExchange exchange, String reason, String code) throws IOException {
        String location = LINK_UNAVAILABLE_PAGE
                + "?reason=" + URLEncoder.encode(reason, StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Location", location);
        // The link may be enabled / created later, do not let the browser cache this redirect
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(302, -1);
        exchange.getResponseBody().close();
    }
}
