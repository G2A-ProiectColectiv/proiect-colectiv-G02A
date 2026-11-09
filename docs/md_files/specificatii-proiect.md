# Platforma de anunțuri imobiliare – Specificații și cerințe (Faza 1)

> Proiect colectiv · Funcționalitățile din faza 1, împărțite în șase etape de dezvoltare

---

## 1. Despre proiect

Platforma este o aplicație web prin care proprietarii pot publica spațiile pe care le au de închiriat, iar persoanele care caută o locuință pot publica, la rândul lor, ce își doresc. Cele două părți se pot găsi cu ajutorul filtrelor și al hărții.

### 1.1 Scopul aplicației

Scopul aplicației este ca atât căutarea, cât și relația dintre proprietar și chiriaș să fie mai transparente. Detaliile unui anunț (preț, dotări, poze, locația pe hartă, disponibilitate etc.) sunt vizibile din start. Astfel se reduc anunțurile incomplete sau vechi, iar potrivirea dintre ce se oferă și ce se caută devine mai rapidă.

### 1.2 Pentru cine este

| Rol | Descriere |
|---|---|
| **Vizitator** | Utilizator neautentificat. Poate căuta și vedea anunțuri, dar fără datele de contact ale proprietarului. Își poate crea cont sau se poate autentifica. |
| **Chiriaș** (`TENANT`) | Oricine caută o locuință, de la studenți la angajați sau familii. Caută anunțuri sau își publică propria cerere (buget, zonă, perioadă). |
| **Proprietar** (`LANDLORD`) | Persoane fizice sau firme mici care dau în chirie apartamente, garsoniere, camere sau case și vor să ajungă mai ușor la chiriași. |
| **Administrator** (`ADMIN`) | Gestionează conturile utilizatorilor din panoul de administrare: le vede, le editează și le șterge. |

### 1.3 Funcționalitățile din faza 1

- **Conturi**: înregistrare (ca chiriaș sau proprietar), autentificare, resetarea parolei și editarea profilului.
- **Anunțuri**: proprietarii publică și gestionează anunțuri cu poze și le pot marca drept închiriate sau indisponibile.
- **Cereri**: chiriașii publică ce caută (tip, buget, zonă, perioadă) și își pot închide cererea după ce au găsit o locuință.
- **Căutare și filtrare**: după preț, tip, camere, băi, suprafață, oraș, dotări și disponibilitate, plus sortare, pentru ambele părți.
- **Hartă**: locația fiecărui anunț și o vedere de ansamblu a anunțurilor, în funcție de filtre. Locația se obține dintr-un link de Google Maps adăugat de utilizator.
- **Administrare**: adminul vede, editează și șterge utilizatori.

### 1.4 Împărțirea pe etape

| Etapă | Nume | Conținut | Depinde de |
|---|---|---|---|
| **1.1** | Fundația și autentificarea | Structura proiectului, baza de date, înregistrare, login, roluri | – |
| **1.2** | Contul utilizatorului | Profil, poză de profil, resetarea parolei, ștergerea contului | 1.1 |
| **1.3** | Anunțuri | Creare, editare, poze, locație din Google Maps, statusuri, „Anunțurile mele” | 1.1 |
| **1.4** | Cereri de închiriere | Creare, editare, zonă căutată, statusuri, „Cererile mele” | 1.1, 1.3 (locația) |
| **1.5** | Căutare, detalii și hartă | Filtre, sortare, paginile de detalii, harta cu rezultatele | 1.3, 1.4 |
| **1.6** | Administrarea utilizatorilor | Panoul de admin: listă, editare, ștergere | 1.2 |

Etapele 1.2 și 1.3 se pot lucra în paralel, iar etapa 1.6 poate începe imediat după 1.2.

**Convenția de numerotare.** Cerințele au forma `<MODUL>-<nr>`, de exemplu `ANUN-07`. Prioritatea este **M** (Must, obligatoriu) sau **S** (Should, de dorit).

---

## 2. Etapa 1.1 – Fundația și autentificarea

**Obiectiv:** utilizatorii își pot face cont și se pot autentifica, iar aplicația știe ce rol are fiecare.

### 2.1 Infrastructură (`INFR`)

| ID | Cerință | Prioritate |
|---|---|---|
| INFR-01 | Proiectul are frontendul în Next.js și backendul în Spring Boot (Gradle), în structura stabilită de echipă, cu un README de pornire. | M |
| INFR-02 | Baza de date este PostgreSQL, iar schema este gestionată prin migrări. | M |
| INFR-03 | Contul de admin se creează printr-o migrare de tip seed, cu datele luate din variabile de mediu. | M |
| INFR-04 | API-ul are un format unic pentru erori, cu codul HTTP, un mesaj și lista erorilor pe câmpuri. | M |
| INFR-05 | Configurația diferă pe medii (dezvoltare și producție) și nu conține parole sau chei în cod. | M |

### 2.2 Conturi și autentificare (`AUTH`)

| ID | Cerință | Prioritate |
|---|---|---|
| AUTH-01 | Vizitatorul își poate crea un cont cu email, parolă, prenume, nume și rol (**Chiriaș** sau **Proprietar**). | M |
| AUTH-02 | Datele de înregistrare se validează atât în client (React Hook Form + Zod), cât și în backend (Bean Validation). Dacă nu sunt valide, backendul răspunde cu `400` și lista erorilor pe câmpuri. | M |
| AUTH-03 | Emailul trebuie să fie unic. Dacă există deja un cont cu acel email, backendul răspunde cu `409`, iar interfața afișează „Există deja un cont cu acest email”. | M |
| AUTH-04 | Rolul `ADMIN` nu poate fi ales la înregistrare. Backendul refuză cererea. | M |
| AUTH-05 | Parola se salvează doar ca hash BCrypt și nu este returnată niciodată de API. | M |
| AUTH-06 | Autentificarea se face cu email și parolă și întoarce un token JWT semnat, cu expirare, care conține id-ul, emailul și rolul utilizatorului. | M |
| AUTH-07 | La credențiale greșite se afișează un mesaj generic („Email sau parolă incorecte”, `401`), fără să se precizeze dacă emailul există. | M |
| AUTH-08 | După login, utilizatorul este redirecționat în funcție de rol: Chiriaș, Proprietar sau Admin (`/admin/users`). | M |
| AUTH-09 | Deconectarea este locală: interfața șterge token-ul și starea de autentificare. | M |
| AUTH-10 | Endpoint-urile protejate verifică token-ul și rolul (`@PreAuthorize`). Fără token sau cu token invalid răspund cu `401`, iar cu rol insuficient, cu `403`. | M |
| AUTH-11 | Rutele din interfață sunt protejate după rol. Un utilizator fără drepturi este redirecționat la login sau la pagina principală. | M |

**Rezultat la final:** te poți înregistra, autentifica și deconecta. Fiecare rol ajunge pe pagina lui, iar contul de admin există din seed.

---

## 3. Etapa 1.2 – Contul utilizatorului

**Obiectiv:** utilizatorul își administrează singur contul.

### 3.1 Profil (`PROF`)

| ID | Cerință | Prioritate |
|---|---|---|
| PROF-01 | Utilizatorul autentificat își vede profilul și poate modifica prenumele, numele, telefonul și descrierea. Emailul și rolul nu se pot schimba din profil. | M |
| PROF-02 | Utilizatorul poate încărca o poză de profil în format jpg, png sau webp, de maximum 5 MB. Poza veche se șterge de pe disc. | M |
| PROF-03 | Pe server, fișierele încărcate sunt verificate după conținutul real (nu doar după extensie), redimensionate și salvate cu un nume unic. | M |
| PROF-04 | Pozele se salvează **doar pe disc local**, în folderul `uploads/` din repository, a cărui cale se configurează în `application.properties`. Folderul face parte din proiect și este versionat în Git. | M |
| PROF-05 | Folderul de upload are o structură fixă: `avatars/` pentru pozele de profil și `listings/{idAnunț}/` pentru pozele anunțurilor. Subfolderele goale sunt păstrate în Git cu un fișier `.gitkeep`. | M |
| PROF-06 | În baza de date se salvează doar calea relativă a fișierului (de exemplu `listings/12/a1b2c3.webp`), nu calea absolută și nici fișierul propriu-zis. | M |
| PROF-07 | Backendul servește pozele ca fișiere statice la adresa `/uploads/**`. Calea cerută este verificată, astfel încât nu se pot citi fișiere din afara folderului de upload (de exemplu cu `../`). | M |
| PROF-08 | Logica de salvare și ștergere a fișierelor este într-un singur serviciu, `ImageStorageService`. | M |

### 3.2 Resetarea parolei (`PAROLA`)

| ID | Cerință | Prioritate |
|---|---|---|
| PAROLA-01 | Utilizatorul poate cere resetarea parolei pe baza emailului și primește pe email un link de resetare. | M |
| PAROLA-02 | Linkul conține un token unic, cu expirare (de exemplu, 1 oră) și de unică folosință. | M |
| PAROLA-03 | Prin link, utilizatorul setează o parolă nouă, care trece prin aceleași reguli de validare ca la înregistrare. | M |
| PAROLA-04 | Răspunsul la cererea de resetare este același indiferent dacă emailul există sau nu, ca să nu se poată afla ce conturi sunt înregistrate. | M |
| PAROLA-05 | O nouă cerere de resetare invalidează tokenurile emise anterior pentru același cont. | S |

### 3.3 Ștergerea contului (`STERG`)

| ID | Cerință | Prioritate |
|---|---|---|
| STERG-01 | Utilizatorul își poate șterge contul după ce își confirmă parola. Dacă parola e greșită, primește `403`. | M |
| STERG-02 | Ștergerea se face prin **anonimizare**, într-o singură tranzacție: anunțurile și cererile trec în status `DELETED`, tokenurile de resetare se șterg, iar datele personale se înlocuiesc (email `deleted_{id}@removed.local`, nume „Utilizator șters”, telefon, poză și descriere șterse, parolă aleatoare). Contul primește status `DELETED`. | M |
| STERG-03 | După ștergere, utilizatorul este deconectat. Orice cerere făcută cu un token vechi primește `401`. | M |
| STERG-04 | Fișierele utilizatorului (avatarul și pozele din anunțuri) se șterg de pe disc după ce tranzacția s-a încheiat cu succes. | S |

**Rezultat la final:** utilizatorul își editează profilul și poza, își poate reseta parola și își poate șterge contul.

---

## 4. Etapa 1.3 – Anunțuri

**Obiectiv:** proprietarii publică și gestionează anunțuri complete, cu poze și locație.

### 4.1 Datele anunțului (`ANUN`)

| ID | Cerință | Prioritate |
|---|---|---|
| ANUN-01 | Doar proprietarul poate crea anunțuri (`hasRole('LANDLORD')`). Un utilizator cu alt rol primește `403`. | M |
| ANUN-02 | Un anunț are: titlu, descriere, tip (`APARTMENT`, `STUDIO`, `ROOM`, `HOUSE`, `OTHER`), preț și monedă, număr de camere și de băi, suprafață (m²), adresă, oraș, coordonate (latitudine și longitudine), dotări și data de la care este disponibil. | M |
| ANUN-03 | Dotările se aleg dintr-o listă fixă: mobilat, parcare, balcon, aer condiționat, mașină de spălat, animale permise, internet. | M |
| ANUN-04 | Validări: preț > 0, camere ≥ 0, băi ≥ 0, suprafață > 0 și o dată validă. Se aplică atât în client, cât și în backend. | M |
| ANUN-05 | Adresa și orașul se completează manual de proprietar. Coordonatele se obțin din linkul de Google Maps (secțiunea 4.2). | M |

### 4.2 Locația din linkul Google Maps (`LOC`)

Utilizatorul caută locuința în Google Maps, apasă **Distribuie → Copiază linkul** și lipește linkul într-un câmp din aplicație. Serverul extrage latitudinea și longitudinea din link, fără nicio cheie de API și fără serviciu de geocodare.

| ID | Cerință | Prioritate |
|---|---|---|
| LOC-01 | Formularul de anunț are câmpul obligatoriu „Link Google Maps”, însoțit de instrucțiuni scurte despre cum se obține linkul. | M |
| LOC-02 | Extragerea coordonatelor se face pe server. Endpoint-ul `POST /api/location/parse` primește `{url}` și întoarce `{latitude, longitude}`, pentru previzualizare. | M |
| LOC-03 | După ce linkul este lipit, interfața afișează un pin pe hartă la coordonatele extrase, pentru ca utilizatorul să confirme că locația e corectă. | M |
| LOC-04 | La salvarea anunțului, serverul extrage din nou coordonatele din link și nu se bazează pe valorile trimise de client. | M |
| LOC-05 | Formatele acceptate de linkuri lungi sunt, în ordinea priorității: <br>1. `…!3d<lat>!4d<lng>…`, care indică pin-ul exact al locului; <br>2. parametrii `q=`, `query=`, `ll=`, dacă au forma `lat,lng`; <br>3. `/place/<lat>,<lng>` sau `/search/<lat>,<lng>`; <br>4. `/@<lat>,<lng>,<zoom>z`, care indică centrul hărții și e folosit doar dacă nu există alt format. | M |
| LOC-06 | Linkurile scurte (`maps.app.goo.gl/…`, `goo.gl/maps/…`) sunt urmărite de server până la URL-ul complet, din care se extrag apoi coordonatele. Serverul citește doar adresa de redirecționare și nu descarcă conținutul paginii. | M |
| LOC-07 | Protecție la urmărirea linkurilor scurte: sunt acceptate doar domeniile Google Maps (`google.com`, `google.ro` și alte domenii `google.*`, `maps.google.*`, `maps.app.goo.gl`, `goo.gl`), verificate la fiecare redirecționare. Se urmează cel mult 5 redirecționări, cu un timeout de 5 secunde. | M |
| LOC-08 | Coordonatele extrase se validează: latitudinea trebuie să fie între -90 și 90, iar longitudinea între -180 și 180. | M |
| LOC-09 | Dacă din link nu se pot extrage coordonate (de exemplu, un link de traseu sau un link care conține doar numele locului), serverul răspunde cu `422` și un mesaj explicit: „Nu am putut găsi coordonatele în acest link. Pune un pin în Google Maps pe locuință și copiază linkul din nou.” | M |
| LOC-10 | Ca alternativă, câmpul acceptă și coordonate scrise direct (de exemplu `44.4268, 26.1025`), copiate din Google Maps cu click dreapta pe hartă. | S |
| LOC-11 | Se salvează atât coordonatele, cât și linkul original, pentru ca proprietarul să-l vadă la editare. | S |
| LOC-12 | Endpoint-ul de extragere are o limită de cereri pe utilizator, ca să nu poată fi folosit abuziv. | S |
| LOC-13 | Dacă locația iese din limitele României, interfața afișează un avertisment, dar permite salvarea. | S |

> **De reținut:** linkurile lungi conțin aproape întotdeauna coordonatele, deci extragerea lor e sigură. Linkurile scurte depind de redirecționările făcute de Google. Dacă Google își schimbă formatul, extragerea poate eșua, iar în acest caz utilizatorul primește mesajul de la LOC-09 și poate folosi varianta LOC-10.

### 4.3 Poze (`POZE`)

| ID | Cerință | Prioritate |
|---|---|---|
| POZE-01 | Un anunț poate avea cel mult 10 poze, în format jpg, png sau webp, de maximum 5 MB fiecare. Se aplică aceleași verificări ca la PROF-03. | M |
| POZE-02 | Prima poză încărcată devine automat principală. Proprietarul poate alege oricând altă poză principală. | M |
| POZE-03 | Dacă poza principală este ștearsă, prima poză rămasă devine principală. | M |
| POZE-05 | O poză ștearsă din anunț se șterge și de pe disc. La ștergerea unui anunț, folderul `listings/{idAnunț}/` se șterge. | M |
| POZE-04 | Pozele au o ordine (`position`), iar proprietarul le poate reordona. | S |

### 4.4 Gestionarea anunțurilor (`GEST`)

| ID | Cerință | Prioritate |
|---|---|---|
| GEST-01 | Proprietarul își poate edita anunțurile: datele, locația (cu un link nou) și pozele. | M |
| GEST-02 | Un proprietar poate modifica doar propriile anunțuri. Pentru anunțul altcuiva primește `403`, iar pentru unul inexistent sau șters, `404`. | M |
| GEST-03 | Statusul unui anunț poate fi `ACTIVE`, `RENTED` (închiriat), `UNAVAILABLE` (indisponibil) sau `DELETED`. Tranzițiile permise sunt ACTIVE ↔ RENTED și ACTIVE ↔ UNAVAILABLE. Orice altă tranziție primește `409`. | M |
| GEST-04 | Schimbarea statusului și ștergerea cer o confirmare. Ștergerea este logică: anunțul trece în status `DELETED`. | M |
| GEST-05 | Pagina „Anunțurile mele” are tab-uri pe status (Active, Închiriate, Indisponibile) și paginare. Fiecare anunț are acțiunile editează, schimbă status și șterge. | M |

**Rezultat la final:** un proprietar poate publica un anunț cu poze și cu locația luată dintr-un link de Google Maps, apoi îl poate edita, îl poate marca închiriat sau indisponibil și îl poate șterge.

---

## 5. Etapa 1.4 – Cereri de închiriere

**Obiectiv:** chiriașii publică ce caută.

| ID | Cerință | Prioritate |
|---|---|---|
| CER-01 | Doar chiriașul poate crea cereri (`hasRole('TENANT')`). | M |
| CER-02 | O cerere are: titlu, descriere, tipul de locuință dorit, buget minim și maxim cu monedă, oraș, zonă (text, opțional), număr de camere (opțional) și data de la care caută. | M |
| CER-03 | Opțional, chiriașul poate indica centrul zonei căutate printr-un link de Google Maps, cu aceleași reguli ca la LOC-02…LOC-10, și o rază în km aleasă cu un slider. Zona se afișează pe hartă ca un cerc. | S |
| CER-04 | Validări: bugetul minim trebuie să fie cel mult egal cu bugetul maxim, iar data nu poate fi în trecut. | M |
| CER-05 | Chiriașul își poate edita cererile. Cererea altcuiva dă `403`, iar una inexistentă sau ștearsă dă `404`. | M |
| CER-06 | Statusul unei cereri poate fi `ACTIVE`, `FOUND` (găsită), `CLOSED` (închisă) sau `DELETED`. Tranzițiile permise sunt ACTIVE ↔ FOUND și ACTIVE ↔ CLOSED. Orice altă tranziție primește `409`. | M |
| CER-07 | Ștergerea unei cereri cere confirmare și este logică: cererea trece în status `DELETED`. | M |
| CER-08 | Pagina „Cererile mele” are tab-uri pe status și paginare. | M |

**Rezultat la final:** un chiriaș poate publica, edita, închide, reactiva și șterge cereri.

---

## 6. Etapa 1.5 – Căutare, detalii și hartă

**Obiectiv:** cele două părți se găsesc una pe alta.

### 6.1 Căutare și filtrare (`CAUT`)

| ID | Cerință | Prioritate |
|---|---|---|
| CAUT-01 | Oricine, inclusiv vizitatorul, poate căuta anunțuri. Filtrele disponibile sunt: preț, tip, camere, băi, suprafață, oraș, dotări și disponibilitate. | M |
| CAUT-02 | Proprietarul poate căuta cereri de la chiriași, filtrând după tip, buget, oraș sau zonă, camere și perioadă. | M |
| CAUT-03 | Rezultatele pot fi sortate (după preț sau după dată, crescător sau descrescător) și sunt paginate pe server. | M |
| CAUT-04 | Filtrele active se păstrează în URL, astfel încât o căutare poate fi trimisă cuiva sau reîncărcată. | S |
| CAUT-05 | În rezultate apar doar anunțurile `ACTIVE` ale proprietarilor activi și cererile `ACTIVE` ale chiriașilor activi. | M |
| CAUT-06 | Un anunț din listă arată titlul, prețul, numărul de camere și poza principală. O cerere din listă arată titlul, bugetul, orașul, numărul de camere, perioada, numele și poza chiriașului. | M |
| CAUT-07 | Când niciun rezultat nu corespunde filtrelor, interfața afișează un mesaj explicit. | M |

### 6.2 Pagini de detalii (`DET`)

| ID | Cerință | Prioritate |
|---|---|---|
| DET-01 | Pagina unui anunț arată descrierea, galeria de poze, dotările, harta cu pin-ul și datele proprietarului. | M |
| DET-02 | Harta din pagina unui anunț are butonul „Deschide în Google Maps” (vezi HARTA-06). | M |
| DET-03 | Vizitatorul nu vede datele de contact ale proprietarului. Utilizatorul autentificat vede și telefonul, dacă proprietarul l-a completat. | M |
| DET-04 | Un anunț inexistent sau șters întoarce `404` și o pagină „Anunțul nu mai este disponibil”. | M |
| DET-05 | Proprietarul poate vedea detaliile unei cereri: preferințele, bugetul, perioada, zona pe hartă (dacă a fost aleasă) și datele chiriașului. | M |

### 6.3 Hartă (`HARTA`)

| ID | Cerință | Prioritate |
|---|---|---|
| HARTA-01 | Hărțile din aplicație se afișează cu Leaflet și tile-uri OpenStreetMap. Harta servește doar la afișare, fără căutare de adrese. | M |
| HARTA-02 | Pagina de căutare are și o vedere pe hartă, cu un marker pentru fiecare anunț care corespunde filtrelor. Click pe un marker afișează un rezumat, un link spre anunț și linkul „Deschide în Google Maps”. | M |
| HARTA-03 | Când filtrele se schimbă, lista și markerii se actualizează împreună. | M |
| HARTA-04 | Pe hartă se încarcă doar anunțurile din zona vizibilă (bounding box pe latitudine și longitudine). | S |
| HARTA-05 | Când există multe anunțuri apropiate, markerii se grupează (clustering). | S |
| HARTA-06 | Fiecare locație afișată pe hartă (pin-ul unui anunț, markerul din căutare, centrul zonei unei cereri) se poate deschide în Google Maps printr-un buton sau link „Deschide în Google Maps”. | M |
| HARTA-07 | Linkul se construiește din coordonatele salvate, după formatul oficial Google Maps, care nu cere cheie de API: `https://www.google.com/maps/search/?api=1&query=<lat>,<lng>`. | M |
| HARTA-08 | Linkul se deschide într-un tab nou. Pe telefon, dacă aplicația Google Maps e instalată, linkul se deschide direct în aplicație. | M |
| HARTA-09 | Butonul „Direcții” deschide Google Maps cu un traseu până la locație: `https://www.google.com/maps/dir/?api=1&destination=<lat>,<lng>`. | S |

**Rezultat la final:** vizitatorii și chiriașii găsesc anunțuri cu ajutorul filtrelor și al hărții, iar proprietarii găsesc cereri.

---

## 7. Etapa 1.6 – Administrarea utilizatorilor

**Obiectiv:** adminul are control asupra conturilor.

| ID | Cerință | Prioritate |
|---|---|---|
| ADM-01 | Adminul se autentifică prin același login și este redirecționat către panoul `/admin/users`. | M |
| ADM-02 | Rutele `/admin/**` sunt protejate după rolul `ADMIN`, iar endpoint-urile `/api/admin/**` cer `hasRole('ADMIN')`. | M |
| ADM-03 | Pagina „Utilizatori” afișează un tabel cu numele, emailul, telefonul, rolul, poza și data înregistrării fiecărui utilizator. | M |
| ADM-04 | Tabelul are căutare după nume sau email (cu debounce), filtru pe rol (Chiriaș sau Proprietar), sortare și paginare. Parametrii se păstrează în URL. | M |
| ADM-05 | Lista nu include conturile de admin și conturile șterse. | M |
| ADM-06 | Adminul poate edita prenumele, numele, emailul, telefonul și descrierea unui utilizator. Dacă emailul nou este deja folosit, primește `409`. | M |
| ADM-07 | Adminul poate elimina poza de profil a unui utilizator, de exemplu dacă e nepotrivită. | S |
| ADM-08 | Adminul nu poate schimba rolul sau parola unui utilizator și nu poate edita sau șterge alte conturi de admin (`403`). | M |
| ADM-09 | Adminul poate șterge un utilizator, după o confirmare care arată numele și emailul acestuia. Se aplică aceeași anonimizare ca la STERG-02, fără confirmarea parolei. | M |
| ADM-10 | Fiecare ștergere făcută de admin se înregistrează în log, cu id-ul adminului, id-ul utilizatorului și data. | S |
| ADM-11 | Un utilizator șters își pierde imediat accesul: la orice cerere cu un token vechi primește `401`. | M |

**Rezultat la final:** adminul vede toți utilizatorii și îi poate căuta, edita și șterge.

---

## 8. Cerințe nefuncționale (toate etapele)

### 8.1 Tehnologii

| ID | Cerință |
|---|---|
| NF-01 | **Frontend**: Next.js (App Router) cu TypeScript, TanStack Query pentru date și cache, Axios pentru apeluri HTTP, React Hook Form și Zod pentru formulare, Leaflet pentru hartă. |
| NF-02 | **Backend**: Spring Boot, cu Spring Security (JWT), Spring Data JPA și Bean Validation. |
| NF-03 | **Bază de date**: PostgreSQL, cu schema gestionată prin migrări. |
| NF-04 | **Fișiere**: pozele se stochează doar pe disc local, în folderul de upload configurat (PROF-04). Nu se folosește S3 sau alt serviciu de stocare în cloud. Folderul `uploads/` este versionat în Git, împreună cu codul. |
| NF-05 | **Locație**: coordonatele se extrag din linkuri Google Maps, pe server. Nu se folosește niciun serviciu de geocodare (Nominatim, Google Geocoding API sau altele) și nicio cheie de API. |

### 8.2 Securitate

| ID | Cerință |
|---|---|
| NF-06 | La fiecare cerere se verifică și statusul contului, astfel încât un cont șters își pierde accesul imediat, chiar dacă token-ul lui nu a expirat. |
| NF-07 | Fiecare operație pe anunțuri și cereri verifică cine este proprietarul elementului. |
| NF-08 | Datele se validează atât în client, cât și pe server. Serverul este sursa de adevăr, inclusiv pentru coordonate (LOC-04). |
| NF-09 | Serverul face cereri HTTP externe doar către domeniile din lista permisă de la LOC-07, ca protecție împotriva atacurilor SSRF. |
| NF-10 | Datele sensibile (hash-ul parolei, tokenurile de resetare) nu apar niciodată în răspunsurile API. |

### 8.3 Date și consistență

| ID | Cerință |
|---|---|
| NF-11 | Anunțurile și cererile se șterg logic (status `DELETED`), iar conturile se anonimizează. |
| NF-12 | Operațiile care modifică mai multe tabele, de exemplu ștergerea unui cont, rulează într-o singură tranzacție. |
| NF-13 | Câmpurile `createdAt` și `updatedAt` se completează automat. |

### 8.4 API și experiența utilizatorului

| ID | Cerință |
|---|---|
| NF-14 | API-ul REST folosește coduri HTTP consecvente: `200`, `201`, `204`, `400` (cu erorile pe câmpuri), `401`, `403`, `404`, `409` și `422`. |
| NF-15 | Toate listele sunt paginate pe server. |
| NF-16 | Interfața este responsivă (desktop și mobil) și în limba română. |
| NF-17 | Acțiunile ireversibile (ștergere, schimbare de status) cer o confirmare explicită. |
| NF-18 | Erorile sunt afișate lângă câmpul la care se referă, iar mesajele de succes apar ca notificări scurte. |

---

## 9. Diagrame asociate

| Etapă | Diagrame de secvență (`sequence-diagrams/`) |
|---|---|
| 1.1 | vizitator: înregistrare, autentificare · chiriaș, proprietar și admin: autentificare |
| 1.2 | chiriaș și proprietar: editare profil, ștergere cont |
| 1.3 | proprietar: creare anunț, editare anunț, status și ștergere anunț, „Anunțurile mele” |
| 1.4 | chiriaș: creare cerere, editare cerere, status și ștergere cerere, „Cererile mele” |
| 1.5 | vizitator și chiriaș: căutare și filtrare anunțuri, detalii anunț · proprietar: căutare cereri, detalii cerere |
| 1.6 | admin: listă utilizatori, editare utilizator, ștergere utilizator |

**Diagrame care trebuie actualizate pentru acest document:**
- **Creare anunț, editare anunț și creare cerere** folosesc încă Nominatim pentru geocodare. Trebuie înlocuit cu fluxul din secțiunea 4.2: link Google Maps, extragere pe server, pin de confirmare.
- **Detalii anunț** afișează încă butoanele „Contactează”, „Favorite” și „Raportează”, care nu fac parte din faza 1.
- **Listă utilizatori** are încă filtrul pe status.
- **Editare profil, ștergere cont, creare și editare anunț** au participantul `ImageStorageService (disc local / S3)`. Trebuie redenumit în `ImageStorageService (disc local)`.
- **Resetarea parolei** (etapa 1.2) nu are diagramă de secvență.
- **Diagrama de clase:** `Listing` are nevoie de câmpul opțional `googleMapsUrl` (LOC-11), iar `RentalRequest` de același câmp pentru zona căutată.
