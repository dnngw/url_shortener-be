package shortlink.dto;

/*
* Login Request
* */
public class LoginRequest {
    /*
    * Username
    * */
    private String username;

    /*
    * Password
    * */
    private String password;

    /*
    * Constructor
    * */
    public LoginRequest(){}

    /*
    * Parameter Constructor
    * */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /*
    * Get username
    * */
    public String getUsername() {
        return username;
    }

    /*
     * Set username
     * */
    public void setUsername(String username) {
        this.username = username;
    }

    /*
     * Get password
     * */
    public String getPassword() {
        return password;
    }

    /*
     * Get password
     * */
    public void setPassword(String password) {
        this.password = password;
    }
}
