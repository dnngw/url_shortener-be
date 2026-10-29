package shortlink.repository.impl;

import org.flywaydb.core.api.logging.Log;
import shortlink.exception.DataAccessException;
import shortlink.exception.UnauthorizedException;
import shortlink.repository.RefreshTokenRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;

/*
* Refresh Token Repository Implementation
* */
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    /*
    * Data source
    * */
    private final DataSource dataSource;


    /*
    * Constructor
    * */
    public RefreshTokenRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*
    * Save
    *
    * @Return void
    * */
    @Override
    public void save(Long userId, String tokenHash, OffsetDateTime expiredAt) {
        String query = "INSERT INTO refresh_tokens (user_id, token_hash, expired_at) VALUES (?,?,?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setString(2, tokenHash);
            statement.setObject(3, expiredAt);

            statement.executeUpdate();

        }catch (SQLException e){
            throw new DataAccessException("failed to access database");
        }

    }

    /*
    * Revoked
    *
    * @Return void
    * */
    @Override
    public void revoke(String tokenHash){
        String query = "UPDATE refresh_tokens SET revoked = (?) WHERE token_hash = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setBoolean(1, true);
            statement.setString(2, tokenHash);

            statement.executeUpdate();

        }catch(SQLException e) {
            throw new DataAccessException("failed to access database");
        }
    };

    /*
    * Revoke if Valid
    * */
    public Long revokeIfValid(String tokenHash) {
        String query = """
                UPDATE refresh_tokens SET revoked = TRUE WHERE token_hash = (?) AND revoked = FALSE 
                AND expired_at > now() RETURNING user_id
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)){
            statement.setString(1, tokenHash);
            ResultSet result = statement.executeQuery();

            if(result.next()) {
                Long user_id = result.getLong("user_id");
                return user_id;
            }
            throw new UnauthorizedException("Invalid refresh token");

        }catch(SQLException e){
            throw new DataAccessException("Failed to access database!");
        }
    }
}
