CREATE TABLE listings (
    id BIGSERIAL PRIMARY KEY,
    landlord_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('APARTMENT', 'STUDIO', 'ROOM', 'HOUSE', 'OTHER')),
    price NUMERIC(10, 2) NOT NULL CHECK (price > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'RON' CHECK (currency IN ('RON', 'EUR')),
    rooms SMALLINT NOT NULL CHECK (rooms >= 0),
    bathrooms SMALLINT NOT NULL CHECK (bathrooms >= 0),
    area_sqm NUMERIC(7, 2) NOT NULL CHECK (area_sqm > 0),
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    google_maps_url VARCHAR(2000),
    available_from DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'RENTED', 'UNAVAILABLE', 'DELETED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_listings_landlord
        FOREIGN KEY (landlord_id) REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_listings_landlord ON listings(landlord_id);

CREATE TABLE listing_amenities (
    listing_id BIGINT NOT NULL,
    amenity VARCHAR(30) NOT NULL CHECK (
        amenity IN (
            'FURNISHED',
            'PARKING',
            'BALCONY',
            'AIR_CONDITIONING',
            'WASHING_MACHINE',
            'PETS_ALLOWED',
            'INTERNET'
        )
    ),
    PRIMARY KEY (listing_id, amenity),
    CONSTRAINT fk_listing_amenities_listing
        FOREIGN KEY (listing_id) REFERENCES listings(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_listing_amenities_listing ON listing_amenities(listing_id);
