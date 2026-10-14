package shortlink.util.validator;

import java.net.URI;
import java.net.URISyntaxException;

/*
* Url validator
* */
public class UrlValidator {
    /*
    * Validate url rules
    *
    * Url not empthy
    * Url Length <= 2048
    * Contain scheme (http/https)
    * Contain host (xxx.com)
    * */
    public static boolean validate(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty!");
        } else if (url.length() > 2048) {
            throw new IllegalArgumentException("URL to long!");
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();

            if(!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalArgumentException("Invalid scheme url");
            }

            if (uri.getHost() == null) {
                throw new IllegalArgumentException("Invalid host url");
            }

        }catch(URISyntaxException e) {
            throw new IllegalArgumentException("URL format invalid");

        }

        return true;

    }
}
