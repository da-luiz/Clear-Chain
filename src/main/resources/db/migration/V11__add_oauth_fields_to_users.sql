-- Add OAuth2 fields to users table
ALTER TABLE users ADD COLUMN oauth_provider VARCHAR(50);
ALTER TABLE users ADD COLUMN oauth_id VARCHAR(255);
ALTER TABLE users ADD COLUMN image_url VARCHAR(500);

-- Create index for OAuth lookups
CREATE INDEX IF NOT EXISTS idx_users_oauth ON users(oauth_provider, oauth_id);




