package shortlink.repository.impl;

import shortlink.exception.DataAccessException;
import shortlink.exception.DuplicateResourceException;
import shortlink.model.Link;
import shortlink.model.LinkWithOwner;
import shortlink.repository.LinkRepository;
import shortlink.util.SearchPattern;
import shortlink.util.SqlErrors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
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
    public List<Link> findAllByUserId(Long userId, String keyword, int limit, int offset) {
        List<Link> links = new ArrayList<>();

        String query = """
                SELECT * FROM links where user_id = (?)
                AND (short_code ILIKE (?) ESCAPE '\\' OR original_url ILIKE (?) ESCAPE '\\')
                ORDER BY created_at DESC, id DESC
                LIMIT (?) OFFSET (?) 
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            String pattern = SearchPattern.contains(keyword);

            statement.setLong(1, userId);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setInt(4, limit);
            statement.setInt(5, offset);

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
    public int countByUserId(Long userId, String keyword) {
        String query = """
                SELECT COUNT(*) FROM links where user_id = (?)
                AND (short_code ILIKE (?) ESCAPE '\\' OR original_url ILIKE (?) ESCAPE '\\')
                """;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            String pattern = SearchPattern.contains(keyword);

            statement.setLong(1, userId);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
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
            // Two links with the same short code created at the same time
            if (SqlErrors.isUniqueViolation(e)) {
                throw new DuplicateResourceException("short code already exist");
            }
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
    * Count all links
    * */
    @Override
    public int countAll() {
        String query = "SELECT COUNT(*) FROM links";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            ResultSet result = statement.executeQuery();

            return result.next() ? result.getInt(1) : 0;

        }catch(SQLException e) {
            throw new DataAccessException("Failed to connect database", e);
        }
    }

    /*
    * Find link by short code
    * */
    @Override
    public Link findLinkByShortCode(String shortCode) {
        String query = "SELECT * FROM links WHERE short_code = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, shortCode);
            ResultSet result = statement.executeQuery();

            return result.next() ? MapLink(result) : null;

        }catch (SQLException e) {
            throw new DataAccessException("Failed connect to database", e);
        }
    }

    /*
    * Filter shared by the admin list and count query.
    * COALESCE with a null param keeps the column value, so that condition is always true
    * */
    private static final String ADMIN_LINK_FILTER = """
            WHERE l.disabled = COALESCE((?), l.disabled)
            AND l.user_id = COALESCE((?), l.user_id)
            AND (l.short_code ILIKE (?) ESCAPE '\\' OR l.original_url ILIKE (?) ESCAPE '\\' OR u.username ILIKE (?) ESCAPE '\\')
            """;

    /*
    * Set params of ADMIN_LINK_FILTER, starting from index 1
    *
    * @Return next param index
    * */
    private int setAdminLinkFilter(PreparedStatement statement, Boolean disabled, Long ownerId, String keyword) throws SQLException {
        String pattern = SearchPattern.contains(keyword);

        statement.setObject(1, disabled, Types.BOOLEAN);
        statement.setObject(2, ownerId, Types.BIGINT);
        statement.setString(3, pattern);
        statement.setString(4, pattern);
        statement.setString(5, pattern);

        return 6;
    }

    /*
    * Find all with owner
    * */
    @Override
    public List<LinkWithOwner> findAllWithOwner(Boolean disabled, Long ownerId, String keyword, int limit, int offset) {
        List<LinkWithOwner> links = new ArrayList<>();

        String query = "SELECT l.*, u.username AS owner_username FROM links l JOIN users u ON u.id = l.user_id "
                + ADMIN_LINK_FILTER
                + "ORDER BY l.created_at DESC, l.id DESC LIMIT (?) OFFSET (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            int index = setAdminLinkFilter(statement, disabled, ownerId, keyword);
            statement.setInt(index, limit);
            statement.setInt(index + 1, offset);

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                links.add(new LinkWithOwner(MapLink(result), result.getString("owner_username")));
            }

        }catch (SQLException e) {
            throw new DataAccessException("Failed to connect database", e);
        }

        return links;
    }

    /*
    * Count all with owner
    * */
    @Override
    public int countAllWithOwner(Boolean disabled, Long ownerId, String keyword) {
        String query = "SELECT COUNT(*) FROM links l JOIN users u ON u.id = l.user_id " + ADMIN_LINK_FILTER;

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            setAdminLinkFilter(statement, disabled, ownerId, keyword);

            ResultSet result = statement.executeQuery();

            return result.next() ? result.getInt(1) : 0;

        }catch (SQLException e) {
            throw new DataAccessException("Failed to connect database", e);
        }
    }

    /*
    * Update disabled
    *
    * "fromDisabled" in WHERE makes the change safe when two admins act at the same time
    * */
    @Override
    public boolean updateDisabled(Long id, boolean fromDisabled, boolean toDisabled) {
        String query = "UPDATE links SET disabled = (?) WHERE id = (?) AND disabled = (?)";

        try(Connection conn = dataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setBoolean(1, toDisabled);
            statement.setLong(2, id);
            statement.setBoolean(3, fromDisabled);

            return statement.executeUpdate() > 0;

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
        link.setDisabled(result.getBoolean("disabled"));
        return link;
    }

}
