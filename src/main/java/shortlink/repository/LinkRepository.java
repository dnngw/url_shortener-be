package shortlink.repository;

import shortlink.model.Link;
import shortlink.model.LinkWithOwner;

import java.util.List;

/*
* Link repository interface
* */
public interface LinkRepository {

    /*
    * Find all links by user id
    *
    * keyword : filter by short code or original url (case insensitive), empty = no filter
    * */
    List<Link> findAllByUserId(Long userId, String keyword, int limit, int offset);

    /*
    * Find link by id and userId
    * */
    Link findByIdAndUserId(Long id, Long userId);

    /*
    * Count total links by user id
    *
    * keyword : same filter as findAllByUserId, so total pages stay correct
    * */
    int countByUserId(Long userId, String keyword);

    /*
    * Count every link in the app (admin dashboard)
    * */
    int countAll();

    /*
    * Find every link with its owner (admin)
    *
    * disabled : null = every link
    * ownerId  : null = every owner
    * keyword  : filter by short code, original url or owner username, empty = no filter
    * */
    List<LinkWithOwner> findAllWithOwner(Boolean disabled, Long ownerId, String keyword, int limit, int offset);

    /*
    * Count links, same filter as findAllWithOwner
    * */
    int countAllWithOwner(Boolean disabled, Long ownerId, String keyword);

    /*
    * Change disabled flag only when the link currently has the expected value
    *
    * @Return false when link not found or the flag already changed
    * */
    boolean updateDisabled(Long id, boolean fromDisabled, boolean toDisabled);

    /*
    * Find by id (without user verification)
    * */
    Link findById(Long id);

    /*
    * Find Original Url by short code
    * */
    String findByShortCode(String shortCode);

    /*
    * Find link by short code (used by redirect, includes disabled flag)
    * */
    Link findLinkByShortCode(String shortCode);

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
