package shortlink.repository.impl;

import shortlink.exception.DataAccessException;
import shortlink.model.Role;
import shortlink.model.User;
import shortlink.repository.UserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

/*
* User Repository Implementation
* */
public class UserRepositoryImpl implements UserRepository {
    /*
    * Data source
    * */
    private final DataSource dataSource;

    /*
    * Constructor
    * */
    public UserRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /*
    * Find by username
    *
    * @Return User
    * */
    @Override
    public User findByUsername(String username) {
        String query = "SELECT u.id AS user_id, u.username, u.password_hash, u.created_at as user_created_at, r.id as role_id, r.role_name, r.role_description, r.created_at as role_created_at " +
                "FROM Users u JOIN roles r ON u.role_id = r.id WHERE u.username = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, username);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
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

                return user;

            }

            return null;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }
    }


    /*
    * Find by id
    *
    * @Return User
    * */
    @Override
    public User findById(Long userId) {
        String query = "SELECT u.id AS user_id, u.username, u.password_hash, u.created_at as user_created_at, r.id as role_id, r.role_name, r.role_description, r.created_at as role_created_at " +
                "FROM Users u JOIN roles r ON u.role_id = r.id WHERE u.id = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, userId);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
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

                return user;

            }

        return null;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database!", e);
        }

    }

    /*
    * Save
    *
    * @Return user
    * */
    @Override
    public User save(User user) {
        String query = "INSERT INTO users (role_id, username, password_hash) VALUES (?,?,?) RETURNING id, username, created_at";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, user.getRole().getId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPasswordHash());

            ResultSet result =  statement.executeQuery();

            if(result.next()) {
                User saveUser = new User();
                saveUser.setId(result.getLong("id"));
                saveUser.setUsername(result.getString("username"));
                saveUser.setRole(user.getRole());
                saveUser.setCreatedAt( result.getObject("created_at", OffsetDateTime.class));

                return saveUser;

            }

         return null;

        }catch(SQLException e) {
            throw new DataAccessException("Failed to access database", e);
        }
    }



}
