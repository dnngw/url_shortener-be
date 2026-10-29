package shortlink.model;

import java.time.OffsetDateTime;

/*
* Refresh Token Model
* */
public class RefreshToken {
    /*
    * Id
    * */
    private Long id;

    /*
    * user id
    * */
    private Long userId;

    /*
    * Token hash
    * */
    private String tokenHash;

    /*
    * Revoked
    * */
    private Boolean revoked;

    /*
    * Created at
    * */
    private OffsetDateTime createdAt;

    /*
    * Expired at
    * */
    private OffsetDateTime expiredAt;


    /*
    * Constructor
    * */
    public RefreshToken(){}

    /*
    * Parameter Constructor
    * */
    public RefreshToken(Long id, Long userId, String tokenHash, Boolean revoked, OffsetDateTime createdAt, OffsetDateTime expiredAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.revoked = revoked;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
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
     * Get user id
     * */
    public Long getUserId() {
        return userId;
    }

    /*
    * Set user id
    * */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /*
    * Get token hash
    * */
    public String getTokenHash() {
        return tokenHash;
    }

    /*
     * Set token hash
     * */
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    /*
    * Get revoked
    * */
    public Boolean getRevoked() {
        return revoked;
    }

    /*
    * Set revoked
    * */
    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
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

    /*
    * Get expired at
    * */
    public OffsetDateTime getExpiredAt() {
        return expiredAt;
    }

    /*
    * Set expired at
    * */
    public void setExpiredAt(OffsetDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }
}
