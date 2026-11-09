# Platforma de anunțuri imobiliare – Structura proiectului

> Document însoțitor pentru `specificatii-proiect.md`. Pentru fiecare etapă din faza 1 arată ce fișiere se adaugă și ce fișiere existente se modifică.

**Tehnologii**

| Parte | Tehnologii |
|---|---|
| Backend | Java (LTS), Gradle, Spring Boot (Web, Security, Data JPA, Validation, Mail), MapStruct, JJWT |
| Bază de date | PostgreSQL, cu migrări Flyway |
| Frontend | Next.js (App Router) cu TypeScript, TanStack Query, Axios, React Hook Form, Zod, React Leaflet |
| Teste | JUnit 5, Mockito, Spring Boot Test (cu o bază PostgreSQL locală, separată, pentru teste), Vitest și React Testing Library |

**Convenții folosite în tot documentul**
- `+` marchează un fișier nou în etapa respectivă, iar `~` un fișier existent care se modifică.
- Pachetul de bază din backend este `ro.rentalplatform`. În arborii de mai jos, `…/` înseamnă `backend/src/main/java/ro/rentalplatform/`.
- În subpachetele `service/`, fiecare serviciu are o interfață și o implementare cu sufixul `Impl` (de exemplu `ListingService` și `ListingServiceImpl`). Excepție fac serviciile tehnice (stocare, email), ale căror implementări poartă numele tehnologiei: `LocalDiskImageStorageService`, `SmtpEmailService`.

---

## 1. Arhitectura

### 1.1 Structura repository-ului

Proiectul stă într-un singur repository (monorepo):

```
rental-platform/
├── backend/                 # Spring Boot + Gradle
├── frontend/                # Next.js
├── uploads/                 # pozele încărcate, versionate în Git (PROF-04)
│   ├── avatars/
│   └── listings/
├── docs/
│   ├── specificatii-proiect.md
│   ├── structura-proiect.md
│   └── diagrams/            # diagramele PlantUML
├── .gitignore
├── .editorconfig
└── README.md
```

### 1.2 Backend – straturile

Fiecare modul de funcționalitate (`auth`, `user`, `listing` etc.) are aceleași straturi, în subpachete separate. Dependențele merg doar de sus în jos:

```
            ┌──────────────────────────────┐
  HTTP  ──▶ │  controller  (+ dto)         │  primește cererea, validează DTO-ul (@Valid),
            └──────────────┬───────────────┘  apelează serviciul, întoarce DTO + cod HTTP
                           ▼
            ┌──────────────────────────────┐
            │  service  (interfață + Impl) │  regulile de business, tranzacțiile,
            └──────────────┬───────────────┘  verificarea proprietarului, tranzițiile de status
                           ▼
            ┌──────────────────────────────┐
            │  repository  (Spring Data)   │  acces la baza de date
            └──────────────┬───────────────┘
                           ▼
            ┌──────────────────────────────┐
            │  domain  (entități, enumuri) │  modelul de date (JPA)
            └──────────────────────────────┘
  mapper (MapStruct): conversia între domain și dto, folosită de service
```

**Reguli de dependență**

| Stratul | Are voie să folosească | Nu are voie să folosească |
|---|---|---|
| `controller` | `service` (prin interfață) și `dto` | `repository` și entitățile din `domain` |
| `service` | `repository`, `domain`, `mapper`, `dto`, interfețele altor module | `controller`, obiectele HTTP (`HttpServletRequest`, `ResponseEntity`) |
| `repository` | `domain` | orice strat de deasupra |
| `domain` | doar alte clase din `domain` și `common/domain` | orice alt strat |

**Alte reguli:**
- Entitățile JPA nu ies niciodată din stratul `service`. API-ul întoarce doar DTO-uri.
- Un modul nu folosește direct repository-ul altui modul. Folosește interfața de serviciu a acelui modul.
- Excepțiile de business (`NotFoundException`, `ConflictException` etc.) se aruncă în `service` și sunt transformate în răspunsuri HTTP într-un singur loc, `GlobalExceptionHandler`.
- Configurarea Hibernate este `spring.jpa.hibernate.ddl-auto=validate`. Schema bazei de date este creată doar de Flyway.

### 1.3 Frontend – straturile

Frontendul folosește **Next.js cu App Router** și TypeScript. Folderul `src/app/` conține doar rutele, iar logica stă în module de funcționalitate (`src/features/<modul>/`), fiecare cu aceleași straturi:

```
src/app/<rută>/page.tsx   rutare: fișier subțire, care doar afișează un view din features/
  └─▶ views/        compune ecranul din componente și hook-uri (fără logică HTTP)
        └─▶ components/   UI: formulare, liste, carduri (primesc date prin props)
        └─▶ hooks/        TanStack Query: useQuery / useMutation, invalidarea cache-ului
              └─▶ api/        funcții care apelează backendul (Axios)
                    └─▶ shared/api/httpClient.ts   instanța Axios, token JWT, tratarea 401
schemas/      validare Zod, folosită de formulare
types.ts      tipurile TypeScript ale DTO-urilor
```

**Reguli specifice Next.js**
- **Componente client.** Ecranele interactive (formulare, filtre, hărți, tabele) sunt componente client (`"use client"`) și iau datele prin TanStack Query. `page.tsx` și `layout.tsx` rămân componente server, cât mai simple.
- **Fără folder `src/pages/`.** Next.js l-ar interpreta ca Pages Router. Din acest motiv, ecranele din module stau în `views/`, nu în `pages/`.
- **Același domeniu pentru API și poze.** `next.config.ts` redirecționează (rewrites) `/api/**` și `/uploads/**` către backend. Astfel, frontendul și API-ul par să fie pe același domeniu, fără probleme de CORS.
- **Token-ul JWT** se păstrează într-un cookie (`access_token`, `SameSite=Lax`, `Secure` în producție), cu aceeași expirare ca token-ul. Axios îl citește și îl trimite în antetul `Authorization`, iar `proxy.ts` îl folosește pentru redirecționări.
- **`proxy.ts`** (fostul `middleware.ts` din versiunile mai vechi de Next.js) redirecționează la login utilizatorii neautentificați și trimite la pagina principală utilizatorii cu rol greșit. Securitatea reală rămâne în backend: `proxy.ts` doar îmbunătățește experiența utilizatorului.
- **Leaflet** are nevoie de `window`, deci componentele de hartă se încarcă cu `next/dynamic` și opțiunea `ssr: false`.

Componentele nu apelează niciodată Axios direct. Codul folosit de mai multe module stă în `src/shared/`.

### 1.4 Migrările Flyway

- Migrările stau în `backend/src/main/resources/db/migration/` și au numele `V<n>__<descriere>.sql`.
- O migrare deja aplicată (inclusiv pe mașina unui coleg) nu se mai modifică niciodată. Orice schimbare se face printr-o migrare nouă.
- Fiecare etapă adaugă propriile migrări, cu numere în continuarea etapei anterioare.

| Etapă | Migrări |
|---|---|
| 1.1 | `V1__create_users_table.sql`, `V2__seed_admin_user.sql` |
| 1.2 | `V3__create_password_reset_tokens_table.sql` |
| 1.3 | `V4__create_listings_table.sql`, `V5__create_listing_images_table.sql` |
| 1.4 | `V6__create_rental_requests_table.sql` |
| 1.5 | `V7__add_search_indexes.sql` |
| 1.6 | – (nicio migrare nouă) |

---

## 2. Etapa 1.1 – Fundația și autentificarea

Cerințe acoperite: INFR-01…05, AUTH-01…11.

### 2.1 Rădăcina repository-ului

```
rental-platform/
+ ├── .env.example                # variabilele necesare: DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, ADMIN_EMAIL, ADMIN_PASSWORD_HASH
+ ├── .gitignore                  # build/, node_modules/, .next/, .env, .idea/ (uploads/ NU se ignoră)
+ ├── .editorconfig
+ └── README.md                   # instalarea PostgreSQL local, crearea bazelor de date, pornirea backendului și a frontendului
```

### 2.2 Backend

```
backend/
+ ├── build.gradle                # dependențele de mai jos
+ ├── settings.gradle
+ ├── gradlew, gradlew.bat, gradle/wrapper/…
  └── src/
      ├── main/java/ro/rentalplatform/
+     │   ├── RentalPlatformApplication.java
      │   │
      │   ├── common/
      │   │   ├── domain/
+     │   │   │   └── BaseEntity.java              # id, createdAt, updatedAt (@MappedSuperclass, auditare JPA)
      │   │   ├── dto/
+     │   │   │   └── PageResponse.java            # răspuns paginat comun: items, page, size, total
      │   │   └── exception/
+     │   │       ├── ApiError.java                # formatul unic al erorilor (INFR-04)
+     │   │       ├── FieldErrorDto.java           # câmp + mesaj
+     │   │       ├── GlobalExceptionHandler.java  # @RestControllerAdvice: excepție → cod HTTP
+     │   │       ├── NotFoundException.java       # → 404
+     │   │       ├── ConflictException.java       # → 409
+     │   │       ├── ForbiddenOperationException.java  # → 403
+     │   │       └── BadCredentialsAppException.java   # → 401
      │   │
      │   ├── config/
+     │   │   ├── JpaAuditingConfig.java           # @EnableJpaAuditing pentru createdAt / updatedAt
+     │   │   └── CorsConfig.java                  # originea frontendului (necesar doar fără rewrites din Next.js)
      │   │
      │   ├── security/
+     │   │   ├── SecurityConfig.java              # rute publice / protejate, sesiune stateless, BCrypt
+     │   │   ├── JwtProperties.java               # @ConfigurationProperties: secret, expirare
+     │   │   ├── JwtService.java                  # interfață: generateToken, parseToken
+     │   │   ├── JwtServiceImpl.java              # implementare cu JJWT
+     │   │   ├── JwtAuthFilter.java               # citește token-ul, verifică statusul contului (NF-06)
+     │   │   ├── UserPrincipal.java               # utilizatorul autentificat (id, email, rol)
+     │   │   ├── CurrentUserProvider.java         # interfață: id-ul utilizatorului curent
+     │   │   ├── SecurityContextCurrentUserProvider.java  # implementare din SecurityContext
+     │   │   ├── RestAuthenticationEntryPoint.java  # 401 în formatul ApiError
+     │   │   └── RestAccessDeniedHandler.java     # 403 în formatul ApiError
      │   │
      │   ├── user/
      │   │   ├── domain/
+     │   │   │   ├── User.java
+     │   │   │   ├── Role.java                    # TENANT, LANDLORD, ADMIN
+     │   │   │   └── UserStatus.java              # ACTIVE, BLOCKED, DELETED
      │   │   ├── repository/
+     │   │   │   └── UserRepository.java          # findByEmail, existsByEmail
      │   │   ├── dto/
+     │   │   │   └── UserDto.java                 # fără parolă (AUTH-05)
      │   │   └── mapper/
+     │   │       └── UserMapper.java              # MapStruct
      │   │
      │   └── auth/
      │       ├── controller/
+     │       │   └── AuthController.java          # POST /api/auth/register, /api/auth/login
      │       ├── dto/
+     │       │   ├── RegisterRequest.java         # @Email, @NotBlank, @Size; rol doar TENANT / LANDLORD
+     │       │   ├── LoginRequest.java
+     │       │   └── AuthResponse.java            # token + UserDto
      │       └── service/
+     │           ├── AuthService.java
+     │           └── AuthServiceImpl.java         # înregistrare, login, refuză rolul ADMIN (AUTH-04)
      │
      ├── main/resources/
+     │   ├── application.properties               # setări comune, ddl-auto=validate, placeholdere Flyway
+     │   ├── application-dev.properties
+     │   ├── application-prod.properties
      │   └── db/migration/
+     │       ├── V1__create_users_table.sql       # users + index unic pe email
+     │       └── V2__seed_admin_user.sql          # INSERT admin cu ${adminEmail} și ${adminPasswordHash}
      │
      ├── test/resources/
+     │   └── application-test.properties          # conexiunea la baza de test rental_platform_test
      │
      └── test/java/ro/rentalplatform/
+         ├── AbstractIntegrationTest.java         # @SpringBootTest + profilul test, comun tuturor testelor de integrare
+         ├── auth/AuthServiceImplTest.java        # email duplicat → 409, ADMIN refuzat, parolă greșită → 401
+         ├── auth/AuthControllerTest.java         # @WebMvcTest: validare → 400 cu erori pe câmpuri
+         └── security/JwtServiceImplTest.java     # token generat / expirat / invalid
```

**Dependențe adăugate în `build.gradle`:** `spring-boot-starter-web`, `-security`, `-data-jpa`, `-validation`, `flyway-core`, `flyway-database-postgresql`, `postgresql`, `jjwt-api` / `-impl` / `-jackson`, `mapstruct` (cu procesor de adnotări), `lombok`, `spring-boot-starter-test`, `spring-security-test`.

**Baza de date, fără Docker:** PostgreSQL se instalează local pe fiecare calculator. Se creează două baze de date: `rental_platform` pentru dezvoltare și `rental_platform_test` pentru teste. Pașii sunt descriși în README. Datele de conexiune vin din variabile de mediu (`DB_URL`, `DB_USER`, `DB_PASSWORD`), iar Flyway creează tabelele la pornirea aplicației. Testele de integrare rulează pe `rental_platform_test`, iar Flyway o curăță și o recreează înainte de rulare (`spring.flyway.clean-disabled=false`, doar în profilul `test`).

**Contul de admin:** Flyway nu poate genera un hash BCrypt, așa că hash-ul parolei de admin se generează o singură dată și se pune în variabila de mediu `ADMIN_PASSWORD_HASH`, de unde este preluat ca placeholder în migrarea `V2`.

### 2.3 Frontend

```
frontend/
+ ├── package.json
+ ├── next.config.ts                 # rewrites /api/** și /uploads/** → BACKEND_URL
+ ├── tsconfig.json                  # alias @/* → src/*
+ ├── .env.example                   # BACKEND_URL
+ ├── eslint.config.mjs, .prettierrc
+ ├── public/                        # favicon, imagini statice
  └── src/
+     ├── proxy.ts                     # protecția rutelor după rol (AUTH-11)
      ├── app/
+     │   ├── layout.tsx               # layout-ul rădăcină: <Providers>, Navbar
+     │   ├── providers.tsx            # "use client": QueryClientProvider, AuthProvider, Toaster
+     │   ├── globals.css
+     │   ├── page.tsx                 # pagina principală
+     │   ├── not-found.tsx
+     │   └── (auth)/                  # grup de rute, fără efect asupra URL-ului
+     │       ├── login/page.tsx
+     │       └── register/page.tsx
      ├── shared/
      │   ├── api/
+     │   │   ├── httpClient.ts        # Axios: atașează JWT din cookie, la 401 deconectează
+     │   │   ├── queryClient.ts
+     │   │   └── apiError.ts          # transformă ApiError în mesaje pe câmpuri
      │   ├── components/
+     │   │   ├── Button.tsx, Input.tsx, Select.tsx, FormField.tsx
+     │   │   ├── Spinner.tsx, EmptyState.tsx
+     │   │   └── Toaster.tsx          # mesaje de succes (NF-18)
      │   ├── layout/
+     │   │   └── Navbar.tsx           # meniu diferit pe rol
      │   ├── auth/
+     │   │   ├── tokenCookie.ts       # citire / scriere / ștergere cookie access_token
+     │   │   └── decodeToken.ts       # citește rolul din JWT (fără verificare; folosit doar pentru UX)
      │   └── types/
+     │       └── api.ts               # PageResponse, ApiError
      └── features/
          └── auth/
              ├── api/
+             │   └── authApi.ts       # register, login
              ├── hooks/
+             │   ├── useLogin.ts      # după login: salvează cookie-ul, redirecționează pe rol (AUTH-08)
+             │   └── useRegister.ts
              ├── context/
+             │   └── AuthContext.tsx  # utilizatorul curent, logout
              ├── components/
+             │   ├── LoginForm.tsx
+             │   └── RegisterForm.tsx     # include alegerea rolului
              ├── views/
+             │   ├── LoginView.tsx
+             │   └── RegisterView.tsx
              ├── schemas/
+             │   └── authSchemas.ts       # Zod: aceleași reguli ca în backend
+             └── types.ts
```

**Dependențe:** `next`, `react`, `react-dom`, `@tanstack/react-query`, `axios`, `react-hook-form`, `@hookform/resolvers`, `zod`. Pentru teste: `vitest`, `@testing-library/react`, `jsdom`.

---

## 3. Etapa 1.2 – Contul utilizatorului

Cerințe acoperite: PROF-01…08, PAROLA-01…05, STERG-01…04.

### 3.1 Rădăcina repository-ului

```
rental-platform/
  └── uploads/
+     ├── avatars/.gitkeep
+     └── listings/.gitkeep
```

### 3.2 Backend

```
…/
  ├── user/
  │   ├── controller/
+ │   │   └── UserController.java            # GET/PUT /api/users/me, POST/DELETE /api/users/me/avatar, DELETE /api/users/me
  │   ├── dto/
+ │   │   ├── UpdateProfileRequest.java      # prenume, nume, telefon, descriere
+ │   │   └── DeleteAccountRequest.java      # parola
  │   ├── service/
+ │   │   ├── UserService.java               # profil și poză
+ │   │   ├── UserServiceImpl.java
+ │   │   ├── AccountService.java            # ștergere / anonimizare
+ │   │   ├── AccountServiceImpl.java        # tranzacție: rulează toți UserDataCleaner, apoi anonimizează
+ │   │   └── UserDataCleaner.java           # interfață: fiecare modul își curăță datele (OCP)
  │   └── event/
+ │       └── UserDeletedEvent.java          # publicat după anonimizare
  │
  ├── auth/
  │   ├── controller/
+ │   │   └── PasswordResetController.java   # POST /api/auth/password-reset/request, /confirm
  │   ├── domain/
+ │   │   └── PasswordResetToken.java
  │   ├── repository/
+ │   │   └── PasswordResetTokenRepository.java
  │   ├── dto/
+ │   │   ├── PasswordResetRequest.java      # email
+ │   │   └── PasswordResetConfirmRequest.java  # token + parolă nouă
  │   └── service/
+ │       ├── PasswordResetService.java
+ │       ├── PasswordResetServiceImpl.java  # răspuns identic indiferent dacă emailul există (PAROLA-04)
+ │       └── PasswordResetTokenCleaner.java # implementează UserDataCleaner
  │
  ├── storage/
+ │   ├── ImageStorageService.java           # interfață: store(file, folder), delete(path), deleteFolder(folder)
+ │   ├── LocalDiskImageStorageService.java  # salvează în uploads/, blochează căile cu „../”
+ │   ├── StorageProperties.java             # app.storage.root, dimensiune maximă, tipuri permise
+ │   ├── ImageValidator.java                # verifică conținutul real al fișierului (PROF-03)
+ │   ├── ImageProcessor.java                # redimensionare și conversie
+ │   ├── StoredImage.java                   # rezultat: calea relativă
+ │   └── UserFilesCleanupListener.java      # @TransactionalEventListener(AFTER_COMMIT) pentru UserDeletedEvent (STERG-04)
  │
  ├── notification/
+ │   ├── EmailService.java                  # interfață
+ │   ├── SmtpEmailService.java              # profilul prod
+ │   └── LoggingEmailService.java           # profilul dev: scrie emailul în log
  │
  ├── config/
+ │   └── StaticResourceConfig.java          # servește /uploads/** din folderul de upload (PROF-07)
  │
~ ├── security/SecurityConfig.java           # permite /api/auth/password-reset/** și GET /uploads/**
~ └── common/exception/GlobalExceptionHandler.java  # MaxUploadSizeExceededException, InvalidImageException → 400

backend/src/main/resources/
~ ├── application.properties                 # app.storage.root=../uploads, spring.mail.*, multipart max 5MB
+ ├── templates/email/password-reset.html
  └── db/migration/
+     └── V3__create_password_reset_tokens_table.sql

backend/src/test/java/ro/rentalplatform/
+ ├── user/UserServiceImplTest.java
+ ├── user/AccountServiceImplTest.java       # parolă greșită → 403, toți cleanerii sunt apelați
+ ├── user/UserControllerTest.java
+ ├── auth/PasswordResetServiceImplTest.java # token expirat / folosit / înlocuit
+ ├── storage/LocalDiskImageStorageServiceTest.java  # „../” refuzat, ștergere
+ └── storage/ImageValidatorTest.java        # fișier .jpg care nu e imagine → refuzat
```

**Dependențe noi:** `spring-boot-starter-mail`, `spring-boot-starter-thymeleaf` (pentru șablonul de email), `thumbnailator` (redimensionarea pozelor).

### 3.3 Frontend

```
src/
  ├── app/
+ │   ├── (auth)/forgot-password/page.tsx
+ │   ├── (auth)/reset-password/page.tsx     # token-ul vine în query (?token=…)
+ │   └── profile/page.tsx
~ ├── proxy.ts                               # /profile cere autentificare
  ├── features/
  │   ├── profile/
+ │   │   ├── api/userApi.ts
+ │   │   ├── hooks/useMe.ts, useUpdateProfile.ts, useUploadAvatar.ts, useDeleteAccount.ts
+ │   │   ├── components/ProfileForm.tsx, AvatarUpload.tsx, DeleteAccountDialog.tsx
+ │   │   ├── views/ProfileView.tsx
+ │   │   ├── schemas/profileSchemas.ts
+ │   │   └── types.ts
  │   └── auth/
~ │       ├── api/authApi.ts                  # requestPasswordReset, confirmPasswordReset
+ │       ├── hooks/useRequestPasswordReset.ts, useConfirmPasswordReset.ts
+ │       ├── views/ForgotPasswordView.tsx, ResetPasswordView.tsx
~ │       └── schemas/authSchemas.ts
  └── shared/
      ├── components/
+     │   ├── ConfirmDialog.tsx               # confirmare pentru acțiuni ireversibile (NF-17)
+     │   └── Avatar.tsx                      # next/image cu src /uploads/…
      └── utils/
+         ├── imageUrl.ts                     # calea relativă → /uploads/…
+         └── validateImageFile.ts            # jpg / png / webp, max 5 MB
```

---

## 4. Etapa 1.3 – Anunțuri

Cerințe acoperite: ANUN-01…05, LOC-01…13, POZE-01…05, GEST-01…05.

### 4.1 Backend

```
…/
  ├── common/
  │   ├── domain/
+ │   │   ├── OwnedResource.java              # interfață: getOwnerId()
+ │   │   └── StatusTransitionPolicy.java     # interfață generică: isAllowed(from, to)
  │   └── security/
+ │       └── OwnershipVerifier.java          # 404 dacă nu există / e DELETED, 403 dacă nu e al tău (GEST-02)
  │
  ├── location/
  │   ├── controller/
+ │   │   └── LocationController.java         # POST /api/location/parse (LOC-02)
  │   ├── dto/
+ │   │   ├── ParseLocationRequest.java
+ │   │   └── CoordinatesDto.java
  │   ├── domain/
+ │   │   └── Coordinates.java                # record; validează intervalele (LOC-08)
  │   ├── service/
+ │   │   ├── LocationService.java            # interfață: parse(input) → Coordinates
+ │   │   └── LocationServiceImpl.java        # link scurt → rezolvare → extragere
  │   ├── parser/
+ │   │   ├── CoordinateExtractor.java        # interfață: Optional<Coordinates> extract(url)
+ │   │   ├── PinDataExtractor.java           # !3d<lat>!4d<lng>          @Order(1)
+ │   │   ├── QueryParamExtractor.java        # q=, query=, ll=           @Order(2)
+ │   │   ├── PathCoordinatesExtractor.java   # /place/…, /search/…       @Order(3)
+ │   │   ├── ViewportExtractor.java          # /@lat,lng,zoom            @Order(4)
+ │   │   ├── RawCoordinatesExtractor.java    # „44.4268, 26.1025” (LOC-10)
+ │   │   └── GoogleMapsLinkParser.java       # încearcă extractorii în ordine
  │   ├── resolver/
+ │   │   ├── ShortLinkResolver.java          # interfață
+ │   │   ├── HttpShortLinkResolver.java      # urmează redirecționările fără să descarce pagina (LOC-06)
+ │   │   └── AllowedHostsPolicy.java         # doar domeniile Google Maps (LOC-07, NF-09)
  │   ├── config/
+ │   │   └── LocationProperties.java         # domenii permise, timeout, număr maxim de redirecționări
  │   └── exception/
+ │       └── CoordinatesNotFoundException.java   # → 422 (LOC-09)
  │
  ├── listing/
  │   ├── domain/
+ │   │   ├── Listing.java                    # implementează OwnedResource; googleMapsUrl (LOC-11)
+ │   │   ├── ListingImage.java
+ │   │   ├── PropertyType.java
+ │   │   ├── Amenity.java
+ │   │   └── ListingStatus.java
  │   ├── repository/
+ │   │   ├── ListingRepository.java
+ │   │   └── ListingImageRepository.java
  │   ├── dto/
+ │   │   ├── CreateListingDto.java           # include googleMapsUrl, nu și coordonatele (LOC-04)
+ │   │   ├── UpdateListingDto.java
+ │   │   ├── ChangeListingStatusDto.java
+ │   │   ├── ListingDto.java
+ │   │   ├── ListingSummaryDto.java
+ │   │   └── ListingImageDto.java
  │   ├── mapper/
+ │   │   └── ListingMapper.java
  │   ├── controller/
+ │   │   ├── MyListingsController.java       # /api/listings (POST, PUT, DELETE, PATCH status), /api/listings/mine
+ │   │   └── ListingImageController.java     # /api/listings/{id}/images (POST, DELETE, PATCH primary)
  │   └── service/
+ │       ├── ListingService.java
+ │       ├── ListingServiceImpl.java         # obține coordonatele prin LocationService
+ │       ├── ListingImageService.java
+ │       ├── ListingImageServiceImpl.java    # max 10 poze, poza principală, ștergerea de pe disc (POZE-05)
+ │       ├── ListingStatusPolicy.java        # ACTIVE ↔ RENTED, ACTIVE ↔ UNAVAILABLE
+ │       └── ListingUserDataCleaner.java     # la ștergerea contului: anunțurile devin DELETED
  │
~ └── common/exception/GlobalExceptionHandler.java  # CoordinatesNotFoundException → 422, tranziție invalidă → 409

backend/src/main/resources/
~ ├── application.properties                  # app.location.* (timeout, max-redirects, allowed-hosts)
  └── db/migration/
+     ├── V4__create_listings_table.sql       # listings + listing_amenities
+     └── V5__create_listing_images_table.sql

backend/src/test/java/ro/rentalplatform/
+ ├── location/parser/*ExtractorTest.java     # câte un test pentru fiecare format, cu linkuri reale
+ ├── location/parser/GoogleMapsLinkParserTest.java  # ordinea priorităților, link de traseu → nimic
+ ├── location/resolver/HttpShortLinkResolverTest.java  # MockWebServer: redirecționări, domeniu interzis, timeout
+ ├── listing/ListingServiceImplTest.java
+ ├── listing/ListingImageServiceImplTest.java # limita de 10, mutarea pozei principale
+ ├── listing/ListingStatusPolicyTest.java
+ └── listing/MyListingsControllerTest.java   # 401 / 403 / 404 / 409
```

**Dependențe noi:** `bucket4j-core` (limita de cereri, LOC-12) și `mockwebserver` (doar pentru teste). Pentru urmărirea redirecționărilor se folosește clientul HTTP din JDK (`java.net.http.HttpClient`), cu redirecționarea automată dezactivată, ca fiecare domeniu să poată fi verificat.

### 4.2 Frontend

```
src/
  ├── app/
  │   └── my-listings/
+ │       ├── page.tsx
+ │       ├── new/page.tsx
+ │       └── [id]/edit/page.tsx
~ ├── proxy.ts                               # /my-listings/** doar pentru LANDLORD
  ├── features/
  │   ├── location/
+ │   │   ├── api/locationApi.ts
+ │   │   ├── hooks/useParseLocation.ts
+ │   │   └── components/
+ │   │       ├── GoogleMapsLinkInput.tsx     # câmp + instrucțiuni (LOC-01) + eroarea 422
+ │   │       └── LocationPreviewMap.tsx      # pin de confirmare (LOC-03)
  │   └── listings/
+ │       ├── api/listingsApi.ts, listingImagesApi.ts
+ │       ├── hooks/useMyListings.ts, useMyListing.ts, useCreateListing.ts, useUpdateListing.ts,
+ │       │        useChangeListingStatus.ts, useDeleteListing.ts, useListingImages.ts
+ │       ├── components/ListingForm.tsx, ListingImagesManager.tsx, ListingStatusBadge.tsx,
+ │       │              ListingStatusActions.tsx, MyListingCard.tsx
+ │       ├── views/MyListingsView.tsx, CreateListingView.tsx, EditListingView.tsx
+ │       ├── schemas/listingSchemas.ts
+ │       ├── constants/amenities.ts, propertyTypes.ts    # etichetele în română
+ │       └── types.ts
  └── shared/
      ├── map/
+     │   ├── BaseMap.tsx                     # Leaflet + tile-uri OpenStreetMap (HARTA-01)
+     │   ├── index.ts                        # exportă hărțile prin next/dynamic cu ssr: false
+     │   └── leafletSetup.ts                 # CSS-ul Leaflet și iconițele implicite ale markerilor
      └── components/
+         └── Tabs.tsx, Pagination.tsx
```

**Dependențe noi:** `leaflet`, `react-leaflet`, `@types/leaflet`.

---

## 5. Etapa 1.4 – Cereri de închiriere

Cerințe acoperite: CER-01…08.

### 5.1 Backend

Modulul se numește `rentalrequest`, pentru a nu se confunda cu cererile HTTP.

```
…/
  └── rentalrequest/
      ├── domain/
+     │   ├── RentalRequest.java              # implementează OwnedResource; googleMapsUrl, rază opțională
+     │   └── RequestStatus.java
      ├── repository/
+     │   └── RentalRequestRepository.java
      ├── dto/
+     │   ├── CreateRentalRequestDto.java     # budgetMin ≤ budgetMax, data nu e în trecut (CER-04)
+     │   ├── UpdateRentalRequestDto.java
+     │   ├── ChangeRequestStatusDto.java
+     │   ├── RentalRequestDto.java
+     │   └── RentalRequestSummaryDto.java
      ├── validation/
+     │   ├── ValidBudgetRange.java           # adnotare de validare la nivel de clasă
+     │   └── BudgetRangeValidator.java
      ├── mapper/
+     │   └── RentalRequestMapper.java
      ├── controller/
+     │   └── MyRentalRequestsController.java # /api/requests (POST, PUT, DELETE, PATCH status), /api/requests/mine
      └── service/
+         ├── RentalRequestService.java
+         ├── RentalRequestServiceImpl.java   # refolosește LocationService și OwnershipVerifier
+         ├── RequestStatusPolicy.java        # ACTIVE ↔ FOUND, ACTIVE ↔ CLOSED
+         └── RentalRequestUserDataCleaner.java

backend/src/main/resources/db/migration/
+ └── V6__create_rental_requests_table.sql

backend/src/test/java/ro/rentalplatform/rentalrequest/
+ ├── RentalRequestServiceImplTest.java
+ ├── RequestStatusPolicyTest.java
+ ├── BudgetRangeValidatorTest.java
+ └── MyRentalRequestsControllerTest.java
```

Etapa nu modifică niciun fișier existent: refolosește `LocationService`, `OwnershipVerifier` și `StatusTransitionPolicy`, iar `AccountService` găsește automat noul `RentalRequestUserDataCleaner` (principiul Open/Closed).

### 5.2 Frontend

```
src/
  ├── app/
  │   └── my-requests/
+ │       ├── page.tsx
+ │       ├── new/page.tsx
+ │       └── [id]/edit/page.tsx
~ ├── proxy.ts                               # /my-requests/** doar pentru TENANT
  ├── features/
  │   └── rentalRequests/
+ │       ├── api/rentalRequestsApi.ts
+ │       ├── hooks/useMyRequests.ts, useMyRequest.ts, useCreateRequest.ts, useUpdateRequest.ts,
+ │       │        useChangeRequestStatus.ts, useDeleteRequest.ts
+ │       ├── components/RequestForm.tsx, RequestZonePicker.tsx (link + slider rază),
+ │       │              RequestStatusBadge.tsx, RequestStatusActions.tsx, MyRequestCard.tsx
+ │       ├── views/MyRequestsView.tsx, CreateRequestView.tsx, EditRequestView.tsx
+ │       ├── schemas/requestSchemas.ts
+ │       └── types.ts
  └── shared/map/
+     └── RadiusCircle.tsx                    # zona căutată, desenată ca cerc (CER-03)
```

---

## 6. Etapa 1.5 – Căutare, detalii și hartă

Cerințe acoperite: CAUT-01…07, DET-01…05, HARTA-01…09.

### 6.1 Backend

Citirile publice stau în controllere și servicii separate de cele care modifică date (Single Responsibility).

```
…/
  ├── common/
  │   └── web/
+ │       └── SortParser.java                 # acceptă doar câmpurile de sortare permise
  │
  ├── listing/
  │   ├── controller/
+ │   │   └── ListingSearchController.java    # GET /api/listings, /api/listings/{id}, /api/listings/map?bbox=
  │   ├── dto/
+ │   │   ├── ListingSearchCriteria.java      # preț, tip, camere, băi, suprafață, oraș, dotări, disponibilitate
+ │   │   ├── ListingDetailsDto.java          # telefonul proprietarului doar pentru utilizatori autentificați (DET-03)
+ │   │   └── ListingMapMarkerDto.java        # id, titlu, preț, poza principală, lat, lng
  │   ├── repository/
+ │   │   └── ListingSpecifications.java      # câte un Specification pentru fiecare filtru, combinate
  │   └── service/
+ │       ├── ListingSearchService.java
+ │       └── ListingSearchServiceImpl.java   # doar ACTIVE, doar proprietari activi (CAUT-05)
  │
  ├── rentalrequest/
  │   ├── controller/
+ │   │   └── RentalRequestSearchController.java  # GET /api/requests, /api/requests/{id} (doar LANDLORD)
  │   ├── dto/
+ │   │   ├── RentalRequestSearchCriteria.java
+ │   │   └── RentalRequestDetailsDto.java
  │   ├── repository/
+ │   │   └── RentalRequestSpecifications.java
  │   └── service/
+ │       ├── RentalRequestSearchService.java
+ │       └── RentalRequestSearchServiceImpl.java
  │
~ └── security/
~     ├── SecurityConfig.java                 # GET /api/listings/** public
~     └── JwtAuthFilter.java                  # token opțional pe rutele publice

backend/src/main/resources/db/migration/
+ └── V7__add_search_indexes.sql              # indexuri pe status, oraș, preț, (latitudine, longitudine)

backend/src/test/java/ro/rentalplatform/
+ ├── listing/ListingSearchServiceImplTest.java   # test de integrare pe baza de test: filtrele combinate
+ ├── listing/ListingSearchControllerTest.java    # vizitator fără telefon, utilizator autentificat cu telefon
+ └── rentalrequest/RentalRequestSearchServiceImplTest.java
```

### 6.2 Frontend

```
src/
  ├── app/
  │   ├── listings/
+ │   │   ├── page.tsx                        # căutare publică
+ │   │   └── [id]/page.tsx                   # detalii anunț, public
  │   └── requests/
+ │       ├── page.tsx                        # doar LANDLORD
+ │       └── [id]/page.tsx
~ ├── proxy.ts                               # /requests/** doar pentru LANDLORD
  ├── features/
  │   ├── search/
+ │   │   ├── hooks/useUrlFilters.ts          # filtrele sincronizate cu URL-ul prin useSearchParams (CAUT-04)
+ │   │   └── components/SortSelect.tsx, ViewToggle.tsx (listă / hartă), ResultsCount.tsx
  │   ├── listings/
+ │   │   ├── hooks/useListingSearch.ts, useListing.ts, useListingsInBounds.ts
+ │   │   ├── components/ListingFilters.tsx, ListingCard.tsx, ListingResultsList.tsx,
+ │   │   │              ListingResultsMap.tsx (markere + grupare), ListingGallery.tsx,
+ │   │   │              ListingDetails.tsx, OwnerContactCard.tsx
+ │   │   └── views/ListingSearchView.tsx, ListingDetailsView.tsx
  │   └── rentalRequests/
+ │       ├── hooks/useRequestSearch.ts, useRequest.ts
+ │       ├── components/RequestFilters.tsx, RequestCard.tsx, RequestDetails.tsx
+ │       └── views/RequestSearchView.tsx, RequestDetailsView.tsx
  ├── shared/map/
+ │   ├── googleMapsUrls.ts                   # linkurile de căutare și de direcții (HARTA-07, HARTA-09)
+ │   ├── OpenInGoogleMapsButton.tsx          # deschide într-un tab nou (HARTA-06, HARTA-08)
+ │   ├── DirectionsButton.tsx
+ │   └── MarkerClusterLayer.tsx              # gruparea markerelor (HARTA-05)
~ └── features/location/components/LocationPreviewMap.tsx   # adaugă OpenInGoogleMapsButton
```

**Dependențe noi:** `react-leaflet-cluster` (gruparea markerelor).

**Opțional, pentru motoarele de căutare:** pagina `listings/[id]/page.tsx` poate încărca anunțul pe server și poate genera titlul și descrierea paginii cu `generateMetadata`. Așa, anunțurile apar mai bine în Google. Restul paginii rămâne interactiv pe client.

---

## 7. Etapa 1.6 – Administrarea utilizatorilor

Cerințe acoperite: ADM-01…11.

### 7.1 Backend

```
…/
  ├── admin/
  │   ├── controller/
+ │   │   └── AdminUserController.java        # /api/admin/users (GET), /{id} (GET, PUT, DELETE), /{id}/avatar (DELETE)
  │   ├── dto/
+ │   │   ├── AdminUserSearchCriteria.java    # căutare, rol, sortare
+ │   │   ├── AdminUserSummaryDto.java
+ │   │   ├── AdminUserDto.java
+ │   │   └── AdminUpdateUserRequest.java     # fără rol și fără parolă (ADM-08)
  │   └── service/
+ │       ├── AdminUserService.java
+ │       ├── AdminUserServiceImpl.java       # refuză conturile ADMIN; ștergerea prin AccountService
+ │       └── AdminActionLogger.java          # log pentru ștergeri: adminId, userId, dată (ADM-10)
  │
  ├── user/
  │   ├── repository/
+ │   │   └── UserSpecifications.java         # fără ADMIN și DELETED, căutare după nume și email
  │   └── service/
~ │       ├── AccountService.java             # + deleteByAdmin(userId, adminId), fără parolă
~ │       └── AccountServiceImpl.java         # aceeași anonimizare, refolosită
  │
~ └── security/SecurityConfig.java            # /api/admin/** → hasRole('ADMIN')

backend/src/test/java/ro/rentalplatform/admin/
+ ├── AdminUserServiceImplTest.java           # cont de admin → 403, email duplicat → 409
+ └── AdminUserControllerTest.java            # utilizator fără rol ADMIN → 403
```

### 7.2 Frontend

```
src/
  ├── app/
  │   └── admin/
+ │       ├── layout.tsx                      # folosește AdminLayout
+ │       └── users/page.tsx
~ ├── proxy.ts                               # /admin/** doar pentru ADMIN
  └── features/
      └── admin/
+         ├── layout/AdminLayout.tsx          # meniul panoului de administrare
          └── users/
+             ├── api/adminUsersApi.ts
+             ├── hooks/useAdminUsers.ts, useAdminUser.ts, useUpdateUser.ts,
+             │        useDeleteUser.ts, useRemoveUserAvatar.ts
+             ├── components/UsersTable.tsx, UsersFilters.tsx, EditUserDialog.tsx, DeleteUserDialog.tsx
+             ├── views/AdminUsersView.tsx
+             ├── schemas/adminUserSchemas.ts
+             └── types.ts
```

---

## 8. Recapitulare: structura finală

### 8.1 Backend

```
backend/src/main/java/ro/rentalplatform/
├── RentalPlatformApplication.java
├── common/          domain, dto, exception, security, web
├── config/          auditare JPA, CORS, resurse statice
├── security/        JWT, filtru, configurarea Spring Security
├── notification/    trimiterea emailurilor
├── storage/         salvarea pozelor pe disc
├── location/        extragerea coordonatelor din linkuri Google Maps
├── auth/            înregistrare, login, resetarea parolei
├── user/            profil, ștergerea contului
├── listing/         anunțuri, poze, căutarea anunțurilor
├── rentalrequest/   cereri, căutarea cererilor
└── admin/           administrarea utilizatorilor
```

### 8.2 Frontend

```
frontend/src/
├── proxy.ts         protecția rutelor după rol
├── app/             doar rutele (page.tsx, layout.tsx)
│   ├── (auth)/      login, register, forgot-password, reset-password
│   ├── profile/
│   ├── my-listings/
│   ├── my-requests/
│   ├── listings/
│   ├── requests/
│   └── admin/
├── shared/          api, auth, components, layout, map, utils, types
└── features/
    ├── auth/
    ├── profile/
    ├── location/
    ├── listings/
    ├── rentalRequests/
    ├── search/
    └── admin/
```

### 8.3 Baza de date

```
backend/src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__seed_admin_user.sql
├── V3__create_password_reset_tokens_table.sql
├── V4__create_listings_table.sql
├── V5__create_listing_images_table.sql
├── V6__create_rental_requests_table.sql
└── V7__add_search_indexes.sql
```
