INSERT INTO users (
    email,
    password_hash,
    first_name,
    last_name,
    phone,
    avatar_url,
    bio,
    role,
    status,
    created_at,
    updated_at
)
VALUES (
    'admin@rental.local',
    '$2b$10$Ap8iywzKPbsVM9RqZF18luyuWVzbcq5jGfKGzV9GZkVKcG.Wy4crK',
    'System',
    'Administrator',
    NULL,
    NULL,
    'Default administrator account',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
