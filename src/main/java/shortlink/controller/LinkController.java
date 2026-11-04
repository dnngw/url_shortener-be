package shortlink.controller;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import shortlink.dto.CreateLinkRequest;
import shortlink.dto.PageResponse;
import shortlink.dto.UpdateLinkRequest;
import shortlink.exception.DataAccessException;
import shortlink.exception.DuplicateResourceException;
import shortlink.exception.NotFoundException;
import shortlink.filter.AuthFilter;
import shortlink.model.Link;
import shortlink.service.LinkService;
import shortlink.util.ParseQuery;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.RequestBody;
import shortlink.util.JsonUtil;
import shortlink.util.http.Response;

import java.io.IOException;
import java.util.Map;

public class LinkController implements HttpHandler {

    /*
    * BASE PATH
    * */

    private static final String BASE_PATH = "/api/v1/link";

    /*
    * Default page and size
    * */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    /*
    * Service
    * */
    private final LinkService service;

    /*
    * Constructor
    * */
    public LinkController(LinkService service) {
        this.service = service;
    }

    /*
    * Router
    * */
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String subPath = exchange.getRequestURI().getPath().substring(BASE_PATH.length());

        switch(method) {
            case "GET" :
                if (subPath.isEmpty()) {
                    getAllLinks(exchange);

                } else if (subPath.matches("/\\d{1,18}")) {
                    Long linkId = Long.parseLong(subPath.substring(1));
                    getLinkById(exchange, linkId);

                }else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }

                break;
            case "POST" :
                if (subPath.isEmpty()) {
                    createLink(exchange);
                }else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }
                break;

            case "PATCH" :
                if (subPath.matches("/\\d{1,18}")) {
                    Long linkId = Long.parseLong(subPath.substring(1));
                    updateLink(exchange, linkId);

                } else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }
                break;

            case "DELETE" :
                if (subPath.matches("/\\d{1,18}")) {
                    Long linkId = Long.parseLong(subPath.substring(1));
                    deleteLink(exchange, linkId);

                } else {
                    Response.response(exchange, 404, new ApiResponse<>(404, "Not found", null));
                }
                break;

            default :
                Response.response(exchange, 405, new ApiResponse<>(405, "method not allowed", null));

        }
    }

    private void getAllLinks(HttpExchange exchange) throws  IOException {
        Long userId = (Long) exchange.getAttribute(AuthFilter.ATTR_USER_ID);
        Map<String, String> params = ParseQuery.parse(exchange.getRequestURI().getRawQuery());

        try {
            int page = parseIntParam(params.get("page"), DEFAULT_PAGE);
            int size = parseIntParam(params.get("size"), DEFAULT_SIZE);

            String keyword = params.get("q");

            PageResponse<Link> data = service.getAllByUserId(userId, page, size, keyword);
            Response.response(exchange, 200, new ApiResponse<>(200, "Success get data", data));

        }catch (IllegalArgumentException e) {
            Response.response(exchange, 400, new ApiResponse<>(400, e.getMessage(), null));

        }catch (DataAccessException e) {
            Response.response(exchange, 500, new ApiResponse<>(500, e.getMessage(), null));

        }catch (Exception e) {
            Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
        }

    }

    private void getLinkById(HttpExchange exchange, Long linkId) throws IOException {
        Long userId = (Long) exchange.getAttribute(AuthFilter.ATTR_USER_ID);

        try {
            Link data = service.getByIdAndUserId(linkId, userId);
            Response.response(exchange, 200, new ApiResponse<>(200, "Success get data!", data));

        }catch(NotFoundException e) {
            Response.response(exchange, 404, new ApiResponse<>(404, "Link not found", null));

        }catch (IllegalArgumentException e) {
            Response.response(exchange, 400, new ApiResponse<>(400, e.getMessage(), null));

        }catch (Exception e) {
            Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
        }
    }

    /*
    * Create Link Controller
    * */
    private void createLink(HttpExchange exchange) throws IOException {
            Long userId = (Long) exchange.getAttribute(AuthFilter.ATTR_USER_ID);


            try{
                String body = RequestBody.read(exchange);
                CreateLinkRequest request = JsonUtil.fromJson(body, CreateLinkRequest.class);

                if (request == null) {
                    throw new JsonSyntaxException("Bad request");
                }

                Link link = service.create(request, userId);
                ApiResponse<Link> res = new ApiResponse<>(201, "Success insert data", link);
                Response.response(exchange, 201, res);

            }catch (JsonSyntaxException e) {
                ApiResponse<Link> res = new ApiResponse<>(400, e.getMessage(), null);
                Response.response(exchange, 400, res);

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

    /*
    * Update link
    * */
    private void updateLink(HttpExchange exchange, Long linkId) throws IOException {
        Long userId = (Long) exchange.getAttribute(AuthFilter.ATTR_USER_ID);

        try{
            String body = RequestBody.read(exchange);
            UpdateLinkRequest request = JsonUtil.fromJson(body, UpdateLinkRequest.class);

            if (request == null) {
                throw new JsonSyntaxException("Bad request");
            }

            service.updateOriginalUrl(linkId, userId, request);

            Response.response(exchange,200, new ApiResponse<>(200, "Success update data", null));

        }catch (DataAccessException e) {
            Response.response(exchange, 500, new ApiResponse<>(500, e.getMessage(), null));

        }catch (JsonSyntaxException e) {
            Response.response(exchange, 400, new ApiResponse<>(400, e.getMessage(), null));

        }catch(NotFoundException e) {
            Response.response(exchange, 404, new ApiResponse<>(404, "Link not found", null));

        }catch(IllegalArgumentException e) {
            Response.response(exchange, 400, new ApiResponse<>(400, e.getMessage(), null));

        }catch (Exception e) {
            Response.response(exchange, 500, new ApiResponse<>(500, "Internal server error", null));
        }
    }

    /*
    * Delete Link
    * */
    private void deleteLink(HttpExchange exchange, Long linkId) throws IOException {
        Long userId = (Long) exchange.getAttribute(AuthFilter.ATTR_USER_ID);

        try{
            service.delete(linkId, userId);
            Response.response(exchange, 200, new ApiResponse<>(200, "Success delete data", null));

        }catch (NotFoundException e) {
            Response.response(exchange, 404, new ApiResponse<>(404, e.getMessage(), null));

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
