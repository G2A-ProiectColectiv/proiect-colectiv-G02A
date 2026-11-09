# Etapa 1.3 – Anunțuri

> **Obiectiv:** proprietarii publică și gestionează anunțuri complete, cu poze și locație luată dintr-un link de Google Maps.
> **Cerințe acoperite:** ANUN-01…05, LOC-01…13, POZE-01…05, GEST-01…05
> **Depinde de:** etapa 1.1 (pentru poze, și `ImageStorageService` din etapa 1.2)
> **Urmează:** etapa 1.4 – Cereri de închiriere

**Convenții:** `+` fișier nou, `~` fișier modificat, `…/` = `backend/src/main/java/ro/rentalplatform/`.

**Sub-etape**

| # | Sub-etapă | Parte |
|---|---|---|
| 1.3.1 | Coordonatele și primul extractor | Backend |
| 1.3.2 | Restul formatelor de link | Backend |
| 1.3.3 | Linkurile scurte | Backend |
| 1.3.4 | Endpoint-ul de extragere a locației | Backend |
| 1.3.5 | Clasele comune pentru resurse cu proprietar | Backend |
| 1.3.6 | Tabelele și entitățile anunțurilor | Backend + BD |
| 1.3.7 | DTO-uri și mapper | Backend |
| 1.3.8 | Crearea, editarea și „Anunțurile mele” | Backend |
| 1.3.9 | Statusul și ștergerea anunțului | Backend |
| 1.3.10 | Pozele anunțului | Backend |
| 1.3.11 | Anunțurile la ștergerea contului | Backend |
| 1.3.12 | Harta de bază | Frontend |
| 1.3.13 | Câmpul pentru linkul Google Maps | Frontend |
| 1.3.14 | Datele și apelurile pentru anunțuri | Frontend |
| 1.3.15 | Formularul de anunț | Frontend |
| 1.3.16 | Pozele în interfață | Frontend |
| 1.3.17 | Pagina „Anunțurile mele” | Frontend |
| 1.3.18 | Verificarea finală a etapei | – |

Extragerea coordonatelor (1.3.1–1.3.4) este cod independent de restul aplicației și se poate lucra în paralel cu anunțurile (1.3.5–1.3.11).

---

## 1.3.1 Coordonatele și primul extractor

**Fișiere**
```
…/location/
  ├── domain/
+ │   └── Coordinates.java                # record(latitude, longitude); constructorul validează intervalele (LOC-08)
  └── parser/
+     ├── CoordinateExtractor.java        # interfață: Optional<Coordinates> extract(String input)
+     └── PinDataExtractor.java           # @Order(1): regex pe !3d<lat>!4d<lng>
backend/src/test/java/ro/rentalplatform/location/parser/
+ └── PinDataExtractorTest.java
```

**Ce faci**
- Strângi de la început câteva linkuri reale de Google Maps (de la un loc anume, de la un pin pus manual, din aplicația de telefon) și le folosești în teste.
- Regex-ul acceptă și valori negative și cu multe zecimale, de exemplu `!3d44.4267674!4d26.1025384`.

**Verificare**
- Testul trece pe linkurile reale și întoarce `Optional.empty()` pe un text care nu conține coordonate.

---

## 1.3.2 Restul formatelor de link

**Fișiere**
```
…/location/parser/
+ ├── QueryParamExtractor.java           # @Order(2): q=, query=, ll= de forma lat,lng
+ ├── PathCoordinatesExtractor.java      # @Order(3): /place/lat,lng și /search/lat,lng
+ ├── ViewportExtractor.java             # @Order(4): /@lat,lng,zoomz (centrul hărții)
+ ├── RawCoordinatesExtractor.java       # @Order(5): „44.4268, 26.1025” (LOC-10)
+ └── GoogleMapsLinkParser.java          # primește List<CoordinateExtractor> și îl întoarce pe primul care găsește ceva
backend/src/test/java/ro/rentalplatform/location/parser/
+ ├── QueryParamExtractorTest.java
+ ├── PathCoordinatesExtractorTest.java
+ ├── ViewportExtractorTest.java
+ ├── RawCoordinatesExtractorTest.java
+ └── GoogleMapsLinkParserTest.java
```

**Ce faci**
- Spring injectează lista de extractori deja ordonată după `@Order`. Un format nou înseamnă un extractor nou, fără să modifici parserul.
- Parametrii din URL se decodează înainte de căutare (`%2C` → `,`).

**Verificare**
- `GoogleMapsLinkParserTest` arată că, atunci când un link are atât `!3d!4d`, cât și `/@`, se aleg coordonatele pin-ului.
- Un link de traseu (`/dir/…`) sau unul care conține doar numele locului întoarce `Optional.empty()`.

---

## 1.3.3 Linkurile scurte

**Fișiere**
```
…/location/
  ├── config/
+ │   └── LocationProperties.java        # allowedHosts, maxRedirects (5), timeout (5s)
  └── resolver/
+     ├── AllowedHostsPolicy.java        # verifică domeniul (google.*, maps.google.*, maps.app.goo.gl, goo.gl) și https
+     ├── ShortLinkResolver.java         # interfață: String resolve(String shortUrl)
+     └── HttpShortLinkResolver.java     # java.net.http.HttpClient cu Redirect.NEVER, urmărire manuală
~ backend/src/main/resources/application.properties # app.location.*
~ build.gradle                                 # + com.squareup.okhttp3:mockwebserver (testImplementation)
backend/src/test/java/ro/rentalplatform/location/resolver/
+ ├── AllowedHostsPolicyTest.java
+ └── HttpShortLinkResolverTest.java
```

**Ce faci**
- Urmărirea redirecționărilor se face manual: trimiți o cerere, citești antetul `Location` și verifici domeniul la fiecare pas (LOC-07, NF-09). Corpul răspunsului nu se citește.
- Cererile au un antet `User-Agent` obișnuit, pentru ca Google să răspundă cu redirecționarea.

**Verificare**
- Testele cu MockWebServer acoperă: lanț de 2 redirecționări → URL final; un domeniu interzis în lanț → eroare; mai mult de 5 redirecționări → eroare; timeout → eroare.
- Un test manual cu un link scurt real, copiat din aplicația Google Maps, întoarce URL-ul lung.

---

## 1.3.4 Endpoint-ul de extragere a locației

**Fișiere**
```
…/location/
  ├── exception/
+ │   └── CoordinatesNotFoundException.java   # → 422, cu mesajul de la LOC-09
  ├── service/
+ │   ├── LocationService.java                # interfață: Coordinates parse(String input)
+ │   └── LocationServiceImpl.java            # dacă e link scurt → resolve; apoi GoogleMapsLinkParser
  ├── dto/
+ │   ├── ParseLocationRequest.java           # url (max 2000 caractere)
+ │   └── CoordinatesDto.java
  └── controller/
+     └── LocationController.java             # POST /api/location/parse (utilizator autentificat)
~ …/common/exception/GlobalExceptionHandler.java   # CoordinatesNotFoundException → 422
~ build.gradle                                     # + com.bucket4j:bucket4j-core
backend/src/test/java/ro/rentalplatform/location/
+ └── LocationServiceImplTest.java
```

**Ce faci**
- Limita de cereri (LOC-12): un `Bucket` per utilizator, de exemplu 20 de cereri pe minut, păstrat într-un `ConcurrentHashMap`. La depășire răspunzi cu `429`.

**Verificare**
```bash
curl -X POST localhost:8080/api/location/parse -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"url":"https://maps.app.goo.gl/…"}'
# 200 {"latitude":44.42…,"longitude":26.10…}
```
- Un link fără coordonate → `422` cu mesajul explicit.

---

## 1.3.5 Clasele comune pentru resurse cu proprietar

**Fișiere**
```
…/common/
  ├── domain/
+ │   ├── OwnedResource.java              # interfață: Long getOwnerId()
+ │   └── StatusTransitionPolicy.java     # interfață generică <S extends Enum<S>>: boolean isAllowed(S from, S to)
  └── security/
+     └── OwnershipVerifier.java          # verify(resource, currentUserId) → 403 dacă nu e proprietarul
```

**Ce faci**
- Aceste clase vor fi folosite atât de anunțuri, cât și de cereri (etapa 1.4).

**Verificare**
- Proiectul compilează.

---

## 1.3.6 Tabelele și entitățile anunțurilor

**Fișiere**
```
backend/src/main/resources/db/migration/
+ ├── V4__create_listings_table.sql
+ └── V5__create_listing_images_table.sql
…/listing/
  ├── domain/
+ │   ├── PropertyType.java        # APARTMENT, STUDIO, ROOM, HOUSE, OTHER
+ │   ├── Amenity.java             # FURNISHED, PARKING, BALCONY, AIR_CONDITIONING, WASHING_MACHINE, PETS_ALLOWED, INTERNET
+ │   ├── ListingStatus.java       # ACTIVE, RENTED, UNAVAILABLE, DELETED
+ │   ├── Listing.java             # implementează OwnedResource; @ManyToOne landlord; @ElementCollection amenities
+ │   └── ListingImage.java        # url, primary, position; @ManyToOne listing
  └── repository/
+     ├── ListingRepository.java
+     └── ListingImageRepository.java
```

**Ce faci**
- `V4` creează tabela `listings` (inclusiv `latitude`, `longitude` și `google_maps_url`, LOC-11) și tabela `listing_amenities` (`listing_id`, `amenity`). `V5` creează `listing_images`.
- Pune constrângeri `CHECK` pentru `price > 0`, `rooms >= 0`, `bathrooms >= 0` și `area_sqm > 0`.
- Pune indexuri pe `landlord_id` și pe `listing_id`.

**Verificare**
- Aplicația pornește (`ddl-auto=validate` confirmă că entitățile corespund tabelelor).

---

## 1.3.7 DTO-uri și mapper

**Fișiere**
```
…/listing/
  ├── dto/
+ │   ├── CreateListingDto.java        # toate câmpurile din ANUN-02 + googleMapsUrl, fără latitude / longitude
+ │   ├── UpdateListingDto.java
+ │   ├── ChangeListingStatusDto.java  # status: RENTED | UNAVAILABLE | ACTIVE
+ │   ├── ListingDto.java              # + coordonate, poze, status
+ │   ├── ListingSummaryDto.java       # titlu, preț, poza principală, status, dată
+ │   └── ListingImageDto.java
  └── mapper/
+     └── ListingMapper.java           # MapStruct
```

**Ce faci**
- Validările din ANUN-04 se pun ca adnotări în DTO-uri: `@Positive` pentru preț și suprafață, `@PositiveOrZero` pentru camere și băi, `@NotNull` pentru dată.
- DTO-urile de intrare nu primesc coordonate, pentru că serverul le extrage singur din link (LOC-04).

**Verificare**
- Proiectul compilează, iar MapStruct nu afișează avertismente despre câmpuri nemapate.

---

## 1.3.8 Crearea, editarea și „Anunțurile mele”

**Fișiere**
```
…/listing/
  ├── service/
+ │   ├── ListingService.java          # create, update, getOwned, listOwned(status, pageable)
+ │   └── ListingServiceImpl.java
  └── controller/
+     └── MyListingsController.java    # POST /api/listings → 201, PUT /api/listings/{id},
+                                      # GET /api/listings/mine?status=&page=, GET /api/listings/mine/{id}
backend/src/test/java/ro/rentalplatform/listing/
+ ├── ListingServiceImplTest.java
+ └── MyListingsControllerTest.java
```

**Ce faci**
- La creare și la editare, `ListingServiceImpl` apelează `LocationService.parse(googleMapsUrl)`. Dacă linkul nu conține coordonate, primești `422`.
- Toate metodele au `@PreAuthorize("hasRole('LANDLORD')")` (ANUN-01).
- `getOwned` și `update` dau `404` pentru un anunț inexistent sau `DELETED` și `403` pentru anunțul altcuiva (GEST-02), folosind `OwnershipVerifier`.

**Verificare**
- Creezi un anunț cu Postman și un link real → `201`, cu coordonatele corecte.
- Ca TENANT → `403`. Cu un preț negativ → `400`. Editarea anunțului altui proprietar → `403`.

---

## 1.3.9 Statusul și ștergerea anunțului

**Fișiere**
```
…/listing/service/
+ └── ListingStatusPolicy.java          # implementează StatusTransitionPolicy<ListingStatus>
~ …/listing/service/ListingService.java     # + changeStatus, delete
~ …/listing/service/ListingServiceImpl.java
~ …/listing/controller/MyListingsController.java   # + PATCH /api/listings/{id}/status, DELETE /api/listings/{id} → 204
~ …/common/exception/GlobalExceptionHandler.java  # tranziție invalidă (IllegalStateException) → 409
backend/src/test/java/ro/rentalplatform/listing/
+ └── ListingStatusPolicyTest.java
```

**Ce faci**
- Tranzițiile permise sunt ACTIVE ↔ RENTED și ACTIVE ↔ UNAVAILABLE. Orice altceva, de exemplu RENTED → UNAVAILABLE, dă `409` (GEST-03).
- Ștergerea doar setează statusul `DELETED`. Pozele se șterg de pe disc în sub-etapa 1.3.10.

**Verificare**
- `ListingStatusPolicyTest` acoperă toate combinațiile de statusuri.
- `GET /api/listings/mine?status=RENTED` arată anunțul după ce i-ai schimbat statusul.

---

## 1.3.10 Pozele anunțului

**Fișiere**
```
…/listing/
  ├── service/
+ │   ├── ListingImageService.java          # add, remove, setPrimary, reorder
+ │   └── ListingImageServiceImpl.java
  └── controller/
+     └── ListingImageController.java       # POST /api/listings/{id}/images (multipart, mai multe fișiere)
+                                           # DELETE /api/listings/{id}/images/{imageId}
+                                           # PATCH /api/listings/{id}/images/{imageId}/primary
+                                           # PUT /api/listings/{id}/images/order (POZE-04)
+ …/listing/event/ListingsDeletedEvent.java    # record: List<Long> listingIds
+ …/listing/event/ListingFilesCleanupListener.java  # AFTER_COMMIT: storage.deleteFolder("listings/{id}") pentru fiecare id
~ …/listing/service/ListingServiceImpl.java  # delete() publică ListingsDeletedEvent (POZE-05)
backend/src/test/java/ro/rentalplatform/listing/
+ └── ListingImageServiceImplTest.java
```

**Ce faci**
- Pozele se salvează în `listings/{idAnunț}/` prin `ImageStorageService`.
- Reguli: cel mult 10 poze (POZE-01); prima poză devine principală (POZE-02); dacă se șterge poza principală, următoarea devine principală (POZE-03); o poză ștearsă dispare și de pe disc (POZE-05).

**Verificare**
- Încarci 3 poze; prima e principală. Ștergi poza principală, iar a doua devine principală. A 11-a poză → `400`.
- Ștergi anunțul, iar folderul `uploads/listings/{id}/` dispare.

---

## 1.3.11 Anunțurile la ștergerea contului

**Fișiere**
```
…/listing/service/
+ └── ListingUserDataCleaner.java      # implementează UserDataCleaner: anunțurile proprietarului devin DELETED
```

**Ce faci**
- Cleanerul marchează anunțurile proprietarului ca `DELETED` și publică `ListingsDeletedEvent` cu id-urile lor.
- `AccountServiceImpl` nu se modifică: Spring îl găsește automat pe noul cleaner și îl adaugă în listă.

**Verificare**
- Un proprietar cu anunțuri își șterge contul, iar anunțurile lui au status `DELETED`.
- Pozele lor dispar de pe disc după commit, pentru că `ListingUserDataCleaner` publică același `ListingsDeletedEvent` ca la ștergerea unui singur anunț (STERG-04).

---

## 1.3.12 Harta de bază

**Fișiere**
```
frontend/src/shared/
  ├── map/
+ │   ├── leafletSetup.ts        # import 'leaflet/dist/leaflet.css' + reparația iconițelor implicite
+ │   ├── BaseMap.tsx            # "use client": MapContainer + TileLayer OpenStreetMap + atribuirea
+ │   └── index.ts               # export const Map = dynamic(() => import('./BaseMap'), { ssr: false })
  └── components/
+     ├── Tabs.tsx
+     └── Pagination.tsx
~ frontend/package.json          # + leaflet, react-leaflet, @types/leaflet
```

**Ce faci**
- Restul aplicației importă hărțile doar din `shared/map/index.ts`, niciodată direct din `BaseMap.tsx`, ca să nu fie randate pe server.

**Verificare**
- O pagină de test afișează harta centrată pe București, fără erori `window is not defined`. Șterge apoi pagina de test.

---

## 1.3.13 Câmpul pentru linkul Google Maps

**Fișiere**
```
frontend/src/features/location/
+ ├── types.ts
+ ├── api/locationApi.ts                 # parseLocation(url)
+ ├── hooks/useParseLocation.ts          # useMutation, cu debounce de 500 ms după lipire
+ └── components/
+     ├── GoogleMapsLinkInput.tsx        # câmp + instrucțiuni „Distribuie → Copiază linkul”; eroarea 422
+     └── LocationPreviewMap.tsx         # pin pe coordonatele extrase (LOC-03)
```

**Ce faci**
- Lângă câmp, o secțiune care se poate deschide explică pașii: deschizi Google Maps, cauți sau pui un pin pe locuință, apeși **Distribuie**, apoi **Copiază linkul** (LOC-01).
- Dacă locația e în afara României, afișezi un avertisment, dar nu blochezi formularul (LOC-13).

**Verificare**
- Lipești un link: apare pin-ul pe hartă. Lipești un link de traseu: apare mesajul de la LOC-09. Lipești coordonate simple: funcționează (LOC-10).

---

## 1.3.14 Datele și apelurile pentru anunțuri

**Fișiere**
```
frontend/src/features/listings/
+ ├── types.ts
+ ├── constants/propertyTypes.ts         # APARTMENT → „Apartament” etc.
+ ├── constants/amenities.ts             # FURNISHED → „Mobilat” etc.
+ ├── schemas/listingSchemas.ts          # Zod, aceleași reguli ca ANUN-04
+ ├── api/listingsApi.ts                 # create, update, getMine, getMineById, changeStatus, remove
+ ├── api/listingImagesApi.ts            # upload, remove, setPrimary, reorder
+ └── hooks/
+     ├── useMyListings.ts, useMyListing.ts
+     ├── useCreateListing.ts, useUpdateListing.ts
+     ├── useChangeListingStatus.ts, useDeleteListing.ts
+     └── useListingImages.ts
```

**Ce faci**
- Fiecare mutație invalidează cheile de cache afectate, de exemplu `["listings", "mine"]` și `["listings", id]`.

**Verificare**
- `npm run build` trece.

---

## 1.3.15 Formularul de anunț

**Fișiere**
```
frontend/src/
  ├── features/listings/
+ │   ├── components/ListingForm.tsx     # câmpurile ANUN-02 + GoogleMapsLinkInput + LocationPreviewMap
+ │   ├── views/CreateListingView.tsx    # după creare → pasul de poze (1.3.16)
+ │   └── views/EditListingView.tsx
  ├── app/my-listings/
+ │   ├── new/page.tsx
+ │   └── [id]/edit/page.tsx
  └── proxy.ts                          ~ # /my-listings/** → doar LANDLORD
```

**Verificare**
- Creezi un anunț din interfață, cu un link real. În baza de date apare cu coordonatele corecte.
- Un chiriaș care deschide `/my-listings/new` este trimis la `/`.

---

## 1.3.16 Pozele în interfață

**Fișiere**
```
frontend/src/features/listings/
+ ├── components/ListingImagesManager.tsx   # încărcare multiplă, previzualizare, ștergere, poză principală, ordine
~ ├── views/CreateListingView.tsx           # pasul 2: poze
~ └── views/EditListingView.tsx             # + secțiunea de poze
```

**Ce faci**
- Verificarea fișierelor în client (`validateImageFile`) și limita de 10 poze se aplică înainte de trimitere.

**Verificare**
- Încarci, ștergi, alegi poza principală și reordonezi pozele. După reîncărcarea paginii, ordinea se păstrează.

---

## 1.3.17 Pagina „Anunțurile mele”

**Fișiere**
```
frontend/src/
  ├── features/listings/
+ │   ├── components/ListingStatusBadge.tsx
+ │   ├── components/ListingStatusActions.tsx   # „Închiriat” / „Indisponibil” / „Reactivează” / „Șterge”, cu ConfirmDialog
+ │   ├── components/MyListingCard.tsx
+ │   └── views/MyListingsView.tsx              # tab-uri pe status + paginare + „Creează anunț” când lista e goală
  ├── app/my-listings/
+ │   └── page.tsx
  └── shared/layout/Navbar.tsx                  ~ # link „Anunțurile mele” pentru LANDLORD
```

**Verificare**
- Schimbi statusul unui anunț, iar acesta se mută în tab-ul corect. Ștergerea îl face să dispară.

---

## 1.3.18 Verificarea finală a etapei

| Verificare | Cerință |
|---|---|
| Doar LANDLORD poate crea; toate câmpurile; dotările în română; validări în client și server | ANUN-01…05 |
| Linkuri lungi, linkuri scurte și coordonate simple; pin de confirmare; serverul extrage din nou la salvare | LOC-01…06, LOC-10 |
| Doar domenii Google, cel mult 5 redirecționări, timeout; coordonate în intervale; 422 cu mesaj clar | LOC-07…09 |
| Linkul original e salvat; limită de cereri; avertisment în afara României | LOC-11…13 |
| Maxim 10 poze; poza principală automată; reordonare; ștergere și de pe disc | POZE-01…05 |
| Editare doar pentru propriile anunțuri; tranziții de status; confirmări; „Anunțurile mele” cu tab-uri | GEST-01…05 |
| `./gradlew test`, `npm run lint` și `npm run build` trec | – |
