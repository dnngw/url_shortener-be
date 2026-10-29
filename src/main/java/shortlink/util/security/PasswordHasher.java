package shortlink.util.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {
    /*
    * Create hash
    * */
    public static String hash(String rawPassword) {
        return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    }

    /*
    * verify hash
    * */
    public static Boolean verify(String rawPassword, String hash) {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), hash).verified;
    }
}
