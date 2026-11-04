package shortlink.repository.impl;

import shortlink.exception.DataAccessException;
import shortlink.exception.DuplicateResourceException;
import shortlink.model.Role;
import shortlink.model.User;
import shortlink.model.UserWithLinkCount;
import shortlink.repository.UserRepository;
import shortlink.util.SearchPattern;
import shortlink.util.SqlErrors;
import shortlink.util.security.UserStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/*
* User repository implementation
* */
public class UserRepositoryImpl implements UserRepository {
    /*
    * Data source
    * */
    private final DataSource dataSource;

    /*
    * Select user with role, shared by every find query
    * */
    private static final String SELECT_USER_WITH_ROLE = """
            SELECT u.id AS user_id, u.username, u.password_hash, u.status, u.created_at as user_created_at,
            r.id as role_id, r.role_name, r.role_description, r.created_at as role_created_at
            FROM users u JOIN roles r ON u.role_id = r.id
            """;

    /*
    * Constructor
    * */
    public UserRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /*
    * Find by username
    * */
    @Override
    public User findByUsername(String username) {
        String query = SELECT_USER_WITH_ROLE + "WHERE u.username = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet result = statement.executeQuery();

            return result.next() ? mapUser(result) : null;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }
    }

    /*
    * Find by id
    * */
    @Override
    public User findById(Long userId) {
        String query = SELECT_USER_WITH_ROLE + "WHERE u.id = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet result = statement.executeQuery();

            return result.next() ? mapUser(result) : null;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }
    }

    /*
    * Save
    *
    * Status is optional, database default (pending) is used when it is null
    * */
    @Override
    public User save(User user) {
        String query = """
                INSERT INTO users (role_id, username, password_hash, status)
                VALUES (?, ?, ?, COALESCE(?, 'pending'))
                RETURNING id, username, status, created_at
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user.getRole().getId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getStatus() == null ? null : user.getStatus().getDbName());

            ResultSet result =  statement.executeQuery();

            if(result.next()) {
                User saveUser = new User();
                saveUser.setId(result.getLong("id"));
                saveUser.setUsername(result.getString("username"));
                saveUser.setRole(user.getRole());
                saveUser.setStatus(UserStatus.fromDbName(result.getString("status")));
                saveUser.setCreatedAt( result.getObject("created_at", OffsetDateTime.class));
                return saveUser;
            }

         return null;
        }catch(SQLException e) {
            // Two registrations with the same username at the same time
            if (SqlErrors.isUniqueViolation(e)) {
                throw new DuplicateResourceException("Username already exist!");
            }
            throw new DataAccessException("Failed to access database", e);
        }
    }

    /*
    * Exists by role
    * */
    @Override
    public boolean existsByRole(String roleName) {
        String query = """
                SELECT EXISTS (
                    SELECT 1 FROM users u JOIN roles r ON u.role_id = r.id WHERE r.role_name = (?)
                )
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, roleName);
            ResultSet result = statement.executeQuery();

            return result.next() && result.getBoolean(1);

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }
    }

    /*
    * Find all by role and status
    *
    * Pending users first (they need an action from admin), then newest first.
    * Null status = every status, COALESCE keeps the column value so the condition is always true
    *
    * link_count is counted only for the users of the current page, using index idx_links_user
    * */
    @Override
    public List<UserWithLinkCount> findAllByRoleAndStatus(String roleName, UserStatus status, String keyword, int limit, int offset) {
        List<UserWithLinkCount> users = new ArrayList<>();

        String query = """
                SELECT u.id AS user_id, u.username, u.password_hash, u.status, u.created_at as user_created_at,
                r.id as role_id, r.role_name, r.role_description, r.created_at as role_created_at,
                (SELECT COUNT(*) FROM links l WHERE l.user_id = u.id) AS link_count
                FROM users u JOIN roles r ON u.role_id = r.id
                WHERE r.role_name = (?) AND u.status = COALESCE((?), u.status) AND u.username ILIKE (?) ESCAPE '\\'
                ORDER BY (u.status = 'pending') DESC, u.created_at DESC, u.id DESC
                LIMIT (?) OFFSET (?)
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, roleName);
            statement.setString(2, status == null ? null : status.getDbName());
            statement.setString(3, SearchPattern.contains(keyword));
            statement.setInt(4, limit);
            statement.setInt(5, offset);

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                users.add(new UserWithLinkCount(mapUser(result), result.getInt("link_count")));
            }

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }

        return users;
    }

    /*
    * Count by role and status
    * */
    @Override
    public int countByRoleAndStatus(String roleName, UserStatus status, String keyword) {
        String query = """
                SELECT COUNT(*) FROM users u JOIN roles r ON u.role_id = r.id
                WHERE r.role_name = (?) AND u.status = COALESCE((?), u.status) AND u.username ILIKE (?) ESCAPE '\\'
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, roleName);
            statement.setString(2, status == null ? null : status.getDbName());
            statement.setString(3, SearchPattern.contains(keyword));

            ResultSet result = statement.executeQuery();

            return result.next() ? result.getInt(1) : 0;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }
    }

    /*
    * Update status
    *
    * "fromStatus" in WHERE makes the change safe when two admins act at the same time
    * */
    @Override
    public boolean updateStatus(Long userId, String roleName, UserStatus fromStatus, UserStatus toStatus) {
        String query = """
                UPDATE users SET status = (?)
                WHERE id = (?) AND status = (?)
                AND role_id = (SELECT id FROM roles WHERE role_name = (?))
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, toStatus.getDbName());
            statement.setLong(2, userId);
            statement.setString(3, fromStatus.getDbName());
            statement.setString(4, roleName);

            return statement.executeUpdate() > 0;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }
    }

    /*
    * Delete by id and status
    * */
    @Override
    public boolean deleteByIdAndStatus(Long userId, String roleName, UserStatus status) {
        String query = """
                DELETE FROM users
                WHERE id = (?) AND status = (?)
                AND role_id = (SELECT id FROM roles WHERE role_name = (?))
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setString(2, status.getDbName());
            statement.setString(3, roleName);

            return statement.executeUpdate() > 0;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }
    }

    /*
    * Map result of user with role
    * */
    private User mapUser(ResultSet result) throws SQLException {
        Role role = new Role(
                result.getInt("role_id"),
                result.getString("role_name"),
                result.getString("role_description"),
                result.getObject("role_created_at", OffsetDateTime.class)
        );

        User user = new User(
                result.getLong("user_id"),
                role,
                result.getString("username"),
                result.getString("password_hash"),
                result.getObject("user_created_at", OffsetDateTime.class)
        );
        user.setStatus(UserStatus.fromDbName(result.getString("status")));

        return user;
    }
}
