package shortlink.service;

import shortlink.dto.AdminLinkResponse;
import shortlink.dto.PageResponse;

/*
* Admin link service
*
* Admin can view every link and disable / enable it (moderation).
* Admin can not create links or change the destination url.
* */
public interface AdminLinkService {

    /*
    * Get every link with pagination
    *
    * status  : all, active or disabled. null / blank = all
    * ownerId : only links of this user, null = every user
    * keyword : search by short code, original url or owner username
    * */
    PageResponse<AdminLinkResponse> getLinks(String status, Long ownerId, int page, int size, String keyword);

    /*
    * Disable link : the short code returns 410 instead of redirecting
    * */
    void disable(Long linkId);

    /*
    * Enable a disabled link again
    * */
    void enable(Long linkId);
}
