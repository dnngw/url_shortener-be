package shortlink.repository;

import shortlink.model.Role;

public interface RoleRepository {
    Role findByName (String roleName);
}
