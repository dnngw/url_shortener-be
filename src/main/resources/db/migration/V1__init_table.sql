-- table init


-- create roles table
CREATE TABLE roles (
   id               SERIAL       PRIMARY KEY,
   role_name        VARCHAR(50)  NOT NULL UNIQUE,
   role_description VARCHAR(255) NULL,
   created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO roles (role_name, role_description) VALUES
    ('admin', 'full access control'),
    ('user', 'regular user');


-- create users table
CREATE TABLE users (
   id            BIGSERIAL    PRIMARY KEY,
   role_id       INT          NOT NULL,
   username      VARCHAR(50)  NOT NULL UNIQUE,
   password_hash VARCHAR(255) NOT NULL,
   created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_users_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);


-- create links table
CREATE TABLE links (
   id           BIGSERIAL   PRIMARY KEY,
   user_id      BIGINT      NOT NULL,
   short_code   VARCHAR(50) NOT NULL UNIQUE,
   original_url TEXT        NOT NULL,
   created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT fk_user_links FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_links_user ON links (user_id);


-- create refresh tokens table
-- Notes : token_hash use SHA-256
CREATE TABLE refresh_tokens (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    token_hash  CHAR(64)    NOT NULL UNIQUE,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_expired ON refresh_tokens (expired_at);