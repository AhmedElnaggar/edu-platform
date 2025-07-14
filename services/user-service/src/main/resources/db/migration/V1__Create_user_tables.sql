-- User Service Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- User profiles table
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    display_name VARCHAR(150),
    bio TEXT,
    profile_picture_url VARCHAR(500),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(20) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    location VARCHAR(200),
    timezone VARCHAR(50) DEFAULT 'UTC',
    language VARCHAR(10) DEFAULT 'en',
    website_url VARCHAR(500),
    linkedin_url VARCHAR(500),
    twitter_url VARCHAR(500),
    profile_visibility VARCHAR(20) DEFAULT 'PUBLIC' CHECK (profile_visibility IN ('PUBLIC', 'PRIVATE', 'CONTACTS_ONLY')),
    email_notifications BOOLEAN DEFAULT true,
    push_notifications BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User preferences table
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL,
    email_notifications BOOLEAN DEFAULT true,
    push_notifications BOOLEAN DEFAULT true,
    sms_notifications BOOLEAN DEFAULT false,
    marketing_emails BOOLEAN DEFAULT false,
    course_reminders BOOLEAN DEFAULT true,
    assignment_reminders BOOLEAN DEFAULT true,
    deadline_notifications BOOLEAN DEFAULT true,
    theme VARCHAR(20) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'en',
    items_per_page INTEGER DEFAULT 20,
    profile_public BOOLEAN DEFAULT true,
    show_online_status BOOLEAN DEFAULT true,
    allow_messages BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_display_name ON user_profiles(display_name);
CREATE INDEX idx_user_profiles_location ON user_profiles(location);
CREATE INDEX idx_user_profiles_visibility ON user_profiles(profile_visibility);
CREATE INDEX idx_user_profiles_created_at ON user_profiles(created_at);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
CREATE INDEX idx_user_preferences_notifications ON user_preferences(email_notifications, push_notifications);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_user_profiles_updated_at
    BEFORE UPDATE ON user_profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_preferences_updated_at
    BEFORE UPDATE ON user_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data
INSERT INTO user_profiles (user_id, first_name, last_name, display_name, bio, location, timezone, language) VALUES
    ('00000000-0000-0000-0000-000000000001', 'John', 'Doe', 'John Doe', 'Software Engineer and Tech Enthusiast', 'New York, NY', 'America/New_York', 'en'),
    ('00000000-0000-0000-0000-000000000002', 'Jane', 'Smith', 'Jane Smith', 'Mathematics Professor', 'Los Angeles, CA', 'America/Los_Angeles', 'en'),
    ('00000000-0000-0000-0000-000000000003', 'Ahmed', 'Ali', 'Ahmed Ali', 'Computer Science Student', 'Riyadh, Saudi Arabia', 'Asia/Riyadh', 'ar')
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_preferences (user_id, email_notifications, push_notifications, theme, language) VALUES
    ('00000000-0000-0000-0000-000000000001', true, true, 'dark', 'en'),
    ('00000000-0000-0000-0000-000000000002', true, false, 'light', 'en'),
    ('00000000-0000-0000-0000-000000000003', false, true, 'light', 'ar')
ON CONFLICT (user_id) DO NOTHING;