# Sinwoo AI Platform

Bootstrap project for a modular monolith B2B SaaS platform built with Java 21, Spring Boot 3, Gradle, MariaDB, Redis, MinIO, Flyway, Spring Security, and a separate Next.js frontend.

## Architecture Direction

- Modular monolith, not microservices
- Module-oriented packages under `com.sinwoo`
- Multi-tenant ready foundation
- Planned domains: auth, tenant, user, company, document, bookkeeping OCR, attendance, HR, payroll, asset management
- Multilingual support planned for Korean, English, and German
- Abbreviation-based database naming standard applied

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Gradle
- MariaDB
- Redis
- MinIO
- Flyway
- Spring Security
- Spring Data JPA
- Validation
- Lombok
- Actuator

## Project Structure

```text
src/main/java/com/sinwoo
|- SinwooBackendApplication.java
|- common
|- auth
|- tenant
|- user
|- company
`- document

frontend
|- app
|- components
|- lib
`- package.json
```

Each module currently contains a package marker and can grow independently with its own:

- `application`
- `domain`
- `infrastructure`
- `interfaces`

## Local Run

### 1. Start infrastructure

```bash
docker compose up -d
```

Services:

- MariaDB: `localhost:3306`
- Redis: `localhost:6379`
- MinIO API: `localhost:9000`
- MinIO Console: `localhost:9001`

### 2. Run the backend

Use the committed Gradle wrapper:

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

### 3. Verify

- App: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- API info: `http://localhost:8080/api/v1/system/ping`

## Frontend Bootstrap

The repository now includes a separate `frontend` app prepared for:

- Next.js App Router
- Tailwind CSS
- shadcn/ui-style component structure
- B2B dashboard-first layout

### Frontend run

```bash
cd frontend
npm install
npm run dev
```

Then open:

- Frontend: `http://localhost:3000`

The first screen is a branded dashboard scaffold intended for the Sinwoo customer portal and admin console direction.

## Database Standard

The current bootstrap follows these conventions:

- Tables: `TB_<ENTITY>`
- Primary keys: `PK_<TABLE>`
- Foreign keys: `FK_<TABLE>_<SEQ>`
- Indexes: `IX_<TABLE>_<COLUMN>`
- Unique keys: `UK_<TABLE>_<COLUMN>`
- Audit columns:
  - `CRT_BY`
  - `CRT_DTM`
  - `UPD_BY`
  - `UPD_DTM`

Main base tables:

- `TB_TENANT`
- `TB_CO`
- `TB_USR`
- `TB_ROLE`
- `TB_USR_ROLE`
- `TB_CHG_HIST`

## Default Local Credentials

### MariaDB

- Database: `sinwoo`
- Username: `sinwoo`
- Password: `sinwoo`

### MinIO

- Access key: `sinwoo`
- Secret key: `sinwoo123`

## Notes

- Flyway runs automatically on startup.
- Security is intentionally minimal and placeholder-oriented at this stage.
- The current schema includes tenant, company, user, role, user-role mapping, and change history.
- The app is structured for future tenant-aware security, auditing, localization, and module boundaries.
- Frontend API contract types can be shared from `frontend/lib/api`.
