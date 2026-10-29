package shortlink.model;

import java.time.OffsetDateTime;

/*
* User Model
* */
public class User {
    /*
    * Id
    * */
    private Long id;

    /*
    * Role
    * */
    private Role role;

    /*
    * Username
    * */
    private String username;

    /*
    * Password hash
    * */
    private String passwordHash;

    /*
    * Created At
    * */
    private OffsetDateTime createdAt;

    /*
    * Default Constructor
    * */
    public User(){}

    /*
    * Parameter Constructor
    * */
    public User(Long id, Role role, String username, String passwordHash, OffsetDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.passwordHash = passwordHash;
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
     * Get role
     * */
    public Role getRole() {
        return role;
    }

    /*
     * Set role
     * */
    public void setRole(Role role) {
        this.role = role;
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
     * Get password hash
     * */
    public String getPasswordHash() {
        return passwordHash;
    }

    /*
    * Set password hash
    * */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /*
     * Get created at
     * */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /*
     * Set Created at
     * */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
