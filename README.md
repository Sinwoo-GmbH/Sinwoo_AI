# Sinwoo AI Backend

Initial backend bootstrap for a modular monolith B2B SaaS platform built with Java 21, Spring Boot 3, Gradle, PostgreSQL, Redis, MinIO, Flyway, Spring Security, and Actuator.

## Architecture Direction

- Modular monolith, not microservices
- Module-oriented packages under `com.sinwoo`
- Multi-tenant ready foundation
- Planned domains: auth, tenant, user, company, document, bookkeeping OCR, attendance, HR, payroll, asset management
- Multilingual support planned for Korean, English, and German

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Gradle
- PostgreSQL
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

- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- MinIO API: `localhost:9000`
- MinIO Console: `localhost:9001`

### 2. Run the application

If Gradle is installed locally:

```bash
gradle bootRun --args='--spring.profiles.active=local'
```

Or generate a Gradle wrapper and use it:

```bash
gradle wrapper
./gradlew bootRun --args='--spring.profiles.active=local'
```

The wrapper is not committed in this bootstrap because `gradle` is not installed in the current environment.

On Windows PowerShell:

```powershell
gradle bootRun --args="--spring.profiles.active=local"
```

### 3. Verify

- App: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- API info: `http://localhost:8080/api/v1/system/ping`

## Default Local Credentials

### PostgreSQL

- Database: `sinwoo`
- Username: `sinwoo`
- Password: `sinwoo`

### MinIO

- Access key: `sinwoo`
- Secret key: `sinwoo123`

## Notes

- Flyway runs automatically on startup.
- Security is intentionally minimal and placeholder-oriented at this stage.
- The current schema includes tenants, companies, users, roles, and user-role mapping.
- The app is structured for future tenant-aware security, auditing, localization, and module boundaries.
