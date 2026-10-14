package shortlink.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/*
* Database connection
* */
public class DatabaseConnection {
    /*
    * Instance
    * */
    private static HikariDataSource dataSource;

    /*
    * Init hikaricp
    * */
    public static void init() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppProperties.properties().getProperty("jdbc.uri"));
        config.setUsername(AppProperties.properties().getProperty("jdbc.username"));
        config.setPassword(AppProperties.properties().getProperty("jdbc.password"));

        dataSource = new HikariDataSource(config);

    }


    /*
    * Get datasource hikaricp
    * */
    public static DataSource getDataSource() {
        return dataSource;
    }

    /*
    * Close datasource hikaricp
    * */
    public static void close() {
        if(dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
