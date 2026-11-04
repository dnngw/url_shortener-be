package shortlink.service.impl;

import shortlink.dto.AdminStatsResponse;
import shortlink.repository.LinkRepository;
import shortlink.repository.UserRepository;
import shortlink.service.AdminStatsService;
import shortlink.util.security.RoleType;
import shortlink.util.security.UserStatus;

/*
* Admin stats service implementation
* */
public class AdminStatsServiceImpl implements AdminStatsService {

    /*
    * Only accounts with role user are counted, same as the admin user list
    * */
    private static final String MANAGED_ROLE = RoleType.USER.getDbName();

    /*
    * Repository
    * */
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;

    /*
    * Constructor
    * */
    public AdminStatsServiceImpl(UserRepository userRepository, LinkRepository linkRepository) {
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
    }

    /*
    * Get stats
    * */
    @Override
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.countByRoleAndStatus(MANAGED_ROLE, UserStatus.PENDING, ""),
                userRepository.countByRoleAndStatus(MANAGED_ROLE, UserStatus.ACTIVE, ""),
                userRepository.countByRoleAndStatus(MANAGED_ROLE, UserStatus.INACTIVE, ""),
                linkRepository.countAll()
        );
    }
}
