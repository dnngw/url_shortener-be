package shortlink.service.impl;

import shortlink.dto.AdminUserResponse;
import shortlink.dto.PageResponse;
import shortlink.exception.NotFoundException;
import shortlink.model.User;
import shortlink.model.UserWithLinkCount;
import shortlink.repository.RefreshTokenRepository;
import shortlink.repository.UserRepository;
import shortlink.util.Pagination;
import shortlink.service.AdminUserService;
import shortlink.util.security.RoleType;
import shortlink.util.security.UserStatus;

import java.util.List;

/*
* Admin user service implementation
* */
public class AdminUserServiceImpl implements AdminUserService {

    /*
    * Only accounts with this role can be managed, so admin accounts are never touched
    * */
    private static final String MANAGED_ROLE = RoleType.USER.getDbName();

    /*
    * Search rules, same as links (page / size rules are in Pagination)
    * */
    private static final int MAX_KEYWORD_LENGTH = 100;

    /*
    * User repository
    * */
    private final UserRepository userRepository;

    /*
    * Refresh token repository
    * */
    private final RefreshTokenRepository refreshTokenRepository;

    /*
    * Constructor
    * */
    public AdminUserServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /*
    * Get users with pagination
    * */
    @Override
    public PageResponse<AdminUserResponse> getUsers(String status, int page, int size, String keyword) {
        String search = keyword == null ? "" : keyword.trim();

        if (search.length() > MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException("Search keyword must be at most " + MAX_KEYWORD_LENGTH + " characters");
        }

        // null = every status. Unknown status throws IllegalArgumentException → 400
        UserStatus userStatus = status == null || status.isBlank() || status.equals("all")
                ? null
                : UserStatus.fromDbName(status);

        int offset = Pagination.offset(page, size);

        List<AdminUserResponse> items = userRepository
                .findAllByRoleAndStatus(MANAGED_ROLE, userStatus, search, size, offset)
                .stream()
                .map(this::toResponse)
                .toList();

        int totalItems = userRepository.countByRoleAndStatus(MANAGED_ROLE, userStatus, search);

        return new PageResponse<>(items, page, size, totalItems);
    }

    /*
    * Count pending users
    * */
    @Override
    public int countPending() {
        return userRepository.countByRoleAndStatus(MANAGED_ROLE, UserStatus.PENDING, "");
    }

    /*
    * Approve
    * */
    @Override
    public void approve(Long userId) {
        changeStatus(userId, UserStatus.PENDING, UserStatus.ACTIVE);
    }

    /*
    * Reject
    * */
    @Override
    public void reject(Long userId) {
        findManagedUser(userId, UserStatus.PENDING);

        if (!userRepository.deleteByIdAndStatus(userId, MANAGED_ROLE, UserStatus.PENDING)) {
            throw new IllegalStateException("User status has changed, please refresh the page");
        }
    }

    /*
    * Deactivate
    * */
    @Override
    public void deactivate(Long userId) {
        changeStatus(userId, UserStatus.ACTIVE, UserStatus.INACTIVE);

        // Refresh token can not be used anymore, the access token still works until it expires
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    /*
    * Activate
    * */
    @Override
    public void activate(Long userId) {
        changeStatus(userId, UserStatus.INACTIVE, UserStatus.ACTIVE);
    }

    /*
    * Change status from the expected status to the new one
    * */
    private void changeStatus(Long userId, UserStatus fromStatus, UserStatus toStatus) {
        findManagedUser(userId, fromStatus);

        // Status is checked again in the query, in case another admin changed it in the meantime
        if (!userRepository.updateStatus(userId, MANAGED_ROLE, fromStatus, toStatus)) {
            throw new IllegalStateException("User status has changed, please refresh the page");
        }
    }

    /*
    * Find a managed user (role user) with the expected status
    *
    * @Throw NotFoundException     user not found or is an admin
    * @Throw IllegalStateException user has another status (ex: approve an active user)
    * */
    private User findManagedUser(Long userId, UserStatus expectedStatus) {
        User user = userRepository.findById(userId);

        if (user == null || !MANAGED_ROLE.equals(user.getRole().getRoleName())) {
            throw new NotFoundException("User not found");
        }

        if (user.getStatus() != expectedStatus) {
            throw new IllegalStateException("User is " + user.getStatus().getDbName()
                    + ", expected " + expectedStatus.getDbName());
        }

        return user;
    }

    /*
    * Map user to response, password hash is never returned
    * */
    private AdminUserResponse toResponse(UserWithLinkCount row) {
        User user = row.user();

        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().getRoleName(),
                user.getStatus().getDbName(),
                row.linkCount(),
                user.getCreatedAt()
        );
    }
}
