package shortlink.dto;

import java.time.OffsetDateTime;

/*
* Admin Link Response
*
* Link data for the admin link list, includes the owner
* */
public class AdminLinkResponse {
    /*
    * Id
    * */
    private final Long id;

    /*
    * Short code
    * */
    private final String shortCode;

    /*
    * Original url
    * */
    private final String originalUrl;

    /*
    * Owner id and username
    * */
    private final Long ownerId;
    private final String ownerUsername;

    /*
    * Disabled by admin
    * */
    private final boolean disabled;

    /*
    * Created at
    * */
    private final OffsetDateTime createdAt;

    /*
    * Constructor
    * */
    public AdminLinkResponse(Long id, String shortCode, String originalUrl, Long ownerId, String ownerUsername,
                             boolean disabled, OffsetDateTime createdAt) {
        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.disabled = disabled;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerUsername() { return ownerUsername; }
    public boolean isDisabled() { return disabled; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
