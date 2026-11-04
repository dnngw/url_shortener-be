package shortlink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/*
* App properties
*
* Where the config is read from :
*   1. File from environment variable APP_CONFIG (production)
*      ex: APP_CONFIG=/etc/shortlink/application.properties java -jar shortlink.jar
*   2. application.properties on the classpath (development, src/main/resources)
*
* The built jar does not contain application.properties, so production must set APP_CONFIG.
* */
public class AppProperties {
    /*
    * Logger instance
    * */
    private static final Logger logger = LoggerFactory.getLogger(AppProperties.class);

    /*
    * Environment variable with the path of the config file
    * */
    private static final String CONFIG_ENV = "APP_CONFIG";

    private static Properties cachedProps;

    public static synchronized Properties properties() {
        if(cachedProps != null) {
            return cachedProps;
        }

        cachedProps = load();
        return cachedProps;
    }

    /*
    * Load config from APP_CONFIG or from the classpath
    *
    * @Throw IllegalStateException when no config is found, the app can not run without it
    * */
    private static Properties load() {
        Properties prop = new Properties();
        String configPath = System.getenv(CONFIG_ENV);

        // 1. External file (production)
        if (configPath != null && !configPath.isBlank()) {
            Path path = Path.of(configPath);

            try (InputStream input = Files.newInputStream(path)) {
                prop.load(input);
                logger.info("Configuration loaded from {}", path.toAbsolutePath());
                return prop;

            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read configuration file " + path.toAbsolutePath(), e);
            }
        }

        // 2. Classpath (development)
        try(InputStream input = AppProperties.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "Configuration not found. Set " + CONFIG_ENV + " to the path of application.properties");
            }

            prop.load(input);
            logger.info("Configuration loaded from classpath application.properties");
            return prop;

        }catch (IOException e) {
            throw new UncheckedIOException("Failed to read the configuration", e);
        }
    }
}
