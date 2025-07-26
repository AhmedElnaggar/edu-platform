-- Add password reset fields to users table
ALTER TABLE users
ADD COLUMN password_reset_token VARCHAR(255),
ADD COLUMN password_reset_token_expiry TIMESTAMP;

-- Create index for faster token lookups
CREATE INDEX idx_users_password_reset_token ON users(password_reset_token);
CREATE INDEX idx_users_password_reset_token_expiry ON users(password_reset_token_expiry);