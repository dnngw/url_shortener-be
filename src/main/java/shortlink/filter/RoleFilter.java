package shortlink.filter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import shortlink.util.http.ApiResponse;
import shortlink.util.http.Response;
import shortlink.util.security.RoleType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/*
* Role Filter
* */
public class RoleFilter extends Filter {
    /*
    * Allowed Roles
    * */
    private final Set<String> allowedRoles;

    public RoleFilter(RoleType... roles) {
        if (roles.length == 0) {
            throw new IllegalArgumentException("RoleFilter required at least one role!");
        }

        this.allowedRoles = Arrays.stream(roles)
                .map(RoleType::getDbName)
                .collect(Collectors.toUnmodifiableSet());
    }

    /*
    *
    * */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String role = (String) exchange.getAttribute(AuthFilter.ATTR_ROLE);

        if (role == null) {
            Response.response(exchange, 401, new ApiResponse<>(401, "Unauthorized", null));
            return;
        }

        if (!allowedRoles.contains(role)){
            Response.response(exchange, 403, new ApiResponse<>(403, "Forbidden", null));
            return;
        }

        // Continue to next filter or controller
        chain.doFilter(exchange);
    }


    @Override
    public String description() {
        return "Allow only roles : " + allowedRoles;
    }

}
