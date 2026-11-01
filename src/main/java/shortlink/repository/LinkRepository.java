package shortlink.repository;

import shortlink.model.Link;

import java.util.List;

/*
* Link repository interface
* */
public interface LinkRepository {

    /*
    * Find all links by user id
    * */
    List<Link> findAllByUserId(Long userId, int limit, int offset);

    /*
    * Find link by id and userId
    * */
    Link findByIdAndUserId(Long id, Long userId);

    /*
    * Count total links by user id
    * */
    int countByUserId(Long userId);

    /*
    * Find by id (without user verification)
    * */
    Link findById(Long id);

    /*
    * Find Original Url by short code
    * */
    String findByShortCode(String shortCode);

    /*
    * Update original url
    * */
    boolean updateOriginalUrl(Long id, Long userId, String originalUrl);

    /*
    * Save Link
    * */
    Long save(Link link);

    /*
    * Delete Link
    * */
    boolean deleteByIdAndUserId(Long id, Long userId);
}
