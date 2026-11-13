-- Indexuri pentru căutarea publică a anunțurilor și pentru căutarea cererilor (etapa 1.5)

CREATE INDEX IF NOT EXISTS idx_listings_status_city ON listings(status, city);
CREATE INDEX IF NOT EXISTS idx_listings_price ON listings(price);
CREATE INDEX IF NOT EXISTS idx_listings_lat_lng ON listings(latitude, longitude);

CREATE INDEX IF NOT EXISTS idx_rental_requests_status_city ON rental_requests(status, city);
CREATE INDEX IF NOT EXISTS idx_rental_requests_budget_max ON rental_requests(budget_max);
