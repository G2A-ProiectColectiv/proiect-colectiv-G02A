# Etapa 1.2 – Contul utilizatorului

> **Obiectiv:** utilizatorul își administrează singur contul: profil, poză, resetarea parolei, ștergerea contului.
> **Cerințe acoperite:** PROF-01…08, PAROLA-01…05, STERG-01…04
> **Depinde de:** etapa 1.1
> **Urmează:** etapa 1.3 – Anunțuri (se poate lucra în paralel cu această etapă)

**Convenții:** `+` fișier nou, `~` fișier modificat, `…/` = `backend/src/main/java/ro/rentalplatform/`.

**Sub-etape**

| # | Sub-etapă | Parte |
|---|---|---|
| 1.2.1 | Folderul `uploads/` | Proiect |
| 1.2.2 | Salvarea pozelor pe disc | Backend |
| 1.2.3 | Servirea pozelor | Backend |
| 1.2.4 | Profilul: citire și editare | Backend |
| 1.2.5 | Poza de profil | Backend |
| 1.2.6 | Trimiterea emailurilor | Backend |
| 1.2.7 | Resetarea parolei | Backend + BD |
| 1.2.8 | Ștergerea contului | Backend |
| 1.2.9 | Componente comune noi | Frontend |
| 1.2.10 | Pagina de profil | Frontend |
| 1.2.11 | Paginile de resetare a parolei | Frontend |
| 1.2.12 | Ștergerea contului din interfață | Frontend |
| 1.2.13 | Verificarea finală a etapei | – |

---

## 1.2.1 Folderul `uploads/`

**Fișiere**
```
rental-platform/
  └── uploads/
+     ├── avatars/.gitkeep
+     └── listings/.gitkeep
~ backend/src/main/resources/application.properties # app.storage.root=../uploads, spring.servlet.multipart.max-file-size=5MB
```

**Ce faci**
- Folderul stă în rădăcina repository-ului și este versionat în Git (PROF-04). Verifică să nu fie prins de vreo regulă din `.gitignore`.
- Calea `../uploads` este relativă la folderul `backend/`, de unde pornește aplicația.

**Verificare**
- `git status` arată cele două fișiere `.gitkeep`.

---

## 1.2.2 Salvarea pozelor pe disc

**Fișiere**
```
…/storage/
+ ├── StorageProperties.java            # @ConfigurationProperties("app.storage"): root, maxSize, allowedTypes
+ ├── StoredImage.java                  # record: relativePath (ex. "avatars/9f2c….webp")
+ ├── InvalidImageException.java        # → 400
+ ├── ImageValidator.java               # verifică semnătura reală a fișierului (magic bytes) și dimensiunea
+ ├── ImageProcessor.java               # redimensionează (latura maximă 1600 px) și convertește
+ ├── ImageStorageService.java          # interfață: store(file, folder), delete(path), deleteFolder(folder)
+ └── LocalDiskImageStorageService.java
~ build.gradle                          # + net.coobird:thumbnailator
~ …/common/exception/GlobalExceptionHandler.java   # InvalidImageException, MaxUploadSizeExceededException → 400
backend/src/test/java/ro/rentalplatform/storage/
+ ├── ImageValidatorTest.java           # text redenumit în .jpg → refuzat; png real → acceptat
+ └── LocalDiskImageStorageServiceTest.java   # @TempDir: salvare, ștergere, „../” refuzat
```

**Ce faci**
- Numele fișierului salvat este generat de server (UUID + extensie). Numele trimis de utilizator nu se folosește niciodată.
- Înainte de orice scriere sau ștergere, calea finală se normalizează (`normalize()`) și se verifică să fie în interiorul folderului de upload. Altfel se aruncă o excepție.

**Verificare**
- Cele două teste trec.

---

## 1.2.3 Servirea pozelor

**Fișiere**
```
…/config/
+ └── StaticResourceConfig.java     # addResourceHandlers: /uploads/** → file:${app.storage.root}/
~ …/security/SecurityConfig.java    # permitAll pentru GET /uploads/**
```

**Ce faci**
- Copiezi manual o imagine în `uploads/avatars/test.jpg`, ca să ai ce servi.

**Verificare**
- `http://localhost:8080/uploads/avatars/test.jpg` afișează imaginea, iar prin frontend `http://localhost:3000/uploads/avatars/test.jpg` (rewrite) la fel.
- `http://localhost:8080/uploads/../build.gradle` nu întoarce fișierul. Șterge apoi imaginea de test.

---

## 1.2.4 Profilul: citire și editare

**Fișiere**
```
…/user/
  ├── dto/
+ │   └── UpdateProfileRequest.java   # firstName, lastName, phone (opțional, format validat), bio (max 1000)
  ├── service/
+ │   ├── UserService.java            # getCurrentUser, updateProfile
+ │   └── UserServiceImpl.java
  └── controller/
+     └── UserController.java         # GET /api/users/me, PUT /api/users/me
backend/src/test/java/ro/rentalplatform/user/
+ ├── UserServiceImplTest.java
+ └── UserControllerTest.java
```

**Ce faci**
- Id-ul utilizatorului vine din `CurrentUserProvider`, niciodată din URL sau din corpul cererii.
- `UpdateProfileRequest` nu are câmpurile email și rol, deci nu pot fi modificate pe această cale (PROF-01).

**Verificare**
- `GET /api/users/me` cu token → `200` și datele tale. Fără token → `401`.
- `PUT /api/users/me` cu un telefon invalid → `400`, iar cu date valide → `200` și datele actualizate.

---

## 1.2.5 Poza de profil

**Fișiere**
```
~ …/user/service/UserService.java       # + updateAvatar(file), removeAvatar()
~ …/user/service/UserServiceImpl.java
~ …/user/controller/UserController.java # + POST /api/users/me/avatar (multipart), DELETE /api/users/me/avatar
~ backend/src/test/…/user/UserServiceImplTest.java
```

**Ce faci**
- La încărcare: salvezi poza nouă în `avatars/`, actualizezi `avatar_url`, apoi ștergi poza veche de pe disc (PROF-02).
- În baza de date se salvează doar calea relativă (PROF-06).

**Verificare**
- Încărcarea unui jpg → `200`, iar fișierul apare în `uploads/avatars/`. O a doua încărcare înlocuiește fișierul vechi.
- Un fișier de 6 MB sau un PDF → `400`.

---

## 1.2.6 Trimiterea emailurilor

**Fișiere**
```
…/notification/
+ ├── EmailService.java              # interfață: send(to, subject, templateName, variables)
+ ├── LoggingEmailService.java       # @Profile("dev"): scrie emailul în log
+ └── SmtpEmailService.java          # @Profile("prod"): JavaMailSender + Thymeleaf
~ build.gradle                       # + spring-boot-starter-mail, spring-boot-starter-thymeleaf
~ backend/src/main/resources/application-prod.properties # spring.mail.* din variabile de mediu
~ .env.example                       # + MAIL_HOST, MAIL_PORT, MAIL_USER, MAIL_PASSWORD, APP_FRONTEND_URL
```

**Ce faci**
- În dezvoltare nu ai nevoie de un server SMTP: emailul (inclusiv linkul) apare în log.

**Verificare**
- Aplicația pornește în profilul `dev` și folosește `LoggingEmailService`.

---

## 1.2.7 Resetarea parolei

**Fișiere**
```
backend/src/main/resources/
+ ├── db/migration/V3__create_password_reset_tokens_table.sql   # id, user_id (FK), token_hash (unic), expires_at, used, created_at
+ └── templates/email/password-reset.html
…/auth/
  ├── domain/
+ │   └── PasswordResetToken.java
  ├── repository/
+ │   └── PasswordResetTokenRepository.java
  ├── dto/
+ │   ├── PasswordResetRequest.java          # email
+ │   └── PasswordResetConfirmRequest.java   # token, newPassword (aceleași reguli ca la înregistrare)
  ├── service/
+ │   ├── PasswordResetService.java
+ │   └── PasswordResetServiceImpl.java
  └── controller/
+     └── PasswordResetController.java       # POST /api/auth/password-reset/request → 204
+                                            # POST /api/auth/password-reset/confirm → 204
~ …/security/SecurityConfig.java              # permitAll pentru /api/auth/password-reset/**
backend/src/test/java/ro/rentalplatform/auth/
+ └── PasswordResetServiceImplTest.java
```

**Ce faci**
- Token-ul este un șir aleator lung (`SecureRandom`). În baza de date se salvează doar hash-ul lui (SHA-256), iar în email ajunge valoarea originală.
- La o cerere nouă de resetare, tokenurile vechi ale aceluiași cont se invalidează (PAROLA-05).
- `request` întoarce mereu `204`, indiferent dacă emailul există sau nu (PAROLA-04).
- `confirm` refuză tokenurile expirate (după o oră), folosite sau inexistente, cu `400`. După succes, tokenul se marchează ca folosit.
- Linkul din email are forma `${APP_FRONTEND_URL}/reset-password?token=…`.

**Verificare**
- Ceri resetarea, copiezi linkul din log și trimiți `confirm` cu o parolă nouă. Login-ul merge cu parola nouă, dar nu și cu cea veche.
- Același token folosit a doua oară → `400`. Testul acoperă și expirarea.

---

## 1.2.8 Ștergerea contului

**Fișiere**
```
…/user/
  ├── dto/
+ │   └── DeleteAccountRequest.java        # password
  ├── event/
+ │   └── UserDeletedEvent.java            # record: userId, avatarPath
  ├── service/
+ │   ├── UserDataCleaner.java             # interfață: void cleanUp(Long userId)
+ │   ├── AccountService.java              # deleteOwnAccount(password)
+ │   └── AccountServiceImpl.java          # @Transactional
  └── controller/
~     └── UserController.java              # + DELETE /api/users/me → 204
…/auth/service/
+ └── PasswordResetTokenCleaner.java        # implementează UserDataCleaner
…/storage/
+ └── UserFilesCleanupListener.java        # @TransactionalEventListener(phase = AFTER_COMMIT)
backend/src/test/java/ro/rentalplatform/user/
+ └── AccountServiceImplTest.java
```

**Ce faci**
- `AccountServiceImpl` primește prin constructor `List<UserDataCleaner>` și îi apelează pe toți. În această etapă există un singur cleaner, cel pentru tokenuri. Etapele 1.3 și 1.4 adaugă câte unul, fără să modifice `AccountServiceImpl`.
- După cleaneri urmează anonimizarea contului: status `DELETED`, email `deleted_{id}@removed.local`, nume „Utilizator șters”, telefon, poză și descriere `NULL`, parolă aleatoare (STERG-02).
- La final se publică `UserDeletedEvent`. Listener-ul șterge avatarul de pe disc doar după ce tranzacția a reușit (STERG-04).
- O parolă greșită dă `403` (STERG-01).

**Verificare**
- După ștergere, datele contului din baza de date sunt anonimizate, iar poza a dispărut din `uploads/avatars/`.
- O cerere cu token-ul vechi → `401` (STERG-03). Login-ul cu emailul vechi → `401`.
- Testul verifică faptul că toți cleanerii sunt apelați și că parola greșită dă `403`.

---

## 1.2.9 Componente comune noi

**Fișiere**
```
frontend/src/shared/
  ├── components/
+ │   ├── ConfirmDialog.tsx          # titlu, mesaj, buton de confirmare (opțional cu un câmp, ex. parola)
+ │   └── Avatar.tsx                 # next/image; inițialele numelui dacă nu există poză
  └── utils/
+     ├── imageUrl.ts                # "avatars/x.webp" → "/uploads/avatars/x.webp"
+     └── validateImageFile.ts       # jpg / png / webp, max 5 MB → mesaj de eroare sau null
```

**Verificare**
- `npm run build` trece.

---

## 1.2.10 Pagina de profil

**Fișiere**
```
frontend/src/
  ├── features/profile/
+ │   ├── types.ts
+ │   ├── schemas/profileSchemas.ts
+ │   ├── api/userApi.ts             # getMe, updateProfile, uploadAvatar, removeAvatar
+ │   ├── hooks/useMe.ts
+ │   ├── hooks/useUpdateProfile.ts  # invalidează ["me"] și actualizează AuthContext
+ │   ├── hooks/useUploadAvatar.ts
+ │   ├── components/ProfileForm.tsx
+ │   ├── components/AvatarUpload.tsx  # previzualizare înainte de trimitere, validateImageFile
+ │   └── views/ProfileView.tsx
  ├── app/
+ │   └── profile/page.tsx
  ├── shared/layout/Navbar.tsx       ~ # avatar + link „Profilul meu”
  └── proxy.ts                       ~ # /profile → orice utilizator autentificat
```

**Verificare**
- Neautentificat, `/profile` te trimite la `/login?next=/profile`, iar după login ajungi înapoi pe `/profile` (verifică AUTH-11).
- Modifici numele și poza, iar schimbările apar imediat și în Navbar.

---

## 1.2.11 Paginile de resetare a parolei

**Fișiere**
```
frontend/src/
  ├── features/auth/
~ │   ├── api/authApi.ts                       # + requestPasswordReset, confirmPasswordReset
~ │   ├── schemas/authSchemas.ts               # + emailul pentru resetare, parola nouă + confirmare
+ │   ├── hooks/useRequestPasswordReset.ts
+ │   ├── hooks/useConfirmPasswordReset.ts
+ │   ├── views/ForgotPasswordView.tsx         # după trimitere: „Dacă există un cont, ai primit un email”
+ │   ├── views/ResetPasswordView.tsx          # citește ?token= cu useSearchParams
~ │   └── components/LoginForm.tsx             # + link „Ai uitat parola?”
  └── app/(auth)/
+     ├── forgot-password/page.tsx
+     └── reset-password/page.tsx
```

**Verificare**
- Fluxul complet din interfață: „Ai uitat parola?” → email → linkul din log → parolă nouă → login.
- Un link cu token invalid afișează un mesaj clar și un link spre „Ai uitat parola?”.

---

## 1.2.12 Ștergerea contului din interfață

**Fișiere**
```
frontend/src/features/profile/
+ ├── hooks/useDeleteAccount.ts              # la succes: logout() + redirect la / + mesaj
+ ├── components/DeleteAccountDialog.tsx     # ConfirmDialog cu câmp pentru parolă
~ └── views/ProfileView.tsx                  # + secțiunea „Șterge contul”
```

**Verificare**
- O parolă greșită afișează eroarea în dialog. Cu parola corectă, contul se șterge și ești deconectat.

---

## 1.2.13 Verificarea finală a etapei

| Verificare | Cerință |
|---|---|
| Editarea profilului; emailul și rolul nu se pot schimba | PROF-01 |
| Poza: jpg / png / webp ≤ 5 MB; poza veche dispare de pe disc | PROF-02, PROF-03 |
| Pozele sunt în `uploads/`, în Git, cu structura `avatars/` și `listings/` | PROF-04, PROF-05 |
| În baza de date e doar calea relativă; `/uploads/**` servește pozele, iar `../` e blocat | PROF-06, PROF-07 |
| Toată logica de fișiere e în `ImageStorageService` | PROF-08 |
| Resetarea parolei: link pe email, token unic, expirare, de unică folosință, același răspuns pentru orice email | PAROLA-01…05 |
| Ștergerea contului: parola cerută, anonimizare, acces pierdut imediat, fișiere șterse după commit | STERG-01…04 |
| `./gradlew test`, `npm run lint` și `npm run build` trec | – |
