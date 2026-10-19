package shortlink.util.validator;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/*
* User Validator
* */
public final class UserValidator {

    /*
    * Constructor
    * */
    private UserValidator(){}

    /*
    * Rules for username
    *
    * Support letter (a-z)
    * Support number (0-9)
    * Username always lowercase
    * */
    private static final Pattern USERNAME_RULES = Pattern.compile("^[a-z0-9]{5,50}$");

    /*
    * Validate Username
    * */
    public static String validateUsername(String rawUsername) {
        if (rawUsername == null) {
            throw new IllegalArgumentException("Username is required");
        }

        String username = rawUsername.trim().toLowerCase();

        if(!USERNAME_RULES.matcher(username).matches()) {
            throw new IllegalArgumentException("Username should be 5-50 characters, and only letters or numbers");
        }

        return username;
    }

    /*
    * Validate password
    * */
    public static void validatePassword(String rawPassword) {
        /*
        * Password not null
        * password length >= 8
        * */
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("password length at least 8 character");
        }

        /*
        * Maximum password size 72 bytes
        * Bcrypt only support maximum 72 bytes
        * */
        if (rawPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password too long");
        }
    }

}
