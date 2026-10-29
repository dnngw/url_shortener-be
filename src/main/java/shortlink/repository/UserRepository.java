package shortlink.repository;

import shortlink.dto.UserResponse;
import shortlink.model.User;

public interface UserRepository {
    User findByUsername(String username);
    User findById(Long userId);
    User save(User user);
}
