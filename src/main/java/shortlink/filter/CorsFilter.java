package shortlink.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/*
* Cors Filter
*
* Must be the first filter of every API context, so the browser preflight (OPTIONS)
* is answered before AuthFilter rejects it for missing a token.
*
* Only origins in the allow list get CORS headers. Credentials are allowed because
* the refresh token is sent as an HttpOnly cookie, so a wildcard origin is never used.
* */
public class CorsFilter extends Filter {

    private static final String ALLOWED_METHODS = "GET, POST, PUT, PATCH, DELETE, OPTIONS";
    // ngrok-skip-browser-warning: sent by the frontend so ngrok free does not answer with its HTML warning page
    private static final String ALLOWED_HEADERS = "Authorization, Content-Type, ngrok-skip-browser-warning";
    private static final String MAX_AGE_SECONDS = "3600";

    /*
    * Allowed origins (exact match, ex: http://localhost:5173)
    * */
    private final Set<String> allowedOrigins;

    /*
    * Constructor
    *
    * originsCsv: comma separated list of origins
    * */
    public CorsFilter(String originsCsv) {
        this.allowedOrigins = Arrays.stream(originsCsv == null ? new String[0] : originsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    /*
    * Do filter
    * */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        boolean allowed = origin != null && allowedOrigins.contains(origin);

        Headers headers = exchange.getResponseHeaders();
        // Response differs per Origin, caches must keep them apart
        headers.add("Vary", "Origin");

        if (allowed) {
            headers.set("Access-Control-Allow-Origin", origin);
            headers.set("Access-Control-Allow-Credentials", "true");
        }

        // Preflight request: answer here, never reach auth / controller
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            if (allowed) {
                headers.set("Access-Control-Allow-Methods", ALLOWED_METHODS);
                headers.set("Access-Control-Allow-Headers", ALLOWED_HEADERS);
                headers.set("Access-Control-Max-Age", MAX_AGE_SECONDS);
            }
            exchange.sendResponseHeaders(allowed ? 204 : 403, -1);
            exchange.close();
            return;
        }

        chain.doFilter(exchange);
    }

    /*
    * Description
    * */
    @Override
    public String description() {
        return "Handle CORS headers and preflight requests";
    }
}
