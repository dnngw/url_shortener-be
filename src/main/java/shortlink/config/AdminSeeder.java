package shortlink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlink.model.Role;
import shortlink.model.User;
import shortlink.repository.RoleRepository;
import shortlink.repository.UserRepository;
import shortlink.util.security.PasswordHasher;
import shortlink.util.security.RoleType;
import shortlink.util.security.UserStatus;
import shortlink.util.validator.UserValidator;

import java.security.SecureRandom;
import java.util.Properties;

/*
* Admin Seeder
*
* Create the first admin account when the app starts and there is no admin yet.
*
* application.properties :
*   admin.username = username of the first admin (default "admin")
*   admin.password = password of the first admin
*                    when empty, a random password is generated and printed once in the log
*
* When an admin already exists nothing happens, so both values can be removed
* from application.properties after the first start.
* */
public class AdminSeeder {
    /*
    * Logger
    * */
    private static final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

    /*
    * Default username when admin.username is empty
    * */
    private static final String DEFAULT_USERNAME = "admin";

    /*
    * Random password rules (no similar looking characters like 0/O, 1/l)
    * */
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int PASSWORD_LENGTH = 20;

    /*
    * Repository
    * */
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /*
    * Constructor
    * */
    public AdminSeeder(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /*
    * Seed
    * */
    public void seed(Properties properties) {
        String adminRoleName = RoleType.ADMIN.getDbName();

        if (userRepository.existsByRole(adminRoleName)) {
            if (!properties.getProperty("admin.password", "").isBlank()) {
                logger.warn("Admin already exists, admin.password is ignored. Remove it from application.properties");
            }
            return;
        }

        Role adminRole = roleRepository.findByName(adminRoleName);
        if (adminRole == null) {
            throw new IllegalStateException("Role admin not found in database!");
        }

        String rawUsername = properties.getProperty("admin.username", "").trim();
        String username = UserValidator.validateUsername(rawUsername.isEmpty() ? DEFAULT_USERNAME : rawUsername);

        // Do not turn an existing normal account into admin silently
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalStateException("Cannot create admin, username \"" + username + "\" is already used by a user");
        }

        String configuredPassword = properties.getProperty("admin.password", "");
        boolean generated = configuredPassword.isBlank();
        String password = generated ? generatePassword() : UserValidator.validatePassword(configuredPassword);

        User admin = new User();
        admin.setUsername(username);
        admin.setPasswordHash(PasswordHasher.hash(password));
        admin.setRole(adminRole);
        // Admin does not need approval
        admin.setStatus(UserStatus.ACTIVE);

        userRepository.save(admin);

        if (generated) {
            logger.warn("Admin account \"{}\" created with a generated password : {}", username, password);
            logger.warn("Save this password now, it is only shown once");
        } else {
            logger.info("Admin account \"{}\" created. Remove admin.password from application.properties", username);
        }
    }

    /*
    * Generate random password
    * */
    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }

        return password.toString();
    }
}
