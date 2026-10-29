package shortlink.service.impl;

import shortlink.dto.AuthTokenDto;
import shortlink.dto.LoginRequest;
import shortlink.dto.RegisterRequest;
import shortlink.dto.UserResponse;
import shortlink.exception.DuplicateResourceException;
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

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole().getRoleName(),
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

        if(existUser == null || !PasswordHasher.verify(request.getPassword(), existUser.getPasswordHash())) {
            throw new UnauthorizedException("username or password invalid");
        }

        String accessToken = jwtService.generateAccessToken(existUser.getId(), existUser.getRole().getRoleName());
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
        String newAccessToken = jwtService.generateAccessToken(currentUser.getId(), currentUser.getRole().getRoleName());
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
}
