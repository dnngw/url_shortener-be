package shortlink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
* App properties
* */
public class AppProperties {
    /*
    * Logger instance
    * */
    private static final Logger logger = LoggerFactory.getLogger(AppProperties.class);

    private static Properties cachedProps;

    public static Properties properties() {
        Properties prop = new Properties();

        if(cachedProps != null) {
            return cachedProps;
        }

        try(InputStream input = AppProperties.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                prop.load(input);
            } else {
                logger.warn("Application properties not found");
            }
        }catch (IOException e) {
            logger.error("Failed to read the configuration", e);
        }

        cachedProps = prop;

        return prop;
    }
}
