package shortlink.repository.impl;

import shortlink.exception.DataAccessException;
import shortlink.model.Role;
import shortlink.repository.RoleRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

/*
* Role Repository Implementation
* */
public class RoleRepositoryImpl implements RoleRepository {

    /*
    * Data source
    * */
    private final DataSource dataSource;

    /*
    * Constructor
    * */
    public RoleRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /*
    * Find by name
    *
    * @return Role
    * */
    @Override
    public Role findByName(String roleName) {
        Role role = null;

        String query = "SELECT * FROM roles WHERE role_name = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)){
            statement.setString(1, roleName);

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                role = new Role();
                role.setId(result.getInt("id"));
                role.setRoleName(result.getString("role_name"));
                role.setRoleDescription(result.getString("role_description"));
                role.setCreatedAt(result.getObject("created_at", OffsetDateTime.class));
            }

        }catch(SQLException e) {
            throw new DataAccessException("Failed to connect database", e);
        }

        return role;

    }


}
