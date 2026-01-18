-- Allow password to be NULL for users without passwords
-- H2 compatible: Recreate the column with NULL allowed
-- Step 1: Add a temporary column (nullable by default)
ALTER TABLE users ADD COLUMN password_temp VARCHAR(255);

-- Step 2: Copy data from password to password_temp
UPDATE users SET password_temp = password;

-- Step 3: Drop the old password column (with NOT NULL constraint)
ALTER TABLE users DROP COLUMN password;

-- Step 4: Rename password_temp to password (now nullable - H2 syntax)
ALTER TABLE users RENAME COLUMN password_temp TO password;

