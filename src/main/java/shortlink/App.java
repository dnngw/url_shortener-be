package shortlink;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlink.config.AppProperties;
import shortlink.controller.AuthController;
import shortlink.controller.LinkController;
import shortlink.config.DatabaseConnection;
import shortlink.controller.QrCodeController;
import shortlink.controller.RedirectController;
import shortlink.repository.impl.LinkRepositoryImpl;
import shortlink.repository.impl.RefreshTokenRepositoryImpl;
import shortlink.repository.impl.RoleRepositoryImpl;
import shortlink.repository.impl.UserRepositoryImpl;
import shortlink.service.impl.AuthServiceImpl;
import shortlink.service.impl.LinkServiceImpl;
import shortlink.service.impl.QrCodeServiceImpl;
import shortlink.config.FlywayInitializer;
import shortlink.util.security.JwtTokenProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/*
* App
* */
public class App {
    /*
    * Logger
    * */
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /*
    * Entry point
    * */
    public static void main(String[] args) throws IOException {
        /*
        * Database init
        * */
        DatabaseConnection.init();

        /*
        * Database Migrate
        * */
        FlywayInitializer.migrate();

        int port = Integer.parseInt(AppProperties.properties()
                .getProperty("server.port", "8080"));
        String secret = AppProperties.properties().getProperty("jwt.secret");
        int jwt_access_ttl_minutes = Integer.parseInt(AppProperties.properties().getProperty("jwt.access.ttl-minutes"));
        int jwt_refresh_ttl_days = Integer.parseInt(AppProperties.properties().getProperty("jwt.refresh.ttl-days"));

        /*
        * Server Instance
        * */

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        /*
        * Router Implementation
        * */
        server.createContext("/",
                new RedirectController(new LinkServiceImpl(new LinkRepositoryImpl(DatabaseConnection.getDataSource()))));

        server.createContext("/api/v1/auth", new AuthController(new AuthServiceImpl
                (new UserRepositoryImpl(DatabaseConnection.getDataSource()), new RoleRepositoryImpl(DatabaseConnection.getDataSource()),
                        new RefreshTokenRepositoryImpl(DatabaseConnection.getDataSource()), new JwtTokenProvider(secret, jwt_access_ttl_minutes),
                        jwt_refresh_ttl_days, jwt_access_ttl_minutes)));


        server.createContext("/api/v1/link",
                new LinkController(new LinkServiceImpl(new LinkRepositoryImpl(DatabaseConnection.getDataSource()))));
        server.createContext("/api/v1/qr", new QrCodeController(new QrCodeServiceImpl()));

        /*
        * Thread pool configuration using virtual thread
        * */
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        /*
        * Run server
        * */
        logger.info("Server running on port : {}", port);
        server.start();


        /*
        * Program close
        * */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(3);
            DatabaseConnection.close();
        }));

    }
}
