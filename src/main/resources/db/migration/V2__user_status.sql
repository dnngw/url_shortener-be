-- user account status
--
-- pending  : registered, waiting for admin approval (cannot login)
-- active   : approved, can use the app
-- inactive : deactivated by admin (cannot login / refresh token)


-- Existing users are considered approved, so current accounts are not locked out
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active';

ALTER TABLE users ADD CONSTRAINT chk_users_status CHECK (status IN ('pending', 'active', 'inactive'));

-- From now on, new users wait for approval
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'pending';

CREATE INDEX idx_users_status ON users (status);
