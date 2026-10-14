package shortlink.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import shortlink.dto.CreateLinkRequest;
import shortlink.exception.DataAccessException;
import shortlink.exception.DuplicateResourceException;
import shortlink.model.Link;
import shortlink.service.LinkService;
import shortlink.util.http.ApiResponse;
import shortlink.util.JsonUtil;
import shortlink.util.http.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LinkController implements HttpHandler {
    /*
    * Service
    * */
    private final LinkService service;

    public LinkController(LinkService service) {
        this.service = service;
    }

    /*
    * Router
    *
    * "POST"
    * api/v1/task
    * */
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch(method) {
            case "POST" :
                createTask(exchange);
                break;

            default :
                Response.response(exchange, 405, null);

        }
    }

    /*
    * Create Task Controller
    * */
    private void createTask(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            CreateLinkRequest request = JsonUtil.fromJson(body, CreateLinkRequest.class);

            try{
                Link link = service.create(request);
                ApiResponse<Link> res = new ApiResponse<>(200, "Success insert data", link);
                Response.response(exchange, 201, res);

            }catch (DuplicateResourceException e){
                ApiResponse<Link> res = new ApiResponse<>(409, e.getMessage(), null);
                Response.response(exchange, 409, res);

            }catch (DataAccessException e) {
                ApiResponse<Link> res = new ApiResponse<>(500, e.getMessage(), null);
                Response.response(exchange, 500, res);

            }catch (IllegalArgumentException e) {
                ApiResponse<Link> res = new ApiResponse<>(400, e.getMessage(), null);
                Response.response(exchange, 400, res);

            }catch (Exception e) {
                ApiResponse<Link> res = new ApiResponse<>(500, "Internal server error", null);
                Response.response(exchange, 500, res);
            }
    }


}
