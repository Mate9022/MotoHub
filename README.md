# MotoHub -- Fejlesztői környezet beállítása

Ez a dokumentum a MotoHub projekt teljes helyi fejlesztői környezetének
beállítását írja le a nulláról.

A projekt felépítése:

-   **Frontend:** Angular
-   **Backend:** Java + Spring Boot
-   **Adatbázis:** PostgreSQL 16
-   **Adatbázis futtatása:** Docker Desktop
-   **Java:** JDK 21
-   **Frontend fejlesztői szerver:** `localhost:4200`
-   **Backend:** `localhost:8081`
-   **PostgreSQL:** `localhost:5432`

> **Megjegyzés:** A backend eredetileg a `8080` portot használta, de
> ezen a gépen az `AgentService.exe` már foglalta ezt a portot. Ezért a
> MotoHub backend `8081` porton fut.

------------------------------------------------------------------------

## 1. Előfeltételek

A gépen szükséges:

1.  Windows 10/11
2.  Docker Desktop
3.  Java JDK 21
4.  Node.js + npm
5.  Git (ajánlott)
6.  VS Code (ajánlott)

Az XAMPP **nem szükséges**, mert a projekt nem PHP/MySQL alapú. A
PostgreSQL-t Docker futtatja.

------------------------------------------------------------------------

# 2. Projekt mappastruktúra

A projekt főmappája:

``` text
MotoHub/
├── motohub-backend-master/
│   ├── gradle/
│   ├── src/
│   ├── .dockerignore
│   ├── .gitattributes
│   ├── .gitignore
│   ├── .gitlab-ci.yml
│   ├── build.gradle
│   ├── Dockerfile
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle
│
└── motohub-frontend-master/
    ├── src/
    ├── .dockerignore
    ├── .gitattributes
    ├── .gitignore
    ├── .gitlab-ci.yml
    ├── angular.json
    ├── Dockerfile
    ├── nginx.conf
    ├── package.json
    ├── package-lock.json
    ├── tsconfig.app.json
    └── tsconfig.json
```

A két projekt külön mappában maradjon. Ne rakjuk a frontendet a backend
mappájába, és fordítva.

------------------------------------------------------------------------

# 3. Docker Desktop telepítése és virtualizáció

A Docker Desktopnak szüksége van hardveres virtualizációra.

## Ellenőrzés

Nyisd meg:

**Feladatkezelő → Teljesítmény → CPU**

A `Virtualizálás` értékének:

``` text
Engedélyezve
```

kell lennie.

Ha le van tiltva, BIOS-ban AMD processzornál általában az:

``` text
SVM Mode → Enabled
```

beállítást kell bekapcsolni.

------------------------------------------------------------------------

# 4. Windows WSL 2 / Virtual Machine Platform

Ha a Docker Desktop azt írja:

``` text
Virtualization support not detected
```

miközben a BIOS-ban a virtualizáció engedélyezve van, rendszergazdai
PowerShellben futtasd:

``` powershell
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
```

majd:

``` powershell
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
```

Ezután:

``` powershell
wsl --set-default-version 2
```

Indítsd újra a számítógépet.

Ezután indítsd el a Docker Desktopot.

------------------------------------------------------------------------

# 5. PostgreSQL létrehozása Dockerben

A MotoHub PostgreSQL adatbázisa Docker containerben fut.

PowerShellben:

``` powershell
docker run --name motoros-postgres `
  -e POSTGRES_DB=motoros `
  -e POSTGRES_USER=motoros `
  -e POSTGRES_PASSWORD=motoros `
  -p 5432:5432 `
  -d postgres:16
```

A konfiguráció:

  Beállítás           Érték
  ------------------- --------------------
  Container neve      `motoros-postgres`
  PostgreSQL verzió   `16`
  Adatbázis           `motoros`
  Felhasználó         `motoros`
  Jelszó              `motoros`
  Port                `5432`

## Ellenőrzés

``` powershell
docker ps
```

A listában szerepelnie kell:

``` text
motoros-postgres
```

és a portnak körülbelül így kell megjelennie:

``` text
0.0.0.0:5432->5432/tcp
```

## Ha a container már létezik

Ha ezt kapod:

``` text
Conflict. The container name "/motoros-postgres" is already in use
```

akkor nem kell újra létrehozni.

Indítsd:

``` powershell
docker start motoros-postgres
```

Ellenőrzés:

``` powershell
docker ps
```

------------------------------------------------------------------------

# 6. Backend beállítása

Nyisd meg a backend mappát VS Code-ban:

``` text
MotoHub/motohub-backend-master
```

A backend Spring Boot alkalmazás.

## Adatbázis környezeti változók

A backend indítása előtt PowerShellben:

``` powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/motoros"
$env:DB_USER="motoros"
$env:DB_PASSWORD="motoros"
```

Ezek alapján a backend a Dockerben futó PostgreSQL-hez csatlakozik.

------------------------------------------------------------------------

# 7. Backend port -- 8081

A projekt eredetileg a `8080` portot használta.

A gépen azonban a `8080` portot az alábbi Windows szolgáltatás
használta:

``` text
AgentService.exe
PID: 5200
```

Ellenőrzés:

``` powershell
netstat -ano | findstr :8080
```

A folyamat azonosítása:

``` powershell
tasklist /FI "PID eq 5200"
```

Ezért a MotoHub backend portját `8081`-re állítottuk.

Az `application.yaml` fájlban:

``` yaml
server:
  port: 8081
```

------------------------------------------------------------------------

# 8. Backend indítása

Lépj be a backend mappába:

``` powershell
cd C:\Users\lakom\Desktop\MotoHub\motohub-backend-master
```

Állítsd be az adatbázis változókat:

``` powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/motoros"
$env:DB_USER="motoros"
$env:DB_PASSWORD="motoros"
```

Indítsd el a Spring Boot alkalmazást:

``` powershell
.\gradlew.bat bootRun
```

Sikeres indulás esetén ilyen üzenet jelenik meg:

``` text
Tomcat started on port 8081 (http)
```

és:

``` text
Started MotorWorkshopApiApplication
```

A terminált hagyd nyitva.

------------------------------------------------------------------------

# 9. PostgreSQL kapcsolat ellenőrzése

A backend indulási logjában ennek kell látszania:

``` text
Database JDBC URL [jdbc:postgresql://localhost:5432/motoros]
```

és:

``` text
Database version: 16.x
```

Ez azt jelenti, hogy:

``` text
Spring Boot
    ↓
localhost:5432
    ↓
Docker PostgreSQL
    ↓
motoros adatbázis
```

kapcsolat működik.

------------------------------------------------------------------------

# 10. Frontend telepítése

Nyiss egy **új terminált**.

A backend terminált ne zárd be.

Lépj a frontend mappába:

``` powershell
cd C:\Users\lakom\Desktop\MotoHub\motohub-frontend-master
```

Telepítsd a függőségeket:

``` powershell
npm install
```

Sikeres telepítés után valami ehhez hasonló jelenik meg:

``` text
added 303 packages
```

Az npm audit által jelzett vulnerability-ket külön lehet később kezelni.

------------------------------------------------------------------------

# 11. Angular frontend indítása

A frontend `package.json` fájljában van `start` script, ezért:

``` powershell
npm start
```

A frontend alapértelmezett fejlesztői szervere:

``` text
http://localhost:4200/
```

Sikeres induláskor:

``` text
Application bundle generation complete.
```

és:

``` text
Local: http://localhost:4200/
```

------------------------------------------------------------------------

# 12. Frontend és backend összekötése

A frontend API útvonala:

``` text
/api
```

A backend viszont:

``` text
http://localhost:8081
```

Ezért fejlesztői módban Angular proxyt használunk.

A frontend gyökerében legyen:

``` text
motohub-frontend-master/
└── proxy.conf.json
```

A fájl tartalma:

``` json
{
  "/api": {
    "target": "http://localhost:8081",
    "secure": false,
    "changeOrigin": true
  }
}
```

Ez azt jelenti, hogy az Angular által küldött:

``` text
/api/...
```

kérések továbbítódnak ide:

``` text
http://localhost:8081/api/...
```

------------------------------------------------------------------------

# 13. Frontend indítása proxyval

A frontend mappájában:

``` powershell
npm start -- --proxy-config proxy.conf.json
```

Ezután:

``` text
Frontend:
http://localhost:4200

Backend:
http://localhost:8081

PostgreSQL:
localhost:5432
```

------------------------------------------------------------------------

# 14. A teljes rendszer működése

A helyi fejlesztői környezet végül így néz ki:

``` text
┌─────────────────────────────┐
│        Angular Frontend     │
│       localhost:4200        │
└──────────────┬──────────────┘
               │
               │ /api
               ▼
┌─────────────────────────────┐
│       Angular Proxy         │
│       localhost:8081        │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│     Spring Boot Backend     │
│       localhost:8081        │
└──────────────┬──────────────┘
               │
               │ JDBC
               ▼
┌─────────────────────────────┐
│     Docker PostgreSQL       │
│       localhost:5432        │
│                             │
│ Database: motoros           │
│ User: motoros               │
└─────────────────────────────┘
```

------------------------------------------------------------------------

# 15. Indítás a következő alkalommal

A teljes projekt újraindításakor három dolgot kell futtatni.

## 1. Docker

Indítsd el a Docker Desktopot.

Ellenőrzés:

``` powershell
docker ps
```

Ha a PostgreSQL container leállt:

``` powershell
docker start motoros-postgres
```

------------------------------------------------------------------------

## 2. Backend

Új PowerShell terminál:

``` powershell
cd C:\Users\lakom\Desktop\MotoHub\motohub-backend-master
```

``` powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/motoros"
$env:DB_USER="motoros"
$env:DB_PASSWORD="motoros"
```

``` powershell
.\gradlew.bat bootRun
```

Ezt a terminált hagyd futni.

------------------------------------------------------------------------

## 3. Frontend

Új terminál:

``` powershell
cd C:\Users\lakom\Desktop\MotoHub\motohub-frontend-master
```

``` powershell
npm start -- --proxy-config proxy.conf.json
```

Ezután böngésző:

``` text
http://localhost:4200
```

------------------------------------------------------------------------

# 16. Leállítás

## Frontend

A frontend terminálban:

``` text
Ctrl + C
```

## Backend

A backend terminálban:

``` text
Ctrl + C
```

## PostgreSQL

A PostgreSQL container leállítása:

``` powershell
docker stop motoros-postgres
```

Fontos: a `docker stop` nem törli az adatbázis containert.

Később újraindítható:

``` powershell
docker start motoros-postgres
```

------------------------------------------------------------------------

# 17. Gyakori hibák

## Port 8080 már használatban

Hiba:

``` text
Web server failed to start.
Port 8080 was already in use.
```

A MotoHubnak `8081` porton kell futnia.

Ellenőrizd az `application.yaml` fájlt:

``` yaml
server:
  port: 8081
```

------------------------------------------------------------------------

## PostgreSQL nem érhető el

Ellenőrizd:

``` powershell
docker ps
```

Ha nincs futó `motoros-postgres`:

``` powershell
docker start motoros-postgres
```

------------------------------------------------------------------------

## Frontend nem tölti be az adatokat

Ellenőrizd, hogy:

1.  a backend fut-e `8081`-en;
2.  a frontend proxy konfiguráció létezik-e;
3.  a frontend a proxyval lett-e indítva.

Indítás:

``` powershell
npm start -- --proxy-config proxy.conf.json
```

------------------------------------------------------------------------

## Docker Desktop nem indul

Ellenőrizd:

-   BIOS virtualizáció: **Enabled**
-   Windows Virtual Machine Platform engedélyezve
-   WSL 2 működik
-   Docker Desktop fut

Ellenőrzés:

``` powershell
wsl --status
```

------------------------------------------------------------------------

# 18. XAMPP

A MotoHub futtatásához **nem szükséges XAMPP**.

A projekt technológiái:

``` text
Angular
Spring Boot
PostgreSQL
Docker
```

Nem használ:

``` text
PHP
MySQL
Apache (XAMPP)
```

Ezért a MotoHub indításakor az XAMPP-ot nem kell elindítani.

------------------------------------------------------------------------

# 19. Hasznos címek

  Szolgáltatás       Cím
  ------------------ -------------------------
  MotoHub frontend   `http://localhost:4200`
  MotoHub backend    `http://localhost:8081`
  PostgreSQL         `localhost:5432`
  Adatbázis          `motoros`

------------------------------------------------------------------------

# 20. Gyors indítás -- rövid verzió

Ha minden már egyszer be lett állítva:

### Docker

``` powershell
docker start motoros-postgres
```

### Backend

``` powershell
cd C:\Users\lakom\Desktop\MotoHub\motohub-backend-master
$env:DB_URL="jdbc:postgresql://localhost:5432/motoros"
$env:DB_USER="motoros"
$env:DB_PASSWORD="motoros"
.\gradlew.bat bootRun
```

### Frontend -- új terminál

``` powershell
cd C:\Users\lakom\Desktop\MotoHub\motohub-frontend-master
npm start -- --proxy-config proxy.conf.json
```

### Böngésző

``` text
http://localhost:4200
```

------------------------------------------------------------------------

## Állapot

A helyi fejlesztői környezet ellenőrzött állapotban:

-   [x] Docker Desktop működik
-   [x] PostgreSQL 16 fut Dockerben
-   [x] `motoros` adatbázis létrejött
-   [x] Spring Boot backend működik
-   [x] PostgreSQL kapcsolat működik
-   [x] Backend `8081` porton működik
-   [x] Angular függőségek telepítve
-   [x] Angular frontend működik
-   [x] Angular → Backend proxy működik
-   [x] MotoHub felület betölt
-   [x] Munkalapok API-kérése működik
