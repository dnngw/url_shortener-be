package shortlink.service;

import shortlink.dto.AuthTokenDto;
import shortlink.dto.LoginRequest;
import shortlink.dto.RegisterRequest;
import shortlink.dto.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    AuthTokenDto login(LoginRequest request);

    AuthTokenDto refresh(String refreshToken);

    void logout(String refreshToken);
}
