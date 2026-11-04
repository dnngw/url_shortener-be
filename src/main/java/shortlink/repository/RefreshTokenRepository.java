package shortlink.repository;

import java.time.OffsetDateTime;

public interface RefreshTokenRepository {
    void save(Long userId, String tokenHash, OffsetDateTime expiredAt);
    void revoke(String tokenHash);
    Long revokeIfValid(String tokenHash);

    /*
    * Revoke every active refresh token of a user (ex: account deactivated)
    * */
    void revokeAllByUserId(Long userId);

    /*
    * Delete tokens that can never be used again (expired or revoked)
    *
    * @Return number of deleted tokens
    * */
    int deleteExpiredAndRevoked();
}
