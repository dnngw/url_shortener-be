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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
    * Find all by user id
    * */
    @Override
    public List<Link> findAllByUserId(Long userId, int limit, int offset) {
        List<Link> links = new ArrayList<>();

        String query = """
                SELECT * FROM links where user_id = (?)
                ORDER BY created_at DESC, id DESC
                LIMIT (?) OFFSET (?) 
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            ResultSet result = statement.executeQuery();

            while(result.next()) {
                links.add(MapLink(result));
            }

        }catch (SQLException e) {
            throw new DataAccessException("Failed to connect database", e);
        }

        return links;
    }

    /*
    * Find by id and user id
    * */
    @Override
    public Link findByIdAndUserId(Long id, Long userId) {
        String query = "SELECT * FROM links WHERE id = (?) AND user_id = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, id);
            statement.setLong(2, userId);

            ResultSet result = statement.executeQuery();

            if(result.next()) {
                return MapLink(result);
            }

            return null;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to connect database!", e);
        }
    }


    /*
    * Count all Links by user id
    * */
    @Override
    public int countByUserId(Long userId) {
        String query = "SELECT COUNT(*) FROM links where user_id = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet result = statement.executeQuery();

            if(result.next()) {
                return result.getInt(1);
            }

            return 0;

        }catch(SQLException e) {
            throw  new DataAccessException("Failed to connect database" , e);
        }
    }

    /*
    * Find Link by id
    * */
    @Override
    public Link findById(Long id) {
        Link data = null;
        String query = "SELECT * FROM links WHERE id = (?)";

        try(Connection conn = dataSource.getConnection();PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                data = MapLink(result);
                return data;
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
        String query = "SELECT original_url FROM links WHERE short_code = ?";

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
    * Update original url
    * */
    @Override
    public boolean updateOriginalUrl(Long id, Long userId, String originalUrl) {
        String query = "UPDATE links SET original_url = (?) WHERE id = (?) AND user_id = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, originalUrl);
            statement.setLong(2, id);
            statement.setLong(3, userId);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to access database", e);
        }
    }

    /*
    * Save link
    * */
    @Override
    public Long save(Link link) {
        Long idValue = null;

        String query = "INSERT INTO links (user_id, short_code, original_url) VALUES (?, ?, ?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, link.getUserId());
            statement.setString(2,link.getShortCode());
            statement.setString(3,link.getOriginalUrl());
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

    /*
    * Delete by id and user id
    * */
    @Override
    public boolean deleteByIdAndUserId(Long id, Long userId ) {
        String query = "DELETE FROM links WHERE id = (?) and user_id = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setLong(1, id);
            statement.setLong(2, userId);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to connect database", e);
        }

    }



    /*
    * Map result of link
    * */
    private Link MapLink(ResultSet result) throws SQLException {
        Link link = new Link();
        link.setId(result.getLong("id"));
        link.setUserId(result.getLong("user_id"));
        link.setShortCode(result.getString("short_code"));
        link.setOriginalUrl(result.getString("original_url"));
        link.setCreatedAt( result.getObject("created_at", OffsetDateTime.class));
        return link;
    }

}
