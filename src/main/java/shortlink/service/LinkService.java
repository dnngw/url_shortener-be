package shortlink.service;

import shortlink.dto.CreateLinkRequest;
import shortlink.dto.PageResponse;
import shortlink.dto.UpdateLinkRequest;
import shortlink.model.Link;

/*
* Link service
* */
public interface LinkService {

    /*
    * Get all Links by user id
    *
    * keyword : search by short code or original url, null / blank = no filter
    * */
    PageResponse<Link> getAllByUserId(Long userId, int page, int size, String keyword);

    /*
    * Get link by id and user id
    * */
     Link getByIdAndUserId(Long id, Long userId);

    /*
    * Create Link
    * */
    Link create(CreateLinkRequest req, Long userId);

    /*
    * Get original url by short code
    * */
    String getByShortCode(String shortCode);

    /*
    * Get link for redirect, null when the short code does not exist
    * */
    Link getForRedirect(String shortCode);

    /*
    * Update original url
    * */
    void updateOriginalUrl(Long id, Long userId, UpdateLinkRequest request);

    /*
    * Delete Link
    * */
    void delete(Long id, Long userId);

}
