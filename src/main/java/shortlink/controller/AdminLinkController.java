package shortlink.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import shortlink.dto.AdminLinkResponse;
import shortlink.dto.PageResponse;
import shortlink.exception.DataAccessException;
import shortlink.exception.NotFoundException;
import shortlink.service.AdminLinkService;
import shortlink.util.ParseQuery;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.Response;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* Admin Link Controller
*
* GET   /api/v1/admin/links?status=all&userId=&q=&page=1&size=10   (status : all | active | disabled)
* PATCH /api/v1/admin/links/{id}/disable
* PATCH /api/v1/admin/links/{id}/enable
* */
public class AdminLinkController implements HttpHandler {

    /*
    * BASE PATH
    * */
    private static final String BASE_PATH = "/api/v1/admin/links";

    /*
    * Default page and size
    * */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    /*
    * Path pattern
    * */
    private static final Pattern ACTION_PATH = Pattern.compile("^/(\\d{1,18})/(disable|enable)$");

    /*
    * Service
    * */
    private final AdminLinkService service;

    /*
    * Constructor
    * */
    public AdminLinkController(AdminLinkService service) {
        this.service = service;
    }

    /*
    * Router
    * */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String subPath = exchange.getRequestURI().getPath().substring(BASE_PATH.length());

        switch (method) {
            case "GET" :
                if (subPath.isEmpty()) {
                    getLinks(exchange);
                } else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }
                break;

            case "PATCH" :
                Matcher action = ACTION_PATH.matcher(subPath);
                if (action.matches()) {
                    changeDisabled(exchange, Long.parseLong(action.group(1)), action.group(2));
                } else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }
                break;

            default :
                Response.response(exchange, 405, new ApiResponse<>(405, "Method not allowed", null));
        }
    }

    /*
    * Get links
    * */
    private void getLinks(HttpExchange exchange) throws IOException {
        Map<String, String> params = ParseQuery.parse(exchange.getRequestURI().getRawQuery());

        try {
            int page = parseIntParam(params.get("page"), DEFAULT_PAGE);
            int size = parseIntParam(params.get("size"), DEFAULT_SIZE);
            Long ownerId = parseLongParam(params.get("userId"));

            PageResponse<AdminLinkResponse> data = service.getLinks(params.get("status"), ownerId, page, size, params.get("q"));
            Response.response(exchange, 200, new ApiResponse<>(200, "Success get data", data));

        }catch (IllegalArgumentException e) {
            Response.response(exchange, 400, new ApiResponse<>(400, e.getMessage(), null));

        }catch (DataAccessException e) {
            Response.response(exchange, 500, new ApiResponse<>(500, e.getMessage(), null));

        }catch (Exception e) {
            Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
        }
    }

    /*
    * Disable / enable
    * */
    private void changeDisabled(HttpExchange exchange, Long linkId, String action) throws IOException {
        try {
            if (action.equals("disable")) {
                service.disable(linkId);
                Response.response(exchange, 200, new ApiResponse<>(200, "Link disabled", null));
            } else {
                service.enable(linkId);
                Response.response(exchange, 200, new ApiResponse<>(200, "Link enabled", null));
            }

        }catch (NotFoundException e) {
            Response.response(exchange, 404, new ApiResponse<>(404, e.getMessage(), null));

        }catch (IllegalStateException e) {
            Response.response(exchange, 409, new ApiResponse<>(409, e.getMessage(), null));

        }catch (DataAccessException e) {
            Response.response(exchange, 500, new ApiResponse<>(500, e.getMessage(), null));

        }catch (Exception e) {
            Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
        }
    }

    /*
     * Helper
     * Parse integer query param, use default value if empty
     * */
    private int parseIntParam(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("page and size must be a number");
        }
    }

    /*
     * Helper
     * Parse optional id query param, null if empty
     * */
    private Long parseLongParam(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("userId must be a number");
        }
    }
}
