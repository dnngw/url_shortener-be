package shortlink.model;

import java.time.OffsetDateTime;

/*
* Link model
* */
public class Link {
    /*
    * Id
    * */
    private Long id;

    /*
    * User id
    * */
    private Long userId;

    /*
    * Short code
    * */
    private String shortCode;

    /*
    * Original url
    * */
    private String originalUrl;

    /*
    * Created at
    * */
    private OffsetDateTime createdAt;

    /*
    * Disabled by admin (short code returns 410 instead of redirecting)
    * */
    private boolean disabled;

    /*
    * Default Constructor
    * */
    public Link() {};


    /*
    * Constructor
    * */
    public Link(Long id, String shortCode, String originalUrl, OffsetDateTime createdAt) {
        this.id = id;
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
    }


    /*
    * Get id
    *
    * return id
    * */
    public Long getId() {
        return id;
    }

    /*
     * Set id
     *
     * param id
     * */
    public void setId(Long id) {
        this.id = id;
    }

    /*
     * Get user id
     *
     * return user id
     * */
    public Long getUserId() {
        return userId;
    }

    /*
     * Set user id
     *
     * param user id
     * */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /*
     * Get short code
     *
     * return short_code
     * */
    public String getShortCode() {
        return shortCode;
    }

    /*
     * Set short code
     *
     * param short_code
     * */
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    /*
     * Get original url
     *
     * return original_url
     * */
    public String getOriginalUrl() {
        return originalUrl;
    }


    /*
     * Set original url
     *
     * param original_url
     * */
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    /*
     * Get created at
     *
     * return created_at
     * */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }


    /*
    * Set created at
    *
    * param created_at
    * */
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /*
    * Is disabled
    *
    * return disabled
    * */
    public boolean isDisabled() {
        return disabled;
    }

    /*
    * Set disabled
    *
    * param disabled
    * */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
