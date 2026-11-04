package shortlink.service.impl;

import shortlink.dto.AuthTokenDto;
import shortlink.dto.LoginRequest;
import shortlink.dto.RegisterRequest;
import shortlink.dto.UserResponse;
import shortlink.exception.DuplicateResourceException;
import shortlink.exception.ForbiddenException;
import shortlink.exception.UnauthorizedException;
import shortlink.model.User;
import shortlink.model.Role;
import shortlink.repository.RefreshTokenRepository;
import shortlink.repository.RoleRepository;
import shortlink.repository.UserRepository;
import shortlink.service.AuthService;
import shortlink.util.security.JwtTokenProvider;
import shortlink.util.security.PasswordHasher;
import shortlink.util.security.RefreshTokenProvider;
import shortlink.util.security.RoleType;
import shortlink.util.security.UserStatus;
import shortlink.util.validator.UserValidator;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

/*
* Auth Service Implementation
* */
public class AuthServiceImpl implements AuthService {
    /*
    * User repository
    * */
    private final UserRepository userRepository;

    /*
    * Role repository
    * */
    private final RoleRepository roleRepository;

    /*
    * Refresh token repository
    * */
    private final RefreshTokenRepository refreshTokenRepository;

    /*
    * Jwt token provider
    * */
    private final JwtTokenProvider jwtService;

    /*
    * Refresh token time to live days
    * */
    private final int refreshTokenTtlDays;

    /*
    * Access token time to live minutes
    * */
    private final int accessTokenTtlMinutes;

    /*
    * Hash used when the username does not exist, so bcrypt still runs.
    * Without it a wrong username answers in ~1 ms and a wrong password in ~250 ms,
    * which tells an attacker which usernames are registered
    * */
    private static final String DUMMY_PASSWORD_HASH = PasswordHasher.hash("dummy-password-for-timing");

    /*
    * Constructor
    * */
    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                            RefreshTokenRepository refreshTokenRepository, JwtTokenProvider jwtService,
                            int refreshTokenTtlDays, int accessTokenTtlMinutes) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshTokenTtlDays = refreshTokenTtlDays;
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }


    /*
    * Register
    *
    * @return User Response
    * */
    @Override
    public UserResponse register(RegisterRequest request) {
        String username = UserValidator.validateUsername(request.getUsername());
        String password = UserValidator.validatePassword(request.getPassword());

        if(userRepository.findByUsername(username) != null) {
            throw new DuplicateResourceException("Username already exist!");

        }

        Role role = roleRepository.findByName(RoleType.USER.getDbName());
        if (role == null) {
            throw new IllegalStateException("Default role USER not found in database!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setRole(role);
        // New account must be approved by admin before it can login
        user.setStatus(UserStatus.PENDING);

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole().getRoleName(),
                savedUser.getStatus().getDbName(),
                savedUser.getCreatedAt()
        );

    }

    @Override
    public AuthTokenDto login(LoginRequest request) {
        if(request.getUsername() == null || request.getUsername().isBlank()) {
            throw  new IllegalArgumentException("username should be filled");
        }

        if(request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password should be filled");
        }

        User existUser = userRepository.findByUsername(request.getUsername().toLowerCase(Locale.ROOT));

        // Always verify a hash, even for an unknown username, so both cases take the same time
        String passwordHash = existUser != null ? existUser.getPasswordHash() : DUMMY_PASSWORD_HASH;
        boolean passwordValid = PasswordHasher.verify(request.getPassword(), passwordHash);

        if(existUser == null || !passwordValid) {
            throw new UnauthorizedException("username or password invalid");
        }

        // Checked after the password, so the account status is not revealed to someone without the password
        ensureCanLogin(existUser);

        String accessToken = jwtService.generateAccessToken(existUser.getId(), existUser.getUsername(), existUser.getRole().getRoleName());
        String refreshToken = RefreshTokenProvider.generateRefreshToken();

        refreshTokenRepository.save(existUser.getId(), RefreshTokenProvider.convertToSHA256(refreshToken),
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(refreshTokenTtlDays));

        return new AuthTokenDto(
                accessToken,
                Duration.ofMinutes(accessTokenTtlMinutes).getSeconds(),
                refreshToken,
                Duration.ofDays(refreshTokenTtlDays).getSeconds()
        );


    }

    /*
    * Refresh
    *
    * @Return AuthTokenDto
    * */
    @Override
    public AuthTokenDto refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Unauthorized!");
        }

        String hashToken = RefreshTokenProvider.convertToSHA256(refreshToken);
        Long userId = refreshTokenRepository.revokeIfValid(hashToken);

        User currentUser = userRepository.findById(userId);

        // Account deleted / deactivated after the last login → session ends here
        if (currentUser == null || currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }
        String newAccessToken = jwtService.generateAccessToken(currentUser.getId(), currentUser.getUsername(), currentUser.getRole().getRoleName());
        String newRefreshToken = RefreshTokenProvider.generateRefreshToken();

        refreshTokenRepository.save(currentUser.getId(), RefreshTokenProvider.convertToSHA256(newRefreshToken),
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(refreshTokenTtlDays));

        return new AuthTokenDto(
                newAccessToken,
                Duration.ofMinutes(accessTokenTtlMinutes).getSeconds(),
                newRefreshToken,
                Duration.ofDays(refreshTokenTtlDays).getSeconds()
        );
    }

    @Override
    public void logout(String refreshToken) {
        if(refreshToken == null) {
            throw new UnauthorizedException("Unauthorized!");
        }
        String hashToken = RefreshTokenProvider.convertToSHA256(refreshToken);

        refreshTokenRepository.revoke(hashToken);
    }

    /*
    * Make sure the account is allowed to login
    *
    * @Throw ForbiddenException when the account is pending or inactive
    * */
    private void ensureCanLogin(User user) {
        switch (user.getStatus()) {
            case PENDING -> throw new ForbiddenException("Your account is waiting for admin approval");
            case INACTIVE -> throw new ForbiddenException("Your account has been deactivated");
            case ACTIVE -> { }
        }
    }
}
