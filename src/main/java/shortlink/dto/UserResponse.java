package shortlink.dto;

import java.time.OffsetDateTime;

/*
* User Response
* */
public class UserResponse {
    /*
    * Id
    * */
    private Long id;

    /*
    * Username
    * */
    private String username;

    /*
    * Role
    * */
    private String role;

    /*
    * Created at
    * */
    private OffsetDateTime createdAt;


    /*
    * Constructor
    * */
    public UserResponse(){}

    /*
    * Parameter constructor
    * */
    public UserResponse(Long id, String username, String role, OffsetDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
    }

    /*
    * Get id
    * */
    public Long getId() {
        return id;
    }

    /*
     * Set id
     * */
    public void setId(Long id) {
        this.id = id;
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
    * Get role
    * */
    public String getRole() {
        return role;
    }

    /*
     * Set role
     * */
    public void setRole(String role) {
        this.role = role;
    }

    /*
     * Get created at
     * */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /*
     * Set created at
     * */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
