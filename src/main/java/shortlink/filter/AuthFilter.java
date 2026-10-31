package shortlink.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.Response;
import shortlink.util.security.JwtTokenProvider;

import java.io.IOException;

/*
* Auth Filter
* */
public class AuthFilter extends Filter {

    /*
    * Bearer Prefix
    * */
    private static final String BEARER_PREFIX = "Bearer ";

    /*
     * Attr User Id
     * */
    public static final String ATTR_USER_ID = "userId";

    /*
    * Attr Role
    * */
    public static final String ATTR_ROLE = "role";

    /*
    * Jwt Token Provider
    * */
    private final JwtTokenProvider jwtTokenProvider;

    /*
    * Constructor
    * */
    public AuthFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }


    /*
    * Do filter
    * */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String header = exchange.getRequestHeaders().getFirst("Authorization");

        if(header == null || !header.startsWith(BEARER_PREFIX)) {
            Response.response(exchange, 401, new ApiResponse<>(401, "Missing access token", null));
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        try {
            Claims claim = jwtTokenProvider.parse(token);

            exchange.setAttribute(ATTR_USER_ID, Long.parseLong(claim.getSubject()));
            exchange.setAttribute(ATTR_ROLE, claim.get("role", String.class));

        }catch(JwtException | IllegalArgumentException e) {
            Response.response(exchange, 401, new ApiResponse<>(401, "Invalid or missing access token", null));
            return;
        }


        // Continue to the next filter or controller
        chain.doFilter(exchange);
    }


    /*
    * Description
    * */
    @Override
    public String description() {
        return "Validate JWT token";
    }


}
