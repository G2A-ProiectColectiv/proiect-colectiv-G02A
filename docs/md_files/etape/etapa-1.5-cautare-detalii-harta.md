# Etapa 1.5 – Căutare, detalii și hartă

> **Obiectiv:** chiriașii și vizitatorii găsesc anunțuri, iar proprietarii găsesc cereri, cu ajutorul filtrelor și al hărții.
> **Cerințe acoperite:** CAUT-01…07, DET-01…05, HARTA-01…09
> **Depinde de:** etapele 1.3 și 1.4
> **Urmează:** etapa 1.6 – Administrarea utilizatorilor

**Convenții:** `+` fișier nou, `~` fișier modificat, `…/` = `backend/src/main/java/ro/rentalplatform/`. Citirile publice stau în controllere și servicii separate de cele care modifică date.

**Sub-etape**

| # | Sub-etapă | Parte |
|---|---|---|
| 1.5.1 | Indexuri și sortare | Backend + BD |
| 1.5.2 | Filtrele pentru anunțuri | Backend |
| 1.5.3 | Endpoint-urile publice pentru anunțuri | Backend |
| 1.5.4 | Anunțurile din zona vizibilă a hărții | Backend |
| 1.5.5 | Căutarea și detaliile cererilor | Backend |
| 1.5.6 | Linkurile către Google Maps | Frontend |
| 1.5.7 | Filtrele în URL | Frontend |
| 1.5.8 | Pagina de căutare: lista | Frontend |
| 1.5.9 | Pagina de căutare: harta | Frontend |
| 1.5.10 | Pagina de detalii a anunțului | Frontend |
| 1.5.11 | Căutarea și detaliile cererilor | Frontend |
| 1.5.12 | Verificarea finală a etapei | – |

---

## 1.5.1 Indexuri și sortare

**Fișiere**
```
backend/src/main/resources/db/migration/
+ └── V7__add_search_indexes.sql
…/common/web/
+ └── SortParser.java              # "price,asc" → Sort; acceptă doar câmpurile dintr-o listă permisă
```

**Ce faci**
- Indexurile din `V7`: `listings(status, city)`, `listings(price)`, `listings(latitude, longitude)`, `rental_requests(status, city)`, `rental_requests(budget_max)`.
- `SortParser` primește lista câmpurilor permise de la fiecare apelant. Pentru anunțuri sunt `price` și `createdAt`, iar un câmp necunoscut dă `400`. Așa nu se poate sorta după coloane interne.

**Verificare**
- Migrarea se aplică. Un test mic pentru `SortParser` acoperă un câmp valid, un câmp nepermis și o direcție invalidă.

---

## 1.5.2 Filtrele pentru anunțuri

**Fișiere**
```
…/listing/
  ├── dto/
+ │   └── ListingSearchCriteria.java       # minPrice, maxPrice, type, minRooms, minBathrooms, minArea, maxArea,
+ │                                        # city, amenities (toate trebuie să existe), availableBy
  ├── repository/
+ │   └── ListingSpecifications.java       # câte o metodă statică pentru fiecare filtru → Specification<Listing>
~ │   └── ListingRepository.java           # + extends JpaSpecificationExecutor<Listing>
  └── service/
+     ├── ListingSearchService.java        # search(criteria, pageable)
+     └── ListingSearchServiceImpl.java
backend/src/test/java/ro/rentalplatform/listing/
+ └── ListingSearchServiceImplTest.java    # test de integrare (AbstractIntegrationTest) pe date pregătite
```

**Ce faci**
- Fiecare filtru întoarce `null` dacă valoarea lipsește, iar `Specification.allOf(...)` le ignoră pe cele `null`.
- Două condiții se adaugă mereu: `status = ACTIVE` și proprietar cu `status = ACTIVE` (CAUT-05).
- Orașul se compară fără să conteze literele mari și mici.

**Verificare**
- Testul de integrare acoperă: fiecare filtru singur, filtrele combinate și faptul că anunțurile `RENTED` și `DELETED` nu apar.

---

## 1.5.3 Endpoint-urile publice pentru anunțuri

**Fișiere**
```
…/listing/
  ├── dto/
+ │   └── ListingDetailsDto.java           # tot anunțul + poze + proprietar (nume, avatar, telefon doar pentru autentificați)
  ├── service/
~ │   ├── ListingSearchService.java        # + getPublicDetails(id, viewerAuthenticated)
~ │   └── ListingSearchServiceImpl.java
  └── controller/
+     └── ListingSearchController.java     # GET /api/listings?…&sort=&page=  → PageResponse<ListingSummaryDto>
+                                          # GET /api/listings/{id}         → ListingDetailsDto
~ …/security/SecurityConfig.java            # permitAll pentru GET /api/listings și GET /api/listings/{id}
~ …/security/JwtAuthFilter.java             # pe rutele publice, un token lipsă nu e eroare
backend/src/test/java/ro/rentalplatform/listing/
+ └── ListingSearchControllerTest.java
```

**Ce faci**
- Ai grijă la ordinea regulilor din `SecurityConfig`: `GET /api/listings/mine/**` trebuie să ceară în continuare rolul LANDLORD și să fie declarată înaintea regulii publice.
- Telefonul proprietarului apare doar dacă cererea vine de la un utilizator autentificat (DET-03).
- Un anunț inexistent sau care nu e `ACTIVE` dă `404` (DET-04).

**Verificare**
- Fără token: căutarea merge, iar detaliile vin fără telefon. Cu token, telefonul apare.
- `GET /api/listings/mine` fără token dă tot `401`.

---

## 1.5.4 Anunțurile din zona vizibilă a hărții

**Fișiere**
```
…/listing/
  ├── dto/
+ │   └── ListingMapMarkerDto.java          # id, titlu, preț, poza principală, latitude, longitude
  ├── repository/
~ │   └── ListingSpecifications.java        # + inBoundingBox(south, west, north, east)
  ├── service/
~ │   ├── ListingSearchService.java         # + findForMap(criteria, bbox)
~ │   └── ListingSearchServiceImpl.java     # limită de 500 de markere
  └── controller/
~     └── ListingSearchController.java      # + GET /api/listings/map?bbox=south,west,north,east&…filtre
```

**Ce faci**
- Endpoint-ul acceptă aceleași filtre ca lista (HARTA-03) și întoarce DTO-uri mici, fără paginare, dar cu o limită de 500 de markere (HARTA-04).

**Verificare**
- Un bbox care cuprinde Bucureștiul întoarce doar anunțurile din București.

---

## 1.5.5 Căutarea și detaliile cererilor

**Fișiere**
```
…/rentalrequest/
  ├── dto/
+ │   ├── RentalRequestSearchCriteria.java       # type, minBudget, maxBudget, city, area, minRooms, desiredFrom
+ │   └── RentalRequestDetailsDto.java           # + chiriaș (nume, avatar, telefon), zona (centru + rază)
  ├── repository/
+ │   └── RentalRequestSpecifications.java
~ │   └── RentalRequestRepository.java           # + JpaSpecificationExecutor
  ├── service/
+ │   ├── RentalRequestSearchService.java
+ │   └── RentalRequestSearchServiceImpl.java    # doar ACTIVE, doar chiriași activi
  └── controller/
+     └── RentalRequestSearchController.java     # GET /api/requests, GET /api/requests/{id} — doar LANDLORD
backend/src/test/java/ro/rentalplatform/rentalrequest/
+ └── RentalRequestSearchServiceImplTest.java
```

**Ce faci**
- Filtrul pe buget găsește cererile compatibile cu un preț: o cerere apare dacă intervalul ei de buget se suprapune cu intervalul căutat.
- La fel ca la anunțuri, `GET /api/requests/mine/**` rămâne doar pentru TENANT.

**Verificare**
- Ca LANDLORD: lista și detaliile funcționează. Ca TENANT sau fără token → `403` / `401`.

---

## 1.5.6 Linkurile către Google Maps

**Fișiere**
```
frontend/src/
  ├── shared/map/
+ │   ├── googleMapsUrls.ts                  # searchUrl(lat, lng), directionsUrl(lat, lng)
+ │   ├── OpenInGoogleMapsButton.tsx         # <a target="_blank" rel="noopener noreferrer">
+ │   └── DirectionsButton.tsx
  └── features/location/components/
~     └── LocationPreviewMap.tsx             # + OpenInGoogleMapsButton sub hartă
```

**Ce faci**
- Formatele linkurilor sunt cele oficiale, fără cheie de API (HARTA-07, HARTA-09):
  ```ts
  `https://www.google.com/maps/search/?api=1&query=${lat},${lng}`
  `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}`
  ```

**Verificare**
- Butonul de pe harta din formularul de anunț deschide locația corectă într-un tab nou. Pe telefon se deschide în aplicația Google Maps (HARTA-08).

---

## 1.5.7 Filtrele în URL

**Fișiere**
```
frontend/src/features/search/
+ ├── hooks/useUrlFilters.ts       # citește și scrie filtrele în URL (useSearchParams + router.replace)
+ └── components/
+     ├── SortSelect.tsx
+     ├── ViewToggle.tsx           # „Listă” / „Hartă”, păstrat tot în URL (?view=map)
+     └── ResultsCount.tsx
```

**Ce faci**
- Hook-ul primește o schemă Zod a filtrelor: valorile invalide din URL se ignoră, iar orice schimbare de filtru resetează pagina la 1.
- Componentele care folosesc `useSearchParams` trebuie învelite în `<Suspense>` în `page.tsx`, altfel `next build` dă eroare.

**Verificare**
- Testul unitar al hook-ului (Vitest) arată că filtrele ajung corect în URL și înapoi.

---

## 1.5.8 Pagina de căutare: lista

**Fișiere**
```
frontend/src/
  ├── features/listings/
+ │   ├── hooks/useListingSearch.ts           # useQuery cu cheia ["listings", "search", filtre]
+ │   ├── components/ListingFilters.tsx       # toate filtrele din CAUT-01
+ │   ├── components/ListingCard.tsx          # titlu, preț, camere, poza principală (CAUT-06)
+ │   ├── components/ListingResultsList.tsx   # listă + paginare + EmptyState (CAUT-07)
+ │   └── views/ListingSearchView.tsx
  ├── app/listings/
+ │   └── page.tsx                            # public, cu <Suspense>
  └── shared/layout/Navbar.tsx                ~ # link „Caută locuințe”
```

**Verificare**
- Ca vizitator: filtrezi, sortezi, schimbi pagina. Copiezi URL-ul într-un tab nou și obții aceleași rezultate (CAUT-04).
- Filtrele fără rezultate afișează mesajul explicit.

---

## 1.5.9 Pagina de căutare: harta

**Fișiere**
```
frontend/src/
  ├── shared/map/
+ │   └── MarkerClusterLayer.tsx               # gruparea markerelor (HARTA-05)
~ │   └── index.ts
  └── features/listings/
+     ├── hooks/useListingsInBounds.ts         # bbox din harta curentă + filtre; debounce la mutarea hărții
+     ├── components/ListingResultsMap.tsx     # markere; popup: rezumat, link spre anunț, OpenInGoogleMapsButton
~     └── views/ListingSearchView.tsx          # ViewToggle între listă și hartă
~ frontend/package.json                        # + react-leaflet-cluster
```

**Verificare**
- Muți harta, iar markerele se reîncarcă pentru zona vizibilă. Schimbi un filtru, iar markerele se actualizează (HARTA-02…05).
- Popup-ul fiecărui marker are linkul „Deschide în Google Maps” (HARTA-06).

---

## 1.5.10 Pagina de detalii a anunțului

**Fișiere**
```
frontend/src/
  ├── features/listings/
+ │   ├── hooks/useListing.ts
+ │   ├── components/ListingGallery.tsx       # poza principală mare + miniaturi
+ │   ├── components/ListingDetails.tsx       # descriere, preț, camere, băi, suprafață, dotări, disponibilitate
+ │   ├── components/OwnerContactCard.tsx     # nume + avatar; telefon sau „Autentifică-te pentru a vedea telefonul”
+ │   └── views/ListingDetailsView.tsx        # + hartă cu pin, OpenInGoogleMapsButton, DirectionsButton
  ├── app/listings/[id]/
+ │   └── page.tsx                            # public
~ └── features/listings/components/ListingCard.tsx   # link spre /listings/{id}
```

**Ce faci**
- Opțional, pentru motoarele de căutare: `page.tsx` poate încărca anunțul pe server și poate genera titlul paginii cu `generateMetadata`. Componentele interactive rămân pe client.
- `404` de la backend → `notFound()`, care afișează „Anunțul nu mai este disponibil” (DET-04).

**Verificare**
- Ca vizitator nu vezi telefonul, iar autentificat îl vezi (DET-03).
- Butoanele „Deschide în Google Maps” și „Direcții” deschid locația corectă (DET-02, HARTA-06…09).

---

## 1.5.11 Căutarea și detaliile cererilor

**Fișiere**
```
frontend/src/
  ├── features/rentalRequests/
+ │   ├── hooks/useRequestSearch.ts, useRequest.ts
+ │   ├── components/RequestFilters.tsx
+ │   ├── components/RequestCard.tsx          # titlu, buget, oraș, camere, perioadă, nume și poza chiriașului
+ │   ├── components/RequestDetails.tsx       # + zona pe hartă (RadiusCircle) + OpenInGoogleMapsButton
+ │   ├── views/RequestSearchView.tsx
+ │   └── views/RequestDetailsView.tsx
  ├── app/requests/
+ │   ├── page.tsx
+ │   └── [id]/page.tsx
  ├── proxy.ts                               ~ # /requests/** → doar LANDLORD
  └── shared/layout/Navbar.tsx               ~ # link „Cereri de la chiriași” pentru LANDLORD
```

**Verificare**
- Ca LANDLORD: filtrezi cererile și deschizi detaliile. Zona apare ca cerc pe hartă (DET-05).
- Ca TENANT, `/requests` te trimite la `/`.

---

## 1.5.12 Verificarea finală a etapei

| Verificare | Cerință |
|---|---|
| Căutare publică cu toate filtrele; căutarea cererilor pentru LANDLORD | CAUT-01, CAUT-02 |
| Sortare și paginare pe server; filtrele în URL | CAUT-03, CAUT-04 |
| Doar anunțuri și cereri `ACTIVE` ale utilizatorilor activi | CAUT-05 |
| Cardurile arată informațiile cerute; mesaj când nu există rezultate | CAUT-06, CAUT-07 |
| Detalii anunț: galerie, dotări, hartă, proprietar; telefon doar pentru autentificați; 404 | DET-01…04 |
| Detalii cerere pentru proprietar, cu zona pe hartă | DET-05 |
| Leaflet + OpenStreetMap; vedere pe hartă cu aceleași filtre; bbox; grupare | HARTA-01…05 |
| „Deschide în Google Maps” peste tot unde apare o locație; tab nou / aplicație; „Direcții” | HARTA-06…09 |
| `./gradlew test`, `npm run lint` și `npm run build` trec | – |
