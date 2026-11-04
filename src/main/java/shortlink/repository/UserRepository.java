package shortlink.repository;

import shortlink.model.User;
import shortlink.model.UserWithLinkCount;
import shortlink.util.security.UserStatus;

import java.util.List;

/*
* User repository interface
* */
public interface UserRepository {
    /*
    * Find by username
    * */
    User findByUsername(String username);

    /*
    * Find by id
    * */
    User findById(Long userId);

    /*
    * Save
    * */
    User save(User user);

    /*
    * Check if at least one user with the role exists (ex: is there any admin yet)
    * */
    boolean existsByRole(String roleName);

    /*
    * Find users by role and status with pagination, including the number of links of each user
    *
    * status  : null = every status
    * keyword : filter by username (case insensitive), empty = no filter
    * */
    List<UserWithLinkCount> findAllByRoleAndStatus(String roleName, UserStatus status, String keyword, int limit, int offset);

    /*
    * Count users by role and status, same filter as findAllByRoleAndStatus
    * */
    int countByRoleAndStatus(String roleName, UserStatus status, String keyword);

    /*
    * Change status only when the user currently has the expected status
    *
    * @Return false when user not found or status already changed
    * */
    boolean updateStatus(Long userId, String roleName, UserStatus fromStatus, UserStatus toStatus);

    /*
    * Delete user only when the user currently has the expected status
    *
    * @Return false when user not found or status is different
    * */
    boolean deleteByIdAndStatus(Long userId, String roleName, UserStatus status);
}
