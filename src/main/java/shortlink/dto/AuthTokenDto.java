package shortlink.dto;

/*
* Auth token DTO
* Access token goes to response body, refresh token goes to HttpOnly cookie
* */
public class AuthTokenDto {
    /*
    * Access token (JWT)
    * */
    private final String accessToken;

    /*
    * Access token lifetime in seconds
    * */
    private final long accessTokenExpiresIn;

    /*
    * Refresh token (raw value, only the hash is stored in database)
    * */
    private final String refreshToken;

    /*
    * Refresh token lifetime in seconds
    * */
    private final long refreshTokenExpiresIn;

    /*
    * Constructor
    * */
    public AuthTokenDto(String accessToken, long accessTokenExpiresIn, String refreshToken, long refreshTokenExpiresIn) {
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    /*
    * Get access token
    * */
    public String getAccessToken() {
        return accessToken;
    }

    /*
     * Get access token expires in
     * */
    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    /*
     * Get refresh token
     * */
    public String getRefreshToken() {
        return refreshToken;
    }

    /*
     * Get refresh toekn expires in
     * */
    public long getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }
}
