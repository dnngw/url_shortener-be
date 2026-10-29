package shortlink.dto;

public class RegisterRequest {
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
    public RegisterRequest(){}

    /*
    * Parameter constructor
    * */
    public RegisterRequest(String username, String password) {
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
    * Set password
    * */
    public void setPassword(String password) {
        this.password = password;
    }
}
