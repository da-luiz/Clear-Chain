-- Rename password_hash to password for Spring Security compatibility
-- H2 compatible: Use RENAME COLUMN (H2 supports this)
ALTER TABLE users RENAME COLUMN password_hash TO password;

-- Update existing users with default password (should be changed on first login)
-- Password: 'password123' hashed with BCrypt (cost factor 10)
UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' 
WHERE password IS NULL OR password = '';

-- Note: Password column remains nullable to allow users without passwords
-- We do NOT set NOT NULL here to allow NULL passwords for users created later
