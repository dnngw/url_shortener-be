package shortlink;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlink.config.AdminSeeder;
import shortlink.config.AppProperties;
import shortlink.config.RefreshTokenCleaner;
import shortlink.controller.AdminLinkController;
import shortlink.controller.AdminStatsController;
import shortlink.controller.AdminUserController;
import shortlink.controller.AuthController;
import shortlink.controller.LinkController;
import shortlink.config.DatabaseConnection;
import shortlink.controller.QrCodeController;
import shortlink.controller.RedirectController;
import shortlink.filter.AuthFilter;
import shortlink.filter.CorsFilter;
import shortlink.filter.RoleFilter;
import shortlink.repository.LinkRepository;
import shortlink.repository.RefreshTokenRepository;
import shortlink.repository.RoleRepository;
import shortlink.repository.UserRepository;
import shortlink.repository.impl.LinkRepositoryImpl;
import shortlink.repository.impl.RefreshTokenRepositoryImpl;
import shortlink.repository.impl.RoleRepositoryImpl;
import shortlink.repository.impl.UserRepositoryImpl;
import shortlink.service.AdminLinkService;
import shortlink.service.AdminStatsService;
import shortlink.service.AdminUserService;
import shortlink.service.AuthService;
import shortlink.service.LinkService;
import shortlink.service.QrCodeService;
import shortlink.service.impl.AdminLinkServiceImpl;
import shortlink.service.impl.AdminStatsServiceImpl;
import shortlink.service.impl.AdminUserServiceImpl;
import shortlink.service.impl.AuthServiceImpl;
import shortlink.service.impl.LinkServiceImpl;
import shortlink.service.impl.QrCodeServiceImpl;
import shortlink.config.FlywayInitializer;
import shortlink.util.security.AuthRateLimit;
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
        // true when the app runs behind a reverse proxy, client ip is then read from X-Forwarded-For
        boolean trustProxy = Boolean.parseBoolean(AppProperties.properties().getProperty("server.trust-proxy", "false"));

        /*
        * Repository
        * */
        DataSource dataSource = DatabaseConnection.getDataSource();
        UserRepository userRepository = new UserRepositoryImpl(dataSource);
        RoleRepository roleRepository = new RoleRepositoryImpl(dataSource);
        LinkRepository linkRepository = new LinkRepositoryImpl(dataSource);
        RefreshTokenRepository refreshTokenRepository = new RefreshTokenRepositoryImpl(dataSource);

        /*
        * Seed first admin account (only when there is no admin yet)
        * */
        new AdminSeeder(userRepository, roleRepository).seed(AppProperties.properties());

        /*
        * Delete expired / revoked refresh tokens on start and every 24 hours
        * */
        new RefreshTokenCleaner(refreshTokenRepository).start();

        /*
         * Security
         * */
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(secret, jwt_access_ttl_minutes);
        AuthFilter authFilter = new AuthFilter(jwtTokenProvider);
        RoleFilter adminOnly = new RoleFilter(RoleType.ADMIN);
        RoleFilter userOnly = new RoleFilter(RoleType.USER);
        // Frontend origins allowed to call the API, comma separated (ex: http://localhost:5173)
        CorsFilter corsFilter = new CorsFilter(AppProperties.properties().getProperty("cors.allowed-origins", ""));


        /*
        * Service
        * */
        AuthService authService = new AuthServiceImpl(userRepository, roleRepository, refreshTokenRepository,
                jwtTokenProvider, jwt_refresh_ttl_days, jwt_access_ttl_minutes);
        LinkService linkService = new LinkServiceImpl(linkRepository);
        QrCodeService qrCodeService = new QrCodeServiceImpl();
        AdminUserService adminUserService = new AdminUserServiceImpl(userRepository, refreshTokenRepository);
        AdminStatsService adminStatsService = new AdminStatsServiceImpl(userRepository, linkRepository);
        AdminLinkService adminLinkService = new AdminLinkServiceImpl(linkRepository);

        /*
        * Server Instance
        * */
        // server.host = 127.0.0.1 → only reachable from the same machine (behind a reverse proxy)
        // empty → every network interface (development)
        String host = AppProperties.properties().getProperty("server.host", "").trim();
        InetSocketAddress address = host.isEmpty() ? new InetSocketAddress(port) : new InetSocketAddress(host, port);

        HttpServer server = HttpServer.create(address, 0);

        /*
        * Public Routes
        * */
        server.createContext("/", new RedirectController(linkService));
        HttpContext authContext = server.createContext("/api/v1/auth", new AuthController(authService, new AuthRateLimit(), trustProxy));
        authContext.getFilters().add(corsFilter);

        /*
        * Protected Routes
        * */
        HttpContext linkContext = server.createContext("/api/v1/link", new LinkController(linkService));
        linkContext.getFilters().add(corsFilter);
        linkContext.getFilters().add(authFilter);
        linkContext.getFilters().add(userOnly);


        HttpContext qrContext = server.createContext("/api/v1/qr", new QrCodeController(qrCodeService));
        qrContext.getFilters().add(corsFilter);
        qrContext.getFilters().add(authFilter);
        qrContext.getFilters().add(userOnly);

        /*
        * Admin Routes
        * */
        HttpContext adminUserContext = server.createContext("/api/v1/admin/users", new AdminUserController(adminUserService));
        adminUserContext.getFilters().add(corsFilter);
        adminUserContext.getFilters().add(authFilter);
        adminUserContext.getFilters().add(adminOnly);

        HttpContext adminStatsContext = server.createContext("/api/v1/admin/stats", new AdminStatsController(adminStatsService));
        adminStatsContext.getFilters().add(corsFilter);
        adminStatsContext.getFilters().add(authFilter);
        adminStatsContext.getFilters().add(adminOnly);

        HttpContext adminLinkContext = server.createContext("/api/v1/admin/links", new AdminLinkController(adminLinkService));
        adminLinkContext.getFilters().add(corsFilter);
        adminLinkContext.getFilters().add(authFilter);
        adminLinkContext.getFilters().add(adminOnly);

        /*
        * Thread pool configuration using virtual thread
        * */
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        /*
        * Run server
        * */
        logger.info("Server running on {}:{}", host.isEmpty() ? "0.0.0.0" : host, port);
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
