# Etapa 1.4 – Cereri de închiriere

> **Obiectiv:** chiriașii publică ce caută și își gestionează cererile.
> **Cerințe acoperite:** CER-01…08
> **Depinde de:** etapa 1.1; din etapa 1.3 refolosește `LocationService`, `OwnershipVerifier`, `StatusTransitionPolicy` și harta de bază
> **Urmează:** etapa 1.5 – Căutare, detalii și hartă

**Convenții:** `+` fișier nou, `~` fișier modificat, `…/` = `backend/src/main/java/ro/rentalplatform/`. Modulul se numește `rentalrequest`, pentru a nu se confunda cu cererile HTTP.

**Sub-etape**

| # | Sub-etapă | Parte |
|---|---|---|
| 1.4.1 | Tabela și entitatea cererilor | Backend + BD |
| 1.4.2 | Validarea bugetului | Backend |
| 1.4.3 | DTO-uri și mapper | Backend |
| 1.4.4 | Crearea, editarea și „Cererile mele” | Backend |
| 1.4.5 | Statusul și ștergerea cererii | Backend |
| 1.4.6 | Cererile la ștergerea contului | Backend |
| 1.4.7 | Datele și apelurile pentru cereri | Frontend |
| 1.4.8 | Zona căutată pe hartă | Frontend |
| 1.4.9 | Formularul de cerere | Frontend |
| 1.4.10 | Pagina „Cererile mele” | Frontend |
| 1.4.11 | Verificarea finală a etapei | – |

---

## 1.4.1 Tabela și entitatea cererilor

**Fișiere**
```
backend/src/main/resources/db/migration/
+ └── V6__create_rental_requests_table.sql
…/rentalrequest/
  ├── domain/
+ │   ├── RequestStatus.java        # ACTIVE, FOUND, CLOSED, DELETED
+ │   └── RentalRequest.java        # implementează OwnedResource; @ManyToOne tenant
  └── repository/
+     └── RentalRequestRepository.java
```

**Ce faci**
- Tabela `rental_requests` are: `title`, `description`, `desired_type`, `budget_min`, `budget_max`, `currency`, `city`, `area` (nullable), `rooms` (nullable), `desired_from`, `status`, plus zona opțională: `google_maps_url`, `center_latitude`, `center_longitude`, `radius_km` (toate nullable).
- `desired_type` refolosește enumul `PropertyType` din modulul `listing`.
- Constrângeri `CHECK`: `budget_min <= budget_max`, `radius_km > 0`. Zona se completează în întregime sau deloc: coordonatele și raza sunt fie toate `NULL`, fie toate completate.

**Verificare**
- Aplicația pornește, iar tabela există.

---

## 1.4.2 Validarea bugetului

**Fișiere**
```
…/rentalrequest/validation/
+ ├── ValidBudgetRange.java          # adnotare la nivel de clasă
+ └── BudgetRangeValidator.java      # budgetMin ≤ budgetMax (CER-04)
backend/src/test/java/ro/rentalplatform/rentalrequest/
+ └── BudgetRangeValidatorTest.java
```

**Ce faci**
- Eroarea se atașează câmpului `budgetMax`, ca interfața să o poată afișa lângă el.

**Verificare**
- Testul acoperă trei cazuri: minim < maxim, minim = maxim și minim > maxim.

---

## 1.4.3 DTO-uri și mapper

**Fișiere**
```
…/rentalrequest/
  ├── dto/
+ │   ├── CreateRentalRequestDto.java      # @ValidBudgetRange; @FutureOrPresent desiredFrom; googleMapsUrl + radiusKm opționale
+ │   ├── UpdateRentalRequestDto.java
+ │   ├── ChangeRequestStatusDto.java      # FOUND | CLOSED | ACTIVE
+ │   ├── RentalRequestDto.java
+ │   └── RentalRequestSummaryDto.java     # titlu, buget, oraș, camere, perioadă, status
  └── mapper/
+     └── RentalRequestMapper.java
```

**Ce faci**
- Dacă `googleMapsUrl` este completat, `radiusKm` este obligatoriu, între 1 și 50 km, și invers.

**Verificare**
- Proiectul compilează fără avertismente de la MapStruct.

---

## 1.4.4 Crearea, editarea și „Cererile mele”

**Fișiere**
```
…/rentalrequest/
  ├── service/
+ │   ├── RentalRequestService.java        # create, update, getOwned, listOwned(status, pageable)
+ │   └── RentalRequestServiceImpl.java
  └── controller/
+     └── MyRentalRequestsController.java  # POST /api/requests → 201, PUT /api/requests/{id},
+                                          # GET /api/requests/mine?status=&page=, GET /api/requests/mine/{id}
backend/src/test/java/ro/rentalplatform/rentalrequest/
+ ├── RentalRequestServiceImplTest.java
+ └── MyRentalRequestsControllerTest.java
```

**Ce faci**
- Totul cu `@PreAuthorize("hasRole('TENANT')")` (CER-01).
- Dacă a fost trimis un link, coordonatele centrului vin din `LocationService.parse` (CER-03), la fel ca la anunțuri.
- Pentru `404` și `403` refolosești `OwnershipVerifier` (CER-05).

**Verificare**
- Ca TENANT: creezi o cerere fără zonă și una cu zonă (link + rază) → `201`.
- Ca LANDLORD → `403`. Bugetul minim mai mare decât maximul → `400` pe `budgetMax`. Data în trecut → `400`.

---

## 1.4.5 Statusul și ștergerea cererii

**Fișiere**
```
…/rentalrequest/service/
+ └── RequestStatusPolicy.java            # implementează StatusTransitionPolicy<RequestStatus>
~ …/rentalrequest/service/RentalRequestService.java       # + changeStatus, delete
~ …/rentalrequest/service/RentalRequestServiceImpl.java
~ …/rentalrequest/controller/MyRentalRequestsController.java   # + PATCH /api/requests/{id}/status, DELETE /api/requests/{id} → 204
backend/src/test/java/ro/rentalplatform/rentalrequest/
+ └── RequestStatusPolicyTest.java
```

**Ce faci**
- Tranzițiile permise sunt ACTIVE ↔ FOUND și ACTIVE ↔ CLOSED. Orice altceva dă `409` (CER-06).
- Ștergerea setează statusul `DELETED` (CER-07).

**Verificare**
- Testul acoperă toate combinațiile. FOUND → CLOSED dă `409`.

---

## 1.4.6 Cererile la ștergerea contului

**Fișiere**
```
…/rentalrequest/service/
+ └── RentalRequestUserDataCleaner.java   # cererile chiriașului devin DELETED
```

**Ce faci**
- Nu modifici niciun fișier existent: `AccountServiceImpl` primește automat noul cleaner.

**Verificare**
- Un chiriaș cu cereri își șterge contul, iar cererile lui au status `DELETED`.

---

## 1.4.7 Datele și apelurile pentru cereri

**Fișiere**
```
frontend/src/features/rentalRequests/
+ ├── types.ts
+ ├── schemas/requestSchemas.ts          # Zod: refine pentru buget și pentru link + rază
+ ├── api/rentalRequestsApi.ts           # create, update, getMine, getMineById, changeStatus, remove
+ └── hooks/
+     ├── useMyRequests.ts, useMyRequest.ts
+     ├── useCreateRequest.ts, useUpdateRequest.ts
+     └── useChangeRequestStatus.ts, useDeleteRequest.ts
```

**Ce faci**
- Etichetele tipurilor de locuință se importă din `features/listings/constants/propertyTypes.ts`. Nu le copiezi.

**Verificare**
- `npm run build` trece.

---

## 1.4.8 Zona căutată pe hartă

**Fișiere**
```
frontend/src/
  ├── shared/map/
+ │   └── RadiusCircle.tsx                     # cerc Leaflet cu centrul și raza date
~ │   └── index.ts                             # export prin next/dynamic
  └── features/rentalRequests/components/
+     └── RequestZonePicker.tsx                # GoogleMapsLinkInput + slider pentru rază (1–50 km) + hartă cu cerc
```

**Ce faci**
- `RequestZonePicker` refolosește `GoogleMapsLinkInput` din `features/location`. Nu rescrii logica de extragere.

**Verificare**
- Lipești un link și miști slider-ul, iar cercul de pe hartă își schimbă dimensiunea.

---

## 1.4.9 Formularul de cerere

**Fișiere**
```
frontend/src/
  ├── features/rentalRequests/
+ │   ├── components/RequestForm.tsx         # câmpurile CER-02 + RequestZonePicker (opțional, într-o secțiune care se deschide)
+ │   ├── views/CreateRequestView.tsx
+ │   └── views/EditRequestView.tsx
  ├── app/my-requests/
+ │   ├── new/page.tsx
+ │   └── [id]/edit/page.tsx
  └── proxy.ts                              ~ # /my-requests/** → doar TENANT
```

**Verificare**
- Creezi și editezi o cerere din interfață, cu și fără zonă. Un proprietar care deschide `/my-requests/new` este trimis la `/`.

---

## 1.4.10 Pagina „Cererile mele”

**Fișiere**
```
frontend/src/
  ├── features/rentalRequests/
+ │   ├── components/RequestStatusBadge.tsx
+ │   ├── components/RequestStatusActions.tsx   # „Am găsit” / „Închide” / „Reactivează” / „Șterge”, cu ConfirmDialog
+ │   ├── components/MyRequestCard.tsx
+ │   └── views/MyRequestsView.tsx              # tab-uri pe status + paginare (CER-08)
  ├── app/my-requests/
+ │   └── page.tsx
  └── shared/layout/Navbar.tsx                  ~ # link „Cererile mele” pentru TENANT
```

**Verificare**
- Schimbi statusul unei cereri, iar aceasta se mută în tab-ul corect.

---

## 1.4.11 Verificarea finală a etapei

| Verificare | Cerință |
|---|---|
| Doar TENANT poate crea cereri | CER-01 |
| Toate câmpurile, cu zona opțională din link + rază, desenată ca cerc | CER-02, CER-03 |
| Bugetul minim ≤ maxim; data nu e în trecut (client și server) | CER-04 |
| Editare doar pentru propriile cereri (403 / 404) | CER-05 |
| Tranziții de status; ștergere logică cu confirmare | CER-06, CER-07 |
| „Cererile mele” cu tab-uri și paginare | CER-08 |
| La ștergerea contului, cererile devin `DELETED` | STERG-02 |
| `./gradlew test`, `npm run lint` și `npm run build` trec | – |
