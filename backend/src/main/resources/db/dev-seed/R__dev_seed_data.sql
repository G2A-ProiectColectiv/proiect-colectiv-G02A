-- Date de test pentru dezvoltare (se încarcă doar în profilul dev, vezi application-dev.properties).
-- Migrare repetabilă și idempotentă: poate rula de mai multe ori fără să dubleze datele.
-- Parola tuturor conturilor de mai jos: Parola123!

-- ---------------------------------------------------------------- utilizatori
INSERT INTO users (email, password_hash, first_name, last_name, phone, bio, role, status)
VALUES
    ('proprietar1@test.ro', '$2a$10$Kl1mdvJphfq4dcrPcYGE1e74gYVVIJzUtiHDARQj4FbBPZ6sP9HzW',
     'Andrei', 'Popescu', '0721000001', 'Proprietar cu mai multe apartamente în Cluj-Napoca.', 'LANDLORD', 'ACTIVE'),
    ('proprietar2@test.ro', '$2a$10$Kl1mdvJphfq4dcrPcYGE1e74gYVVIJzUtiHDARQj4FbBPZ6sP9HzW',
     'Maria', 'Ionescu', '0721000002', 'Închiriez locuințe în București și Iași.', 'LANDLORD', 'ACTIVE'),
    ('chirias1@test.ro', '$2a$10$Kl1mdvJphfq4dcrPcYGE1e74gYVVIJzUtiHDARQj4FbBPZ6sP9HzW',
     'Elena', 'Dumitru', '0721000003', 'Studentă, caut o garsonieră aproape de facultate.', 'TENANT', 'ACTIVE'),
    ('chirias2@test.ro', '$2a$10$Kl1mdvJphfq4dcrPcYGE1e74gYVVIJzUtiHDARQj4FbBPZ6sP9HzW',
     'Mihai', 'Georgescu', '0721000004', 'Angajat IT, mă mut în oraș pentru un job nou.', 'TENANT', 'ACTIVE'),
    ('chirias3@test.ro', '$2a$10$Kl1mdvJphfq4dcrPcYGE1e74gYVVIJzUtiHDARQj4FbBPZ6sP9HzW',
     'Ioana', 'Marin', NULL, NULL, 'TENANT', 'ACTIVE')
ON CONFLICT ((LOWER(email))) DO NOTHING;

-- ------------------------------------------------------------------- anunțuri
INSERT INTO listings (landlord_id, title, description, type, price, currency, rooms, bathrooms, area_sqm,
                      address, city, latitude, longitude, available_from, status)
SELECT u.id, v.title, v.description, v.type, v.price, v.currency, v.rooms, v.bathrooms, v.area_sqm,
       v.address, v.city, v.latitude, v.longitude, v.available_from, v.status
FROM (VALUES
    ('proprietar1@test.ro', 'Garsonieră modernă lângă UBB',
     'Garsonieră complet renovată, la 10 minute de pe jos de Universitatea Babeș-Bolyai. Mobilată și utilată, internet inclus.',
     'STUDIO', 350.00, 'EUR', 1, 1, 28.00, 'Strada Observatorului 12', 'Cluj-Napoca', 46.7561, 23.5740, DATE '2026-12-01', 'ACTIVE'),
    ('proprietar1@test.ro', 'Apartament 2 camere în Mărăști',
     'Apartament luminos cu balcon, la etaj intermediar, într-o zonă liniștită cu parcare și magazine în apropiere.',
     'APARTMENT', 480.00, 'EUR', 2, 1, 52.00, 'Strada Aurel Vlaicu 45', 'Cluj-Napoca', 46.7790, 23.6140, DATE '2026-12-15', 'ACTIVE'),
    ('proprietar1@test.ro', 'Cameră într-un apartament nou, Zorilor',
     'Cameră spațioasă într-un apartament împărțit cu alți doi chiriași. Bucătărie și baie comune, mașină de spălat.',
     'ROOM', 1200.00, 'RON', 1, 1, 18.00, 'Strada Observatorului 80', 'Cluj-Napoca', 46.7499, 23.5810, DATE '2026-11-20', 'ACTIVE'),
    ('proprietar2@test.ro', 'Apartament 3 camere, zona Floreasca',
     'Apartament elegant cu 3 camere, 2 băi, aer condiționat și loc de parcare subteran. Aproape de metrou și parc.',
     'APARTMENT', 900.00, 'EUR', 3, 2, 85.00, 'Strada Floreasca 120', 'București', 44.4710, 26.0990, DATE '2027-01-01', 'ACTIVE'),
    ('proprietar2@test.ro', 'Studio în Dorobanți',
     'Studio compact, ideal pentru o persoană, la 5 minute de stația de metrou Dorobanți. Mobilat modern.',
     'STUDIO', 520.00, 'EUR', 1, 1, 35.00, 'Calea Dorobanți 98', 'București', 44.4530, 26.0970, DATE '2026-12-10', 'ACTIVE'),
    ('proprietar2@test.ro', 'Casă cu curte în Copou',
     'Casă cu 4 camere și curte proprie, într-o zonă verde din Iași, potrivită pentru o familie sau colegi de apartament.',
     'HOUSE', 1800.00, 'RON', 4, 2, 130.00, 'Strada Sadoveanu 7', 'Iași', 47.1830, 27.5660, DATE '2027-02-01', 'ACTIVE'),
    ('proprietar2@test.ro', 'Apartament 2 camere, Timișoara Centru',
     'Apartament cu 2 camere, central, într-o clădire istorică renovată. Încălzire în centrala proprie.',
     'APARTMENT', 420.00, 'EUR', 2, 1, 48.00, 'Strada Mercy 3', 'Timișoara', 45.7550, 21.2260, DATE '2026-12-05', 'ACTIVE'),
    ('proprietar1@test.ro', 'Apartament închiriat în Gheorgheni',
     'Anunț marcat ca închiriat, pentru testarea filtrelor după status.',
     'APARTMENT', 500.00, 'EUR', 2, 1, 55.00, 'Strada Fabricii 20', 'Cluj-Napoca', 46.7840, 23.6200, DATE '2026-10-01', 'RENTED')
) AS v(landlord_email, title, description, type, price, currency, rooms, bathrooms, area_sqm,
       address, city, latitude, longitude, available_from, status)
JOIN users u ON LOWER(u.email) = v.landlord_email
WHERE NOT EXISTS (
    SELECT 1 FROM listings l WHERE l.landlord_id = u.id AND l.title = v.title
);

-- ------------------------------------------------------------------- dotări
INSERT INTO listing_amenities (listing_id, amenity)
SELECT l.id, a.amenity
FROM (VALUES
    ('Garsonieră modernă lângă UBB', 'FURNISHED'),
    ('Garsonieră modernă lângă UBB', 'INTERNET'),
    ('Garsonieră modernă lângă UBB', 'AIR_CONDITIONING'),
    ('Apartament 2 camere în Mărăști', 'BALCONY'),
    ('Apartament 2 camere în Mărăști', 'PARKING'),
    ('Apartament 2 camere în Mărăști', 'WASHING_MACHINE'),
    ('Cameră într-un apartament nou, Zorilor', 'FURNISHED'),
    ('Cameră într-un apartament nou, Zorilor', 'WASHING_MACHINE'),
    ('Cameră într-un apartament nou, Zorilor', 'INTERNET'),
    ('Apartament 3 camere, zona Floreasca', 'PARKING'),
    ('Apartament 3 camere, zona Floreasca', 'AIR_CONDITIONING'),
    ('Apartament 3 camere, zona Floreasca', 'BALCONY'),
    ('Apartament 3 camere, zona Floreasca', 'FURNISHED'),
    ('Studio în Dorobanți', 'FURNISHED'),
    ('Studio în Dorobanți', 'INTERNET'),
    ('Studio în Dorobanți', 'PETS_ALLOWED'),
    ('Casă cu curte în Copou', 'PARKING'),
    ('Casă cu curte în Copou', 'PETS_ALLOWED'),
    ('Casă cu curte în Copou', 'WASHING_MACHINE'),
    ('Apartament 2 camere, Timișoara Centru', 'BALCONY'),
    ('Apartament 2 camere, Timișoara Centru', 'INTERNET'),
    ('Apartament închiriat în Gheorgheni', 'PARKING')
) AS a(title, amenity)
JOIN listings l ON l.title = a.title
ON CONFLICT (listing_id, amenity) DO NOTHING;

-- ------------------------------------------------------------ cereri de închiriere
INSERT INTO rental_requests (tenant_id, title, description, desired_type, budget_min, budget_max, currency,
                             city, area, rooms, desired_from, status,
                             center_latitude, center_longitude, radius_km)
SELECT u.id, v.title, v.description, v.desired_type, v.budget_min, v.budget_max, v.currency,
       v.city, v.area, v.rooms, v.desired_from, v.status,
       v.center_latitude, v.center_longitude, v.radius_km
FROM (VALUES
    ('chirias1@test.ro', 'Caut garsonieră aproape de facultate',
     'Sunt studentă în anul 3 și caut o garsonieră liniștită, de preferat mobilată, aproape de centrul universitar.',
     'STUDIO', 250.00, 400.00, 'EUR', 'Cluj-Napoca', 'Centru / Zorilor', 1, DATE '2026-12-01', 'ACTIVE',
     46.7600::double precision, 23.5800::double precision, 3.00::numeric),
    ('chirias1@test.ro', 'Cameră în apartament, buget mic',
     'Caut o cameră într-un apartament cu alți studenți, cu internet și mașină de spălat.',
     'ROOM', 800.00, 1300.00, 'RON', 'Cluj-Napoca', NULL, 1, DATE '2026-11-25', 'ACTIVE',
     NULL, NULL, NULL),
    ('chirias2@test.ro', 'Apartament 2 camere pentru mutare în București',
     'Mă mut pentru un job nou și caut un apartament cu 2 camere, aproape de metrou, cu loc de parcare.',
     'APARTMENT', 500.00, 800.00, 'EUR', 'București', 'Nord', 2, DATE '2027-01-01', 'ACTIVE',
     44.4600::double precision, 26.0900::double precision, 5.00::numeric),
    ('chirias2@test.ro', 'Casă pentru familie, Iași',
     'Caut o casă cu curte pentru o familie cu animale de companie, într-o zonă verde.',
     'HOUSE', 1500.00, 2500.00, 'RON', 'Iași', 'Copou', 3, DATE '2027-02-01', 'ACTIVE',
     NULL, NULL, NULL),
    ('chirias3@test.ro', 'Apartament în Timișoara, găsit deja',
     'Cerere marcată ca găsită, pentru testarea filtrelor după status.',
     'APARTMENT', 350.00, 500.00, 'EUR', 'Timișoara', 'Centru', 2, DATE '2026-11-01', 'FOUND',
     NULL, NULL, NULL)
) AS v(tenant_email, title, description, desired_type, budget_min, budget_max, currency,
       city, area, rooms, desired_from, status, center_latitude, center_longitude, radius_km)
JOIN users u ON LOWER(u.email) = v.tenant_email
WHERE NOT EXISTS (
    SELECT 1 FROM rental_requests r WHERE r.tenant_id = u.id AND r.title = v.title
);
