package shortlink;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlink.config.AppProperties;
import shortlink.controller.AuthController;
import shortlink.controller.LinkController;
import shortlink.config.DatabaseConnection;
import shortlink.controller.QrCodeController;
import shortlink.controller.RedirectController;
import shortlink.filter.AuthFilter;
import shortlink.filter.RoleFilter;
import shortlink.repository.LinkRepository;
import shortlink.repository.RefreshTokenRepository;
import shortlink.repository.RoleRepository;
import shortlink.repository.UserRepository;
import shortlink.repository.impl.LinkRepositoryImpl;
import shortlink.repository.impl.RefreshTokenRepositoryImpl;
import shortlink.repository.impl.RoleRepositoryImpl;
import shortlink.repository.impl.UserRepositoryImpl;
import shortlink.service.AuthService;
import shortlink.service.LinkService;
import shortlink.service.QrCodeService;
import shortlink.service.impl.AuthServiceImpl;
import shortlink.service.impl.LinkServiceImpl;
import shortlink.service.impl.QrCodeServiceImpl;
import shortlink.config.FlywayInitializer;
import shortlink.util.security.JwtTokenProvider;
import shortlink.util.security.RoleType;

import javax.sql.DataSource;
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
        * Environment Configuration
        * */
        int port = Integer.parseInt(AppProperties.properties()
                .getProperty("server.port", "8080"));
        String secret = AppProperties.properties().getProperty("jwt.secret");
        int jwt_access_ttl_minutes = Integer.parseInt(AppProperties.properties().getProperty("jwt.access.ttl-minutes"));
        int jwt_refresh_ttl_days = Integer.parseInt(AppProperties.properties().getProperty("jwt.refresh.ttl-days"));

        /*
        * Repository
        * */
        DataSource dataSource = DatabaseConnection.getDataSource();
        UserRepository userRepository = new UserRepositoryImpl(dataSource);
        RoleRepository roleRepository = new RoleRepositoryImpl(dataSource);
        LinkRepository linkRepository = new LinkRepositoryImpl(dataSource);
        RefreshTokenRepository refreshTokenRepository = new RefreshTokenRepositoryImpl(dataSource);

        /*
         * Security
         * */
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(secret, jwt_access_ttl_minutes);
        AuthFilter authFilter = new AuthFilter(jwtTokenProvider);
        RoleFilter adminOnly = new RoleFilter(RoleType.ADMIN);
        RoleFilter userOnly = new RoleFilter(RoleType.USER);


        /*
        * Service
        * */
        AuthService authService = new AuthServiceImpl(userRepository, roleRepository, refreshTokenRepository,
                jwtTokenProvider, jwt_refresh_ttl_days, jwt_access_ttl_minutes);
        LinkService linkService = new LinkServiceImpl(linkRepository);
        QrCodeService qrCodeService = new QrCodeServiceImpl();



        /*
        * Server Instance
        * */
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        /*
        * Public Routes
        * */
        server.createContext("/", new RedirectController(linkService));
        server.createContext("/api/v1/auth", new AuthController(authService));

        /*
        * Protected Routes
        * */
        HttpContext linkContext = server.createContext("/api/v1/link", new LinkController(linkService));
        linkContext.getFilters().add(authFilter);
        linkContext.getFilters().add(userOnly);


        HttpContext qrContext = server.createContext("/api/v1/qr", new QrCodeController(qrCodeService));
        qrContext.getFilters().add(authFilter);
        qrContext.getFilters().add(userOnly);



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
