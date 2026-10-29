package shortlink.dto;

/*
* Auth Response
* */
public class AuthResponse {
    /*
    * Access token
    * */
    private String accessToken;

    /*
    * Expires in
    * */
    private Long expiresIn;

    /*
    * Constructor
    * */
    public AuthResponse(){}

    /*
    * Parameter constrcutor
    * */
    public AuthResponse(String accessToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }

    /*
    * Get access token
    * */
    public String getAccessToken() {
        return accessToken;
    }

    /*
     * Set access token
     * */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /*
     * Get Expires in
     * */
    public Long getExpiresIn() {
        return expiresIn;
    }

    /*
    * Set expires in
    * */
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
