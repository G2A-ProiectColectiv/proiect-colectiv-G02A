CREATE TABLE listing_images (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL,
    url VARCHAR(255) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    position SMALLINT NOT NULL CHECK (position >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listing_images_listing
        FOREIGN KEY (listing_id) REFERENCES listings(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_listing_images_listing_position ON listing_images(listing_id, position);
CREATE UNIQUE INDEX uk_listing_images_primary ON listing_images(listing_id) WHERE is_primary;
