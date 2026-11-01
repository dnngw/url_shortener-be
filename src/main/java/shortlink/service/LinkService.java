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
    * */
    PageResponse<Link> getAllByUserId(Long userId, int page, int size);

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
    * Update original url
    * */
    void updateOriginalUrl(Long id, Long userId, UpdateLinkRequest request);

    /*
    * Delete Link
    * */
    void delete(Long id, Long userId);

}
