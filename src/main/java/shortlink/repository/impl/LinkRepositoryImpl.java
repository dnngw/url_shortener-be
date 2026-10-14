package shortlink.repository.impl;

import shortlink.exception.DataAccessException;
import shortlink.model.Link;
import shortlink.repository.LinkRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*
* Link Repository Implementation
* */
public class LinkRepositoryImpl implements LinkRepository {
    /*
    * Data source
    * */
    private final DataSource dataSource;

    /*
    * Constructor
    * */
    public LinkRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    /*
    * Find by id
    * */
    @Override
    public Link findById(Long id) {
        Link data = null;
        String query = "SELECT * FROM Links WHERE id = (?)";

        try(Connection conn = dataSource.getConnection();PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();

            while(result.next()) {
                data = new Link();
                data.setId(result.getLong("id"));
                data.setShort_code(result.getString("short_code"));
                data.setOriginal_url(result.getString("original_url"));
                data.setCreated_at(result.getTimestamp("created_at").toLocalDateTime());
            }

        }catch(SQLException e) {
            throw new DataAccessException("Failed connect to database", e);
        }

        return data;

    }


    /*
    * Find original url by short code
    * */
    @Override
    public String findByShortCode(String shortCode) {
        String data = null;
        String query = "SELECT original_url FROM Links WHERE short_code = ?";

        try(Connection conn = dataSource.getConnection();PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shortCode);
            ResultSet result = statement.executeQuery();

            if(result.next()){
                data = result.getString("original_url");
            }

        }catch (SQLException e) {
            throw new DataAccessException("Failed connect to database", e);
        }

        return data;
    }


    /*
    * Save link
    * */
    @Override
    public Long save(Link link) {
        Long idValue = null;

        String query = "INSERT INTO Links (short_code, original_url) VALUES (?, ?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1,link.getShort_code());
            statement.setString(2,link.getOriginal_url());
            statement.executeUpdate();

            try(ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                  idValue = rs.getLong("id");
                }
            }

        }catch(SQLException e) {
            throw new DataAccessException("Failed connect to database", e);

        }

        return idValue;
    }

}
