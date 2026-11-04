package shortlink.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import shortlink.dto.AdminUserResponse;
import shortlink.dto.PageResponse;
import shortlink.exception.DataAccessException;
import shortlink.exception.NotFoundException;
import shortlink.service.AdminUserService;
import shortlink.util.ParseQuery;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.Response;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* Admin User Controller
*
* GET    /api/v1/admin/users?status=pending&q=&page=1&size=10   (status optional, empty = all)
* GET    /api/v1/admin/users/pending-count
* PATCH  /api/v1/admin/users/{id}/approve
* PATCH  /api/v1/admin/users/{id}/deactivate
* PATCH  /api/v1/admin/users/{id}/activate
* DELETE /api/v1/admin/users/{id}            (reject pending registration)
* */
public class AdminUserController implements HttpHandler {

    /*
    * BASE PATH
    * */
    private static final String BASE_PATH = "/api/v1/admin/users";

    /*
    * Default page and size
    * */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    /*
    * Path patterns
    * */
    private static final Pattern ID_PATH = Pattern.compile("^/(\\d{1,18})$");
    private static final Pattern ACTION_PATH = Pattern.compile("^/(\\d{1,18})/(approve|deactivate|activate)$");

    /*
    * Service
    * */
    private final AdminUserService service;

    /*
    * Constructor
    * */
    public AdminUserController(AdminUserService service) {
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
                    getUsers(exchange);
                } else if (subPath.equals("/pending-count")) {
                    getPendingCount(exchange);
                } else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }
                break;

            case "PATCH" :
                Matcher action = ACTION_PATH.matcher(subPath);
                if (action.matches()) {
                    changeStatus(exchange, Long.parseLong(action.group(1)), action.group(2));
                } else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }
                break;

            case "DELETE" :
                Matcher id = ID_PATH.matcher(subPath);
                if (id.matches()) {
                    reject(exchange, Long.parseLong(id.group(1)));
                } else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }
                break;

            default :
                Response.response(exchange, 405, new ApiResponse<>(405, "Method not allowed", null));
        }
    }

    /*
    * Get users (filter by status is optional)
    * */
    private void getUsers(HttpExchange exchange) throws IOException {
        Map<String, String> params = ParseQuery.parse(exchange.getRequestURI().getRawQuery());

        try {
            String status = params.get("status");
            int page = parseIntParam(params.get("page"), DEFAULT_PAGE);
            int size = parseIntParam(params.get("size"), DEFAULT_SIZE);

            PageResponse<AdminUserResponse> data = service.getUsers(status, page, size, params.get("q"));
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
    * Get pending count
    * */
    private void getPendingCount(HttpExchange exchange) throws IOException {
        try {
            Map<String, Integer> data = Map.of("count", service.countPending());
            Response.response(exchange, 200, new ApiResponse<>(200, "Success get data", data));

        }catch (DataAccessException e) {
            Response.response(exchange, 500, new ApiResponse<>(500, e.getMessage(), null));

        }catch (Exception e) {
            Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
        }
    }

    /*
    * Approve / deactivate / activate
    * */
    private void changeStatus(HttpExchange exchange, Long userId, String action) throws IOException {
        try {
            String message = switch (action) {
                case "approve" -> {
                    service.approve(userId);
                    yield "User approved";
                }
                case "deactivate" -> {
                    service.deactivate(userId);
                    yield "User deactivated";
                }
                default -> {
                    service.activate(userId);
                    yield "User activated";
                }
            };

            Response.response(exchange, 200, new ApiResponse<>(200, message, null));

        }catch (NotFoundException e) {
            Response.response(exchange, 404, new ApiResponse<>(404, e.getMessage(), null));

        }catch (IllegalStateException e) {
            // User is not in the expected status (ex: approve an active user)
            Response.response(exchange, 409, new ApiResponse<>(409, e.getMessage(), null));

        }catch (DataAccessException e) {
            Response.response(exchange, 500, new ApiResponse<>(500, e.getMessage(), null));

        }catch (Exception e) {
            Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
        }
    }

    /*
    * Reject pending registration (delete the account)
    * */
    private void reject(HttpExchange exchange, Long userId) throws IOException {
        try {
            service.reject(userId);
            Response.response(exchange, 200, new ApiResponse<>(200, "User rejected", null));

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
}
