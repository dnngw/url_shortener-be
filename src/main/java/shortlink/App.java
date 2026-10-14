package shortlink;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlink.config.AppProperties;
import shortlink.controller.LinkController;
import shortlink.config.DatabaseConnection;
import shortlink.controller.QrCodeController;
import shortlink.controller.RedirectController;
import shortlink.repository.impl.LinkRepositoryImpl;
import shortlink.service.impl.LinkServiceImpl;
import shortlink.service.impl.QrCodeServiceImpl;
import shortlink.config.FlywayInitializer;

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

        /*
        * Server Instance
        * */
        int port = Integer.parseInt(AppProperties.properties().getProperty("server.port", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        /*
        * Register http and wire depedencies
        * */
        server.createContext("/",
                new RedirectController(new LinkServiceImpl(new LinkRepositoryImpl(DatabaseConnection.getDataSource()))));
        server.createContext("/api/v1/link",
                new LinkController(new LinkServiceImpl(new LinkRepositoryImpl(DatabaseConnection.getDataSource()))));
        server.createContext("/api/v1/qr", new QrCodeController(new QrCodeServiceImpl()));

        /*
        * Thread pool configuration
        * */
        server.setExecutor(Executors.newFixedThreadPool(10));

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
