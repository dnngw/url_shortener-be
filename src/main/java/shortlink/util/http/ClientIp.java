package shortlink.util.http;

import com.sun.net.httpserver.HttpExchange;

/*
* Client IP
*
* Find the ip address of the visitor, used as rate limit key
* */
public final class ClientIp {

    private ClientIp() {}

    /*
    * Resolve client ip
    *
    * trustProxy = false : ip of the direct connection (app is reached directly)
    * trustProxy = true  : app runs behind a reverse proxy (ex: Nginx), every connection comes from the proxy,
    *                      so the real ip is read from X-Forwarded-For.
    *
    * The LAST entry of X-Forwarded-For is used : the proxy appends the ip it saw,
    * earlier entries come from the client and can be faked.
    * Only enable trustProxy when the app is not reachable without the proxy.
    * */
    public static String resolve(HttpExchange exchange, boolean trustProxy) {
        if (trustProxy) {
            String forwardedFor = exchange.getRequestHeaders().getFirst("X-Forwarded-For");

            if (forwardedFor != null && !forwardedFor.isBlank()) {
                String[] ips = forwardedFor.split(",");
                return ips[ips.length - 1].trim();
            }
        }

        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
