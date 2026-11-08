package shortlink.controller;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlink.dto.*;
import shortlink.exception.DuplicateResourceException;
import shortlink.exception.ForbiddenException;
import shortlink.exception.TooManyRequestsException;
import shortlink.exception.UnauthorizedException;
import shortlink.service.AuthService;
import shortlink.util.security.AuthRateLimit;
import shortlink.util.JsonUtil;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.ClientIp;
import shortlink.util.http.RequestBody;
import shortlink.util.http.Response;

import java.io.IOException;

/*
* Auth Controller
* */
public class AuthController implements HttpHandler {
    /*
    * Logger
    * */
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /*
    * BASE_PATH
    * */
    private static final String BASE_PATH = "/api/v1/auth";

    /*
    * Auth service
    * */
    private final AuthService authService;

    /*
    * Rate limit for login and register
    * */
    private final AuthRateLimit rateLimit;

    /*
    * Read the client ip from X-Forwarded-For (app runs behind a reverse proxy)
    * */
    private final boolean trustProxy;


    public AuthController(AuthService authService, AuthRateLimit rateLimit, boolean trustProxy) {
        this.authService = authService;
        this.rateLimit = rateLimit;
        this.trustProxy = trustProxy;
    }



    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!"POST".equals(exchange.getRequestMethod())) {
            Response.response(exchange, 405, new ApiResponse<>(405, "Method not allowed", null));
            return;
        }

        String path = exchange.getRequestURI().getPath().substring(BASE_PATH.length());

        switch (path) {
            case "/register" :
                register(exchange);
                break;

            case "/login" :
                login(exchange);
                break;


            case "/refresh" :
                refresh(exchange);
                break;


            case "/logout" :
                logout(exchange);
                break;


            default:
                Response.response(exchange,404, new ApiResponse<>(404, "Not found", null));
        }

    }

    /*
    * Register
    * */
    private void register(HttpExchange exchange) throws IOException {
        try{
            rateLimit.beforeRegister(ClientIp.resolve(exchange, trustProxy));

            String body = RequestBody.read(exchange);
            RegisterRequest request = JsonUtil.fromJson(body, RegisterRequest.class);

            if (request == null) {
                throw new JsonSyntaxException("Bad request!");
            }

            UserResponse data = authService.register(request);
            Response.response(exchange, 201, new ApiResponse<>(201, "Register success, waiting for admin approval", data));

        }catch(TooManyRequestsException e) {
            tooManyRequests(exchange, e);
        }catch(IllegalStateException e) {
            // Server misconfiguration (ex: default role missing), not a client error
            logger.error("Request failed: {} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), e);
            Response.response(exchange,500, new ApiResponse<>(500, "Internal Server Error", null));
        }catch(IllegalArgumentException e) {
            Response.response(exchange,400, new ApiResponse<>(400, e.getMessage(), null));
        }catch(JsonSyntaxException e) {
            Response.response(exchange,400, new ApiResponse<>(400, "Invalid JSON body", null));
        }catch(DuplicateResourceException e) {
            Response.response(exchange,409, new ApiResponse<>(409, e.getMessage(), null));
        }catch(Exception e) {
            logger.error("Request failed: {} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), e);
            Response.response(exchange,500, new ApiResponse<>(500, "Internal Server Error", null));
        }

    }

    /*
    * Login
    * */
    private void login(HttpExchange exchange) throws IOException {
        String ip = ClientIp.resolve(exchange, trustProxy);
        String username = null;

        try {
            String body = RequestBody.read(exchange);
            LoginRequest request = JsonUtil.fromJson(body, LoginRequest.class);

            if (request == null) {
                throw new JsonSyntaxException("Bad request!");
            }

            username = request.getUsername();
            rateLimit.beforeLogin(ip, username);

            AuthTokenDto token = authService.login(request);
            rateLimit.loginSucceeded(ip, username);

            // Set refresh token to http cookie
            exchange.getResponseHeaders().add("Set-Cookie", "refresh_token=" + token.getRefreshToken() + "; HttpOnly; Secure; Path=/api/v1/auth; Max-Age=" + token.getRefreshTokenExpiresIn() + "; SameSite=Strict");

            AuthResponse data = new AuthResponse(token.getAccessToken(), token.getAccessTokenExpiresIn());
            Response.response(exchange, 200, new ApiResponse<>(200, "Login Success", data));

        }catch(TooManyRequestsException e) {
            tooManyRequests(exchange, e);
        }catch(IllegalArgumentException e) {
            Response.response(exchange,400, new ApiResponse<>(400, e.getMessage(), null));
        }catch(UnauthorizedException e) {
            // Wrong username / password, counted for the brute force limit
            rateLimit.loginFailed(ip, username);
            Response.response(exchange,401, new ApiResponse<>(401, e.getMessage(), null));
        }catch(ForbiddenException e) {
            // Account pending approval or deactivated
            Response.response(exchange,403, new ApiResponse<>(403, e.getMessage(), null));
        }catch(JsonSyntaxException e) {
            Response.response(exchange,400, new ApiResponse<>(400, "Invalid JSON body", null));
        }catch(Exception e) {
            logger.error("Request failed: {} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), e);
            Response.response(exchange,500, new ApiResponse<>(500, "Internal Server Error", null));
        }
    }

    /*
    * Refresh
    * */
    private void refresh(HttpExchange exchange) throws IOException {
        String refreshToken = readRefreshCookie(exchange);

        try {
            AuthTokenDto token = authService.refresh(refreshToken);

            // Set new refresh token to cookie
            exchange.getResponseHeaders().add("Set-Cookie", "refresh_token=" + token.getRefreshToken() + "; HttpOnly; Secure; Path=/api/v1/auth; Max-Age=" + token.getRefreshTokenExpiresIn() + "; SameSite=Strict");

            AuthResponse data = new AuthResponse(token.getAccessToken(), token.getAccessTokenExpiresIn());
            Response.response(exchange, 200, new ApiResponse<>(200, "Access Token Generated", data));

        }catch(UnauthorizedException e) {
            Response.response(exchange,401, new ApiResponse<>(401, e.getMessage(), null));
        }
        catch(Exception e) {
            logger.error("Request failed: {} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), e);
            Response.response(exchange,500, new ApiResponse<>(500, "Internal Server Error", null));
        }
    }

    /*
    * Logout
    * */
    private void logout(HttpExchange exchange) throws IOException {
        String refreshToken = readRefreshCookie(exchange);

        try {
            authService.logout(refreshToken);

            // Clear refresh token cookie
            exchange.getResponseHeaders().add("Set-Cookie", "refresh_token=; HttpOnly; Secure; Path=/api/v1/auth; Max-Age=0; SameSite=Strict");

            Response.response(exchange, 200, new ApiResponse<>(200, "Logout Success", null));
        }catch(UnauthorizedException e) {
            Response.response(exchange,401, new ApiResponse<>(401, e.getMessage(), null));
        }catch(Exception e) {
            logger.error("Request failed: {} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), e);
            Response.response(exchange,500, new ApiResponse<>(500, "Internal Server Error", null));
        }
    }


    /*
     *  Helper Function
     *  Read refresh token from cookie
     * */
    private String readRefreshCookie(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Cookie");
        if (header == null) return null;

        for (String cookie : header.split(";")) {
            String[] kv = cookie.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals("refresh_token")) return kv[1];
        }
        return null;
    }

    /*
    * Helper
    * 429 response with Retry-After header (seconds)
    * */
    private void tooManyRequests(HttpExchange exchange, TooManyRequestsException e) throws IOException {
        exchange.getResponseHeaders().set("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
        Response.response(exchange, 429, new ApiResponse<>(429, e.getMessage(), null));
    }
}
