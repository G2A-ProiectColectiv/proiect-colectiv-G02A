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
    '$2a$10$KlDGP5sdPXw5dzbK0r9DtuiuEDuh.hyD/CS.ERPuy42XLyzMxAvVa',
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
