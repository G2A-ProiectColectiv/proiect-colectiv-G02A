# Platforma de anunturi imobiliare

Aplicatie web pentru publicarea si cautarea anunturilor imobiliare de inchiriere (proiect colectiv, Faza 1).

- `backend/`: Spring Boot, Java 21, Gradle, PostgreSQL, Flyway
- `frontend/`: Next.js
- `docs/`: specificatii, structura proiectului, schema bazei de date, etapele de lucru

## Cerinte
- JDK 21
- Git
- PostgreSQL (instalat local, port 5432)
- IntelliJ IDEA (recomandat)

## Baza de date

1. Porneste serviciul PostgreSQL (pe Windows: `Start-Service postgresql-x64-18`, ca administrator; numele serviciului depinde de versiune).
2. Creeaza cele doua baze de date (in `psql`, pgAdmin sau DBeaver):
   ```sql
   CREATE DATABASE rental_platform;
   CREATE DATABASE rental_platform_test;
   ```
   Poti folosi utilizatorul `postgres` sau poti crea unul dedicat:
   ```sql
   CREATE USER rental WITH PASSWORD 'parola-ta';
   CREATE DATABASE rental_platform OWNER rental;
   CREATE DATABASE rental_platform_test OWNER rental;
   ```
3. Verifica conexiunea: `psql -U <utilizator> -d rental_platform`.

Tabelele sunt create doar de Flyway, la pornirea aplicatiei.

## Backend

1. Copiaza `backend/.env.example` ca `backend/.env` si completeaza `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. Fisierul `.env` nu se comite.
2. Deschide folderul `backend` in IntelliJ (se importa Gradle automat, JDK 21).
3. Porneste aplicatia:
   ```powershell
   cd backend
   .\gradlew.bat bootRun
   ```
   Aplicatia porneste pe `http://localhost:8080`.
4. Ruleaza testele (necesita baza `rental_platform_test`):
   ```powershell
   .\gradlew.bat test
   ```

## Frontend

Se adauga in sub-etapa 1.1.11.

## Lucrul in echipa
- `main` ramane stabil. Fiecare sub-etapa se lucreaza pe o ramura (`feature/<sub-etapa>-<descriere>`) si intra in `main` printr-un Pull Request.
- Nu se comite `.env`.
- Migrarile Flyway aplicate nu se mai modifica. Orice schimbare se face printr-o migrare noua.
