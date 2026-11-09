# Etapa 1.6 – Administrarea utilizatorilor

> **Obiectiv:** adminul vede toți utilizatorii și îi poate căuta, edita și șterge.
> **Cerințe acoperite:** ADM-01…11
> **Depinde de:** etapa 1.2 (`AccountService`, `ImageStorageService`); poate începe imediat după ea
> **Notă:** etapa folosește `SortParser` (backend) și `useUrlFilters` (frontend), create în etapa 1.5. Dacă lucrezi 1.6 înaintea etapei 1.5, creezi aceste două fișiere aici, exact cum sunt descrise în sub-etapele 1.5.1 și 1.5.7.
> **Urmează:** – (ultima etapă din faza 1)

**Convenții:** `+` fișier nou, `~` fișier modificat, `…/` = `backend/src/main/java/ro/rentalplatform/`.

**Sub-etape**

| # | Sub-etapă | Parte |
|---|---|---|
| 1.6.1 | Filtrele pentru utilizatori | Backend |
| 1.6.2 | DTO-urile de admin | Backend |
| 1.6.3 | Lista și detaliile utilizatorilor | Backend |
| 1.6.4 | Editarea unui utilizator | Backend |
| 1.6.5 | Ștergerea unui utilizator | Backend |
| 1.6.6 | Panoul de administrare | Frontend |
| 1.6.7 | Datele și apelurile pentru admin | Frontend |
| 1.6.8 | Tabelul de utilizatori | Frontend |
| 1.6.9 | Editarea din interfață | Frontend |
| 1.6.10 | Ștergerea din interfață | Frontend |
| 1.6.11 | Verificarea finală a etapei și a fazei 1 | – |

---

## 1.6.1 Filtrele pentru utilizatori

**Fișiere**
```
…/user/repository/
+ ├── UserSpecifications.java        # notAdmin(), notDeleted(), search(text), hasRole(role)
~ └── UserRepository.java            # + extends JpaSpecificationExecutor<User>
```

**Ce faci**
- `search(text)` caută fără să conteze literele mari și mici, în email, prenume, nume și în numele complet (`first_name || ' ' || last_name`).
- `notAdmin()` și `notDeleted()` se aplică mereu (ADM-05).

**Verificare**
- Proiectul compilează. Testarea se face în 1.6.3.

---

## 1.6.2 DTO-urile de admin

**Fișiere**
```
…/admin/dto/
+ ├── AdminUserSearchCriteria.java    # search, role (TENANT | LANDLORD)
+ ├── AdminUserSummaryDto.java       # id, nume, email, telefon, rol, avatar, createdAt (ADM-03)
+ ├── AdminUserDto.java              # + bio
+ └── AdminUpdateUserRequest.java    # firstName, lastName, email, phone, bio — fără rol și fără parolă (ADM-08)
```

**Verificare**
- Proiectul compilează.

---

## 1.6.3 Lista și detaliile utilizatorilor

**Fișiere**
```
…/admin/
  ├── service/
+ │   ├── AdminUserService.java          # search, getById
+ │   └── AdminUserServiceImpl.java
  └── controller/
+     └── AdminUserController.java       # GET /api/admin/users?search=&role=&sort=&page=
+                                        # GET /api/admin/users/{id}
~ …/security/SecurityConfig.java          # /api/admin/** → hasRole('ADMIN') (ADM-02)
backend/src/test/java/ro/rentalplatform/admin/
+ ├── AdminUserServiceImplTest.java
+ └── AdminUserControllerTest.java
```

**Ce faci**
- Câmpurile de sortare permise: `createdAt`, `lastName`, `email` (prin `SortParser` din etapa 1.5).
- `getById` pentru un cont de admin sau un cont șters dă `404`.

**Verificare**
- Autentificat ca admin: lista nu conține admini sau conturi șterse, iar căutarea „ana” găsește după nume și după email.
- Ca TENANT → `403`. Fără token → `401`.

---

## 1.6.4 Editarea unui utilizator

**Fișiere**
```
~ …/admin/service/AdminUserService.java          # + update(id, request), removeAvatar(id)
~ …/admin/service/AdminUserServiceImpl.java
~ …/admin/controller/AdminUserController.java    # + PUT /api/admin/users/{id}, DELETE /api/admin/users/{id}/avatar → 204
~ backend/src/test/…/admin/AdminUserServiceImplTest.java
```

**Ce faci**
- Un email schimbat se verifică să nu fie folosit de alt cont (`409`, ADM-06). Se salvează cu litere mici, la fel ca la înregistrare.
- Eliminarea pozei folosește `ImageStorageService.delete` (ADM-07).
- Un cont de admin dă `403` (ADM-08).

**Verificare**
- Editezi numele și emailul unui utilizator, iar acesta se poate autentifica cu emailul nou.
- Un email deja folosit → `409`.

---

## 1.6.5 Ștergerea unui utilizator

**Fișiere**
```
…/user/service/
~ ├── AccountService.java                # + deleteByAdmin(userId, adminId)
~ └── AccountServiceImpl.java            # aceeași anonimizare ca deleteOwnAccount, extrasă într-o metodă privată
…/admin/service/
+ └── AdminActionLogger.java             # log.info: adminId, userId, acțiunea, data (ADM-10)
~ …/admin/controller/AdminUserController.java   # + DELETE /api/admin/users/{id} → 204
~ backend/src/test/…/user/AccountServiceImplTest.java
```

**Ce faci**
- `deleteOwnAccount` și `deleteByAdmin` folosesc aceeași metodă internă de anonimizare. Astfel cleanerii, evenimentele și ștergerea fișierelor funcționează la fel (ADM-09).
- Adminul nu se poate șterge singur și nu poate șterge alți admini (`403`).

**Verificare**
- Ștergi un proprietar cu anunțuri: contul e anonimizat, anunțurile devin `DELETED`, iar pozele dispar de pe disc.
- Utilizatorul șters, care era autentificat într-un alt browser, primește `401` la următoarea cerere (ADM-11).

---

## 1.6.6 Panoul de administrare

**Fișiere**
```
frontend/src/
  ├── features/admin/layout/
+ │   └── AdminLayout.tsx                # meniu lateral: „Utilizatori”
  ├── app/admin/
+ │   └── layout.tsx                     # folosește AdminLayout
  └── proxy.ts                          ~ # /admin/** → doar ADMIN
```

**Verificare**
- Ca TENANT, `/admin` te trimite la `/`. Ca admin, vezi meniul panoului.

---

## 1.6.7 Datele și apelurile pentru admin

**Fișiere**
```
frontend/src/features/admin/users/
+ ├── types.ts
+ ├── schemas/adminUserSchemas.ts        # aceleași reguli ca la profil + email
+ ├── api/adminUsersApi.ts               # list, getById, update, removeAvatar, remove
+ └── hooks/
+     ├── useAdminUsers.ts, useAdminUser.ts
+     ├── useUpdateUser.ts, useRemoveUserAvatar.ts
+     └── useDeleteUser.ts
```

**Verificare**
- `npm run build` trece.

---

## 1.6.8 Tabelul de utilizatori

**Fișiere**
```
frontend/src/
  ├── features/admin/users/
+ │   ├── components/UsersFilters.tsx    # căutare cu debounce de 300 ms + rol; refolosește useUrlFilters (ADM-04)
+ │   ├── components/UsersTable.tsx      # avatar, nume, email, telefon, rol, dată; sortare pe coloane; „Editează” / „Șterge”
+ │   └── views/AdminUsersView.tsx
  └── app/admin/users/
+     └── page.tsx                       # cu <Suspense>
```

**Ce faci**
- După login, adminul este redirecționat pe `/admin/users` încă din etapa 1.1. Acum pagina există.

**Verificare**
- Te autentifici ca admin și ajungi direct în tabel. Căutarea, filtrul pe rol, sortarea și paginarea funcționează și se păstrează în URL.

---

## 1.6.9 Editarea din interfață

**Fișiere**
```
frontend/src/features/admin/users/components/
+ └── EditUserDialog.tsx                 # formular precompletat; eroarea 409 la câmpul email; „Elimină poza”
~ frontend/src/features/admin/users/views/AdminUsersView.tsx
```

**Verificare**
- Editezi un utilizator, iar rândul din tabel se actualizează fără reîncărcarea paginii. Un email duplicat afișează eroarea la câmp.

---

## 1.6.10 Ștergerea din interfață

**Fișiere**
```
frontend/src/features/admin/users/components/
+ └── DeleteUserDialog.tsx               # ConfirmDialog cu numele și emailul + avertismentul că acțiunea e ireversibilă
~ frontend/src/features/admin/users/views/AdminUsersView.tsx
```

**Verificare**
- După confirmare, utilizatorul dispare din tabel, iar în logul backendului apare intrarea de la ADM-10.

---

## 1.6.11 Verificarea finală a etapei și a fazei 1

**Etapa 1.6**

| Verificare | Cerință |
|---|---|
| Adminul ajunge după login pe `/admin/users`; rutele și API-ul sunt protejate | ADM-01, ADM-02 |
| Tabel cu toate coloanele; căutare, filtru pe rol, sortare, paginare în URL | ADM-03, ADM-04 |
| Fără admini și fără conturi șterse în listă | ADM-05 |
| Editare cu verificarea emailului; eliminarea pozei; rolul și parola nu se pot schimba; adminii nu pot fi editați | ADM-06…08 |
| Ștergere cu confirmare, aceeași anonimizare, log, acces pierdut imediat | ADM-09…11 |

**Faza 1, în întregime**
- Parcurgi aplicația cap-coadă, cu trei conturi (chiriaș, proprietar, admin):
  1. Proprietarul creează un anunț cu link Google Maps și poze.
  2. Chiriașul îl găsește prin filtre și pe hartă, îi vede detaliile și îl deschide în Google Maps.
  3. Chiriașul publică o cerere, iar proprietarul o găsește.
  4. Adminul editează și apoi șterge contul proprietarului. Anunțul acestuia dispare din căutare.
- Rulezi `./gradlew test`, `npm run lint` și `npm run build`.
- Un coleg care clonează repository-ul de la zero pornește proiectul doar urmând README-ul.
- Actualizezi diagramele care nu mai corespund implementării (lista lor e la finalul `specificatii-proiect.md`).
