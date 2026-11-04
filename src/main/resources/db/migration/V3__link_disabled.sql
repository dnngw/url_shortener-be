-- link moderation
--
-- disabled = TRUE : link was disabled by admin, the short code returns 410 Gone instead of redirecting.
-- The short code stays reserved, so nobody else can take it over while old QR codes are still around.

ALTER TABLE links ADD COLUMN disabled BOOLEAN NOT NULL DEFAULT FALSE;
