package shortlink.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlink.dto.AdminStatsResponse;
import shortlink.exception.DataAccessException;
import shortlink.service.AdminStatsService;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.Response;

import java.io.IOException;

/*
* Admin Stats Controller
*
* GET /api/v1/admin/stats
* */
public class AdminStatsController implements HttpHandler {
    /*
    * Logger
    * */
    private static final Logger logger = LoggerFactory.getLogger(AdminStatsController.class);


    /*
    * BASE PATH
    * */
    private static final String BASE_PATH = "/api/v1/admin/stats";

    /*
    * Service
    * */
    private final AdminStatsService service;

    /*
    * Constructor
    * */
    public AdminStatsController(AdminStatsService service) {
        this.service = service;
    }

    /*
    * Router
    * */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String subPath = exchange.getRequestURI().getPath().substring(BASE_PATH.length());

        if (!subPath.isEmpty()) {
            Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            Response.response(exchange, 405, new ApiResponse<>(405, "Method not allowed", null));
            return;
        }

        try {
            AdminStatsResponse data = service.getStats();
            Response.response(exchange, 200, new ApiResponse<>(200, "Success get data", data));

        }catch (DataAccessException e) {
            logger.error("Request failed: {} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), e);
            Response.response(exchange, 500, new ApiResponse<>(500, e.getMessage(), null));

        }catch (Exception e) {
            logger.error("Request failed: {} {}", exchange.getRequestMethod(), exchange.getRequestURI().getPath(), e);
            Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
        }
    }
}
