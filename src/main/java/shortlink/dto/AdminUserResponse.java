package shortlink.dto;

import java.time.OffsetDateTime;

/*
* Admin User Response
*
* User data for the admin user list, includes the number of links.
* Password hash is never returned.
* */
public class AdminUserResponse {
    /*
    * Id
    * */
    private final Long id;

    /*
    * Username
    * */
    private final String username;

    /*
    * Role
    * */
    private final String role;

    /*
    * Account status (pending, active, inactive)
    * */
    private final String status;

    /*
    * Number of links created by the user
    * */
    private final int linkCount;

    /*
    * Created at
    * */
    private final OffsetDateTime createdAt;

    /*
    * Constructor
    * */
    public AdminUserResponse(Long id, String username, String role, String status, int linkCount, OffsetDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.status = status;
        this.linkCount = linkCount;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public int getLinkCount() { return linkCount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
