package shortlink.config;

import org.flywaydb.core.Flyway;

/*
* Flayway Configuration
* */
public class FlywayInitializer {
    public static void migrate() {
        Flyway flayway = Flyway.configure()
                .dataSource(DatabaseConnection.getDataSource())
                .load();

        flayway.migrate();
    }
}
