# Etapa 1.1 – Fundația și autentificarea

> **Obiectiv:** utilizatorii își pot face cont și se pot autentifica, iar aplicația știe ce rol are fiecare.
> **Cerințe acoperite:** INFR-01…05, AUTH-01…11 (din `specificatii-proiect.md`)
> **Depinde de:** –
> **Urmează:** etapa 1.2 – Contul utilizatorului

**Convenții**
- `+` marchează un fișier nou, iar `~` un fișier existent care se modifică.
- `…/` înseamnă `backend/src/main/java/ro/rentalplatform/`.
- Fiecare sub-etapă se termină cu o verificare. Treci la sub-etapa următoare doar după ce verificarea trece.

**Sub-etape**

| # | Sub-etapă | Parte |
|---|---|---|
| 1.1.1 | Repository-ul și structura de foldere | Proiect |
| 1.1.2 | PostgreSQL local | Bază de date |
| 1.1.3 | Proiectul backend | Backend |
| 1.1.4 | Clasele comune și tratarea erorilor | Backend |
| 1.1.5 | Tabela `users` și entitatea `User` | Backend + BD |
| 1.1.6 | Contul de admin (seed) | Backend + BD |
| 1.1.7 | Serviciul JWT | Backend |
| 1.1.8 | Spring Security | Backend |
| 1.1.9 | Înregistrare și login | Backend |
| 1.1.10 | Testele backendului | Backend |
| 1.1.11 | Proiectul frontend | Frontend |
| 1.1.12 | Codul comun din frontend | Frontend |
| 1.1.13 | Paginile de login și înregistrare | Frontend |
| 1.1.14 | Protecția rutelor | Frontend |
| 1.1.15 | Verificarea finală a etapei | – |

---

## 1.1.1 Repository-ul și structura de foldere

**Fișiere**
```
rental-platform/
+ ├── .gitignore          # build/, .gradle/, node_modules/, .next/, .env, .idea/, *.iml
+ ├── .editorconfig       # UTF-8, LF, indentare 4 spații (Java) / 2 spații (TS, JSON)
+ ├── .env.example        # DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, ADMIN_EMAIL, ADMIN_PASSWORD_HASH
+ ├── README.md           # deocamdată doar titlul și descrierea proiectului
+ └── docs/
+     ├── specificatii-proiect.md
+     ├── structura-proiect.md
+     └── diagrams/       # diagramele PlantUML existente
```

**Ce faci**
- Creezi repository-ul pe GitHub sau GitLab și îl clonezi local.
- Adaugi fișierele de mai sus. Folderele `backend/` și `frontend/` apar în sub-etapele 1.1.3 și 1.1.11.
- `.env` (fără „example”) conține valorile reale și nu se pune niciodată în Git.
- Stabiliți în echipă cum lucrați pe ramuri, de exemplu `main` protejat și câte o ramură pentru fiecare sub-etapă.

**Verificare**
- Primul commit apare pe `main`, iar `git status` nu arată fișiere `.env`.

---

## 1.1.2 PostgreSQL local

**Fișiere**
```
~ README.md               # secțiunea „Baza de date”
```

**Ce faci**
- Instalezi PostgreSQL pe calculator (installer-ul oficial pe Windows și macOS, managerul de pachete pe Linux).
- Creezi un utilizator și cele două baze de date:
  ```sql
  CREATE USER rental WITH PASSWORD 'parola-ta';
  CREATE DATABASE rental_platform OWNER rental;
  CREATE DATABASE rental_platform_test OWNER rental;
  ```
- Scrii acești pași în README, ca fiecare coleg să-i poată repeta.

**Verificare**
- Te poți conecta la ambele baze de date cu `psql -U rental -d rental_platform` sau cu pgAdmin / DBeaver.

---

## 1.1.3 Proiectul backend

**Fișiere**
```
backend/
+ ├── settings.gradle                 # rootProject.name = 'rental-platform-backend'
+ ├── build.gradle                    # plugin Spring Boot, Java (LTS), dependențele de mai jos
+ ├── gradlew, gradlew.bat, gradle/wrapper/…
  └── src/main/
+     ├── java/ro/rentalplatform/RentalPlatformApplication.java
      └── resources/
+         ├── application.properties  # datasource din DB_URL / DB_USER / DB_PASSWORD, ddl-auto=validate, profil activ dev
+         ├── application-dev.properties # log SQL, port 8080
+         ├── application-prod.properties
+         └── db/migration/           # gol deocamdată
```

**Ce faci**
- Generezi proiectul cu Spring Initializr (Gradle – Groovy, Java, pachetul `ro.rentalplatform`).
- Adaugi dependențele: `spring-boot-starter-web`, `-security`, `-data-jpa`, `-validation`, `flyway-core`, `flyway-database-postgresql`, `postgresql`, `lombok`, `mapstruct` (cu procesorul de adnotări), `jjwt-api` / `-impl` / `-jackson`, `spring-boot-starter-test`, `spring-security-test`.
- În `application.properties` setezi `spring.jpa.hibernate.ddl-auto=validate` și `spring.jpa.open-in-view=false`.

**Verificare**
- `./gradlew bootRun` pornește aplicația pe portul 8080. În log apare că Flyway s-a conectat și că nu a găsit nicio migrare.

---

## 1.1.4 Clasele comune și tratarea erorilor

**Fișiere**
```
…/common/
  ├── domain/
+ │   └── BaseEntity.java              # @MappedSuperclass: id, createdAt (@CreatedDate), updatedAt (@LastModifiedDate)
  ├── dto/
+ │   └── PageResponse.java            # record: items, page, size, totalElements, totalPages
  └── exception/
+     ├── ApiError.java                # record: status, message, path, timestamp, fieldErrors
+     ├── FieldErrorDto.java           # record: field, message
+     ├── NotFoundException.java
+     ├── ConflictException.java
+     ├── ForbiddenOperationException.java
+     ├── BadCredentialsAppException.java
+     └── GlobalExceptionHandler.java  # @RestControllerAdvice
…/config/
+ └── JpaAuditingConfig.java           # @EnableJpaAuditing
```

**Ce faci**
- `GlobalExceptionHandler` transformă fiecare excepție în `ApiError`, cu codul corect:

  | Excepție | Cod HTTP |
  |---|---|
  | `MethodArgumentNotValidException` | `400`, cu `fieldErrors` |
  | `BadCredentialsAppException` | `401` |
  | `ForbiddenOperationException` | `403` |
  | `NotFoundException` | `404` |
  | `ConflictException` | `409` |
  | orice altă excepție | `500`, cu un mesaj generic și detaliile doar în log |

**Verificare**
- Proiectul compilează (`./gradlew build`). Testarea propriu-zisă vine odată cu primul endpoint, în 1.1.9.

---

## 1.1.5 Tabela `users` și entitatea `User`

**Fișiere**
```
backend/src/main/resources/db/migration/
+ └── V1__create_users_table.sql
…/user/
  ├── domain/
+ │   ├── Role.java                # TENANT, LANDLORD, ADMIN
+ │   ├── UserStatus.java          # ACTIVE, BLOCKED, DELETED
+ │   └── User.java                # extinde BaseEntity
  └── repository/
+     └── UserRepository.java      # findByEmail, existsByEmail
```

**Ce faci**
- În `V1`, tabela `users` are coloanele: `id` (bigserial), `email` (unic), `password_hash`, `first_name`, `last_name`, `phone` (nullable), `avatar_url` (nullable), `bio` (nullable), `role`, `status`, `created_at`, `updated_at`.
- Rolul și statusul sunt `varchar`, cu o constrângere `CHECK` pe valorile permise. În entitate se mapează cu `@Enumerated(EnumType.STRING)`.
- Pentru unicitatea emailului, salvează-l mereu cu litere mici. Indexul unic poate fi chiar pe `lower(email)`.

**Verificare**
- La pornire, Flyway aplică `V1`. În baza de date există tabela `users`, iar în `flyway_schema_history` apare migrarea.
- Aplicația pornește fără erori. Asta înseamnă că entitatea `User` corespunde tabelei, pentru că `ddl-auto=validate` ar opri pornirea altfel.

---

## 1.1.6 Contul de admin (seed)

**Fișiere**
```
backend/src/main/resources/
~ ├── application.properties           # spring.flyway.placeholders.adminEmail / adminPasswordHash din variabile de mediu
  └── db/migration/
+     └── V2__seed_admin_user.sql      # INSERT INTO users (…, role, status) VALUES ('${adminEmail}', '${adminPasswordHash}', …, 'ADMIN', 'ACTIVE')
```

**Ce faci**
- Generezi o singură dată hash-ul BCrypt al parolei de admin, de exemplu dintr-un test mic sau dintr-un generator BCrypt, și îl pui în `.env`, în `ADMIN_PASSWORD_HASH`.
- Valoarea hash-ului conține caractere `$`, deci pune-o între ghilimele simple în `.env`.

**Verificare**
- `SELECT email, role FROM users;` arată contul de admin.

---

## 1.1.7 Serviciul JWT

**Fișiere**
```
…/security/
+ ├── JwtProperties.java             # @ConfigurationProperties("app.jwt"): secret, expiration
+ ├── UserPrincipal.java             # record: id, email, role
+ ├── JwtService.java                # interfață: generateToken(UserPrincipal), parseToken(String)
+ └── JwtServiceImpl.java            # implementare cu JJWT, HS256
~ backend/src/main/resources/application.properties # app.jwt.secret=${JWT_SECRET}, app.jwt.expiration=24h
backend/src/test/java/ro/rentalplatform/security/
+ └── JwtServiceImplTest.java
```

**Ce faci**
- Token-ul conține în `sub` id-ul utilizatorului, plus claim-urile `email` și `role`.
- Secretul are cel puțin 32 de caractere și vine doar din variabila de mediu `JWT_SECRET`.
- Testul verifică trei cazuri: un token generat se citește corect, un token expirat este refuzat, iar un token modificat este refuzat.

**Verificare**
- `./gradlew test --tests '*JwtServiceImplTest'` trece.

---

## 1.1.8 Spring Security

**Fișiere**
```
…/security/
+ ├── JwtAuthFilter.java                     # OncePerRequestFilter
+ ├── RestAuthenticationEntryPoint.java      # 401 în formatul ApiError
+ ├── RestAccessDeniedHandler.java           # 403 în formatul ApiError
+ ├── CurrentUserProvider.java               # interfață: getCurrentUserId()
+ ├── SecurityContextCurrentUserProvider.java
+ └── SecurityConfig.java
…/config/
+ └── CorsConfig.java                        # originea frontendului (http://localhost:3000)
```

**Ce faci**
- `JwtAuthFilter` citește antetul `Authorization: Bearer …`, validează token-ul, încarcă utilizatorul din baza de date și verifică statusul contului. Dacă statusul nu este `ACTIVE`, cererea primește `401` (NF-06).
- În `SecurityConfig`:
  - sesiunea este `STATELESS`, iar CSRF este dezactivat (API cu JWT);
  - `POST /api/auth/register` și `POST /api/auth/login` sunt publice, iar restul rutelor cer autentificare;
  - activezi `@EnableMethodSecurity`, pentru a putea folosi `@PreAuthorize`;
  - definești bean-ul `PasswordEncoder` (BCrypt).

**Verificare**
- Orice cerere fără token, de exemplu `GET /api/oricare`, primește `401` cu un corp JSON în formatul `ApiError`.

---

## 1.1.9 Înregistrare și login

**Fișiere**
```
…/user/
  ├── dto/
+ │   └── UserDto.java                 # id, email, firstName, lastName, phone, avatarUrl, bio, role
  └── mapper/
+     └── UserMapper.java              # MapStruct: User → UserDto
…/auth/
  ├── dto/
+ │   ├── RegisterRequest.java         # @Email, @NotBlank, @Size(min = 8) pentru parolă, rol
+ │   ├── LoginRequest.java
+ │   └── AuthResponse.java            # token + UserDto
  ├── service/
+ │   ├── AuthService.java
+ │   └── AuthServiceImpl.java
  └── controller/
+     └── AuthController.java          # POST /api/auth/register → 201, POST /api/auth/login → 200
```

**Ce faci**
- `register` respinge rolul `ADMIN` (AUTH-04) și emailul deja folosit (`409`, AUTH-03). Parola se salvează cu `passwordEncoder.encode` (AUTH-05).
- `login` întoarce același mesaj de eroare (`401`) atât pentru un email inexistent, cât și pentru o parolă greșită (AUTH-07).
- Controllerul doar validează (`@Valid`) și deleagă către `AuthService` (NF-08).

**Verificare** (cu Postman, Insomnia sau curl)
```bash
curl -i -X POST localhost:8080/api/auth/register -H 'Content-Type: application/json' \
  -d '{"email":"ana@test.ro","password":"parola123","firstName":"Ana","lastName":"Pop","role":"TENANT"}'
# 201 + UserDto, fără parolă

curl -i -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"ana@test.ro","password":"parola123"}'
# 200 + { token, user }
```
- Aceeași înregistrare încă o dată → `409`. Rolul `ADMIN` → `400` sau `403`. Parolă greșită → `401`. Un câmp lipsă → `400` cu `fieldErrors`.

---

## 1.1.10 Testele backendului

**Fișiere**
```
backend/src/test/
  ├── resources/
+ │   └── application-test.properties         # conexiunea la rental_platform_test, spring.flyway.clean-disabled=false
  └── java/ro/rentalplatform/
+     ├── AbstractIntegrationTest.java        # @SpringBootTest, @ActiveProfiles("test"), Flyway clean + migrate
+     └── auth/
+         ├── AuthServiceImplTest.java        # Mockito: email duplicat, rol ADMIN, parolă greșită
+         └── AuthControllerTest.java         # @WebMvcTest: 400 cu fieldErrors, 201, 401
```

**Ce faci**
- `AbstractIntegrationTest` este clasa de bază pentru testele care au nevoie de baza de date reală. Testele simple de serviciu folosesc doar Mockito, fără Spring.

**Verificare**
- `./gradlew test` trece în întregime.

---

## 1.1.11 Proiectul frontend

**Fișiere**
```
frontend/
+ ├── package.json
+ ├── next.config.ts                 # rewrites: /api/:path* și /uploads/:path* → ${BACKEND_URL}
+ ├── tsconfig.json                  # alias @/* → src/*
+ ├── .env.example                   # BACKEND_URL=http://localhost:8080
+ ├── eslint.config.mjs, .prettierrc
+ ├── public/
  └── src/app/
+     ├── layout.tsx                 # <html lang="ro">, deocamdată fără providers
+     ├── globals.css
+     ├── page.tsx                   # pagina principală, provizorie
+     └── not-found.tsx
```

**Ce faci**
- Creezi proiectul cu `npx create-next-app@latest frontend`, alegând TypeScript, ESLint, App Router și folderul `src/`.
- Instalezi dependențele: `@tanstack/react-query`, `axios`, `react-hook-form`, `@hookform/resolvers`, `zod`. Pentru teste: `vitest`, `@testing-library/react`, `jsdom`.
- Configurezi `rewrites` în `next.config.ts`, ca apelurile către `/api/...` să ajungă la backend.

**Verificare**
- `npm run dev` pornește pe `http://localhost:3000`.
- Cu backendul pornit, `http://localhost:3000/api/auth/login` (cerere GET) ajunge la backend. Răspunsul e o eroare de la Spring, nu pagina 404 din Next.js.

---

## 1.1.12 Codul comun din frontend

**Fișiere**
```
frontend/src/
  ├── shared/
  │   ├── types/
+ │   │   └── api.ts                 # PageResponse<T>, ApiError, FieldError
  │   ├── auth/
+ │   │   ├── tokenCookie.ts         # getToken, setToken, clearToken (cookie access_token)
+ │   │   └── decodeToken.ts         # rolul și expirarea din payload, fără verificarea semnăturii
  │   ├── api/
+ │   │   ├── httpClient.ts          # Axios cu baseURL "/api"; interceptor: Bearer; la 401 → clearToken + /login
+ │   │   ├── apiError.ts            # extrage mesajul și erorile pe câmpuri din răspuns
+ │   │   └── queryClient.ts
  │   └── components/
+ │       ├── Button.tsx, Input.tsx, Select.tsx, FormField.tsx
+ │       ├── Spinner.tsx, EmptyState.tsx
+ │       └── Toaster.tsx
  └── app/
+     ├── providers.tsx              # "use client": QueryClientProvider + Toaster
~     └── layout.tsx                 # învelește conținutul în <Providers>
```

**Ce faci**
- Toate apelurile HTTP trec prin `httpClient.ts`. Nicio componentă nu importă Axios direct.
- Cookie-ul `access_token` are `SameSite=Lax`, `Secure` în producție și aceeași expirare ca token-ul.

**Verificare**
- `npm run lint` și `npm run build` trec fără erori.

---

## 1.1.13 Paginile de login și înregistrare

**Fișiere**
```
frontend/src/
  ├── features/auth/
+ │   ├── types.ts                   # RegisterRequest, LoginRequest, AuthResponse, User, Role
+ │   ├── schemas/authSchemas.ts     # Zod, cu aceleași reguli ca în backend
+ │   ├── api/authApi.ts             # register(), login()
+ │   ├── context/AuthContext.tsx    # utilizatorul curent, logout()
+ │   ├── hooks/useRegister.ts       # useMutation → redirect la /login cu mesaj de succes
+ │   ├── hooks/useLogin.ts          # useMutation → setToken, setează utilizatorul, redirect pe rol
+ │   ├── components/LoginForm.tsx   # React Hook Form + zodResolver
+ │   ├── components/RegisterForm.tsx  # include alegerea rolului: Chiriaș / Proprietar
+ │   ├── views/LoginView.tsx
+ │   └── views/RegisterView.tsx
  ├── shared/layout/
+ │   └── Navbar.tsx                 # link-uri diferite pe rol, buton „Deconectare”
  └── app/
+     ├── (auth)/login/page.tsx      # afișează <LoginView />
+     ├── (auth)/register/page.tsx
~     ├── providers.tsx              # + AuthProvider
~     └── layout.tsx                 # + Navbar
```

**Ce faci**
- Erorile `400` venite de la backend se afișează la câmpul corespunzător (`setError` din React Hook Form). Eroarea `409` apare la câmpul email.
- Redirecționarea după login: chiriașul și proprietarul ajung pe pagina principală, iar adminul pe `/admin/users`. Pagina de admin apare abia în etapa 1.6, deci până atunci adminul va vedea pagina 404.
- La deconectare se șterge cookie-ul, se golește cache-ul TanStack Query și se face redirecționare la `/login`.

**Verificare**
- Îți faci cont din interfață, te autentifici, vezi numele în Navbar, te deconectezi.
- Erorile de validare apar la câmpurile corecte.

---

## 1.1.14 Protecția rutelor

**Fișiere**
```
frontend/src/
+ └── proxy.ts                       # Next.js 16 (în versiunile mai vechi: middleware.ts)
```

**Ce faci**
- `proxy.ts` citește cookie-ul `access_token` și rolul din el, apoi:
  - trimite utilizatorii neautentificați de pe rutele protejate la `/login?next=<ruta>`;
  - trimite utilizatorii cu rol greșit la `/`;
  - trimite utilizatorii deja autentificați de pe `/login` și `/register` la `/`.
- Lista de rute protejate și rolurile lor stau într-un singur obiect, completat în etapele următoare:
  ```ts
  const protectedRoutes = [
    // { prefix: '/profile', roles: ['TENANT', 'LANDLORD', 'ADMIN'] },  ← etapa 1.2
    // { prefix: '/my-listings', roles: ['LANDLORD'] },                  ← etapa 1.3
  ];
  ```
- Securitatea reală este în backend. `proxy.ts` doar evită afișarea paginilor la care utilizatorul oricum nu are acces.

**Verificare**
- Deocamdată nu există rute protejate. Verificarea completă vine în etapa 1.2, odată cu `/profile`.
- Un utilizator autentificat care deschide `/login` este trimis la `/`.

---

## 1.1.15 Verificarea finală a etapei

| Verificare | Cerință |
|---|---|
| Un coleg nou poate porni proiectul doar urmând README-ul | INFR-01 |
| Tabelele sunt create doar de Flyway, iar aplicația pornește cu `ddl-auto=validate` | INFR-02 |
| Contul de admin există după prima pornire | INFR-03 |
| Toate erorile API au același format JSON | INFR-04 |
| Nicio parolă sau cheie nu apare în cod sau în Git | INFR-05 |
| Înregistrare cu Chiriaș / Proprietar; ADMIN refuzat; email duplicat → 409 | AUTH-01…04 |
| Parola nu apare în niciun răspuns, iar în baza de date e hash BCrypt | AUTH-05 |
| Login → JWT; mesaj generic la eroare; redirect pe rol; deconectare | AUTH-06…09 |
| Fără token → 401 | AUTH-10 |
| `proxy.ts` există și redirecționează pe rol (se verifică complet în etapa 1.2) | AUTH-11 |
| `./gradlew test`, `npm run lint` și `npm run build` trec | – |
