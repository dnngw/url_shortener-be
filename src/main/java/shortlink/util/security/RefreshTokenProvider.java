package shortlink.util.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/*
* Refresh Token Provider
* */
public class RefreshTokenProvider {

    /*
    * RANDOM value
    * */
    private static final SecureRandom RANDOM = new SecureRandom();

    /*
    * Generate raw refresh token with encode Base64
    * */
    public static String generateRefreshToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /*
    * Hash raw refresh token to SHA-256
    * */
    public static String convertToSHA256(String rawRefreshToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8)));

        }catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
