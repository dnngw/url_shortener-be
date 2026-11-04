package shortlink.service;

import shortlink.dto.AdminUserResponse;
import shortlink.dto.PageResponse;

/*
* Admin user service
*
* Manage accounts with role user (admin accounts cannot be changed from here)
* */
public interface AdminUserService {

    /*
    * Get users with pagination
    *
    * status  : pending, active, inactive. null / blank / "all" = every status
    * keyword : search by username, null / blank = no filter
    * */
    PageResponse<AdminUserResponse> getUsers(String status, int page, int size, String keyword);

    /*
    * Count users waiting for approval (sidebar badge)
    * */
    int countPending();

    /*
    * Approve registration : pending → active
    * */
    void approve(Long userId);

    /*
    * Reject registration : delete the pending account
    * */
    void reject(Long userId);

    /*
    * Deactivate account : active → inactive, also ends every session
    * */
    void deactivate(Long userId);

    /*
    * Activate account again : inactive → active
    * */
    void activate(Long userId);
}
