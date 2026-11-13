CREATE TABLE rental_requests (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    title           VARCHAR(150) NOT NULL,
    description     TEXT NOT NULL,
    desired_type    VARCHAR(20) NOT NULL
                        CHECK (desired_type IN ('APARTMENT', 'STUDIO', 'ROOM', 'HOUSE', 'OTHER')),
    budget_min      NUMERIC(10, 2) NOT NULL CHECK (budget_min >= 0),
    budget_max      NUMERIC(10, 2) NOT NULL CHECK (budget_max >= 0),
    currency        VARCHAR(3) NOT NULL DEFAULT 'RON'
                        CHECK (currency IN ('RON', 'EUR')),
    city            VARCHAR(100) NOT NULL,
    area            VARCHAR(100),
    rooms           SMALLINT CHECK (rooms >= 0),
    desired_from    DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'FOUND', 'CLOSED', 'DELETED')),
    google_maps_url VARCHAR(2000),
    center_latitude  DOUBLE PRECISION,
    center_longitude DOUBLE PRECISION,
    radius_km        NUMERIC(5, 2),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rental_requests_tenant
        FOREIGN KEY (tenant_id) REFERENCES users(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_budget_range
        CHECK (budget_min <= budget_max),

    CONSTRAINT chk_radius_positive
        CHECK (radius_km IS NULL OR radius_km > 0),

    -- Zona se completează integral sau deloc
    CONSTRAINT chk_zone_all_or_nothing
        CHECK (
            (center_latitude IS NULL AND center_longitude IS NULL AND radius_km IS NULL)
            OR
            (center_latitude IS NOT NULL AND center_longitude IS NOT NULL AND radius_km IS NOT NULL)
        )
);

CREATE INDEX idx_rental_requests_tenant ON rental_requests(tenant_id);
CREATE INDEX idx_rental_requests_status  ON rental_requests(status);
