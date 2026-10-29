package shortlink.util.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/*
* jwt Token Provider
* */
public class JwtTokenProvider {
    /*
    * Secret Key
    * */
    private final SecretKey key;

    /*
    * Duration in minutes
    * */
    private final int duration;


    /*
    * Constructor
    * */
    public JwtTokenProvider(String secret, int duration) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.duration = duration;

    }

    /*
    * Generate access token
    * */
    public String generateAccessToken(Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(duration, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /*
    * parse
    * */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

}
