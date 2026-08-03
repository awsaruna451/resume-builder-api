-- V2__oauth2_support.sql
-- Add Google OAuth2 provider columns to users table

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS provider         VARCHAR(50)  NOT NULL DEFAULT 'GOOGLE',
    ADD COLUMN IF NOT EXISTS provider_id      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS picture_url      VARCHAR(500),
    ALTER COLUMN password DROP NOT NULL;   -- OAuth2 users have no password

-- provider_id must be unique per provider
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_provider_provider_id
    ON users(provider, provider_id)
    WHERE provider_id IS NOT NULL;
