package shortlink.repository;

import java.time.OffsetDateTime;

public interface RefreshTokenRepository {
    void save(Long userId, String tokenHash, OffsetDateTime expiredAt);
    void revoke(String tokenHash);
    Long revokeIfValid(String tokenHash);
}
