# Sinwoo AI Platform

Next-generation modular monolith B2B SaaS platform built with Java 21, Spring Boot 3, Gradle, MariaDB, Redis, MinIO, Flyway, Spring Security, and a separate Next.js frontend.

## Architecture Direction

- Modular monolith, not microservices
- Module-oriented packages under `com.sinwoo`
- Multi-tenant ready foundation
- Planned domains: auth, tenant, user, company, menu authorization, subscription billing, document, bookkeeping OCR, attendance, HR, payroll, asset management
- Current execution domains: tenant, company, department, employee, user, role, menu authorization, subscription billing
- Multilingual support planned for Korean, English, and German
- Abbreviation-based database naming standard applied
- German-law-first compliance baseline for German entities
- Both internal use and external B2B customer use are supported

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
|- department
|- employee
|- document
|- menu
`- billing

frontend
|- app
|- components
|- lib
`- package.json
```

Each module can grow independently with its own:

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

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

### 3. Verify backend

- App: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Ping: `http://localhost:8080/api/v1/system/ping`

## Frontend Bootstrap

The repository includes a separate `frontend` app prepared for:

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

## Database Standard

The current platform follows these conventions:

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
- `TB_DEPT`
- `TB_EMP`
- `TB_MNU`
- `TB_ROLE_MNU_AUTH`
- `TB_SUBS_PLAN`
- `TB_SUBS`
- `TB_PAY_TXN`
- `TB_USR_OAUTH`

History tables:

- `TB_TENANT_HIST`
- `TB_CO_HIST`
- `TB_USR_HIST`
- `TB_ROLE_HIST`
- `TB_USR_ROLE_HIST`
- `TB_DEPT_HIST`
- `TB_EMP_HIST`
- `TB_MNU_HIST`
- `TB_ROLE_MNU_AUTH_HIST`
- `TB_SUBS_PLAN_HIST`
- `TB_SUBS_HIST`
- `TB_PAY_TXN_HIST`
- `TB_USR_OAUTH_HIST`

Operational log table:

- `TB_ACCESS_LOG`

## Current Next-Gen Porting Status

The current next-gen foundation includes:

- tenant master API
- company master API
- role master API
- user master API
- department master API
- employee master API
- role depth hierarchy for admin/customer authorization
- menu master API
- role-menu authorization API
- subscription plan API
- subscription API
- payment transaction API
- OAuth2 login bridge and SINWOO JWT issue
- per-table history logging by MariaDB triggers
- request/access logging through `TB_ACCESS_LOG`

### Tenant policy rules

- Internal tenant: `TENANT_TP_CD = INTERNAL`
- Internal tenant is always billing-free
- Customer tenant: `TENANT_TP_CD = CUSTOMER`
- Customer tenant can use paid subscription flow

### Role depth model

- Depth 1
  - `ADMIN`
  - `CUSTOMER`
- Depth 2
  - admin: `SUPER_ADMIN`, `NORMAL_ADMIN`
  - customer: `USER`, `FINANCE_ADMIN`, `HR_ADMIN`, `ADMIN`
- Depth 3
  - customer: `TEAM_MEMBER`, `TEAM_LEADER`

Current APIs:

- `GET /api/v1/system/ping`
- `GET /api/v1/tenants`
- `POST /api/v1/tenants`
- `GET /api/v1/companies?tenantId=<id>`
- `POST /api/v1/companies`
- `GET /api/v1/roles`
- `POST /api/v1/roles`
- `GET /api/v1/users?tenantId=<id>&coId=<id>`
- `POST /api/v1/users`
- `GET /api/v1/departments?tenantId=<id>&coId=<id>`
- `GET /api/v1/departments/tree?tenantId=<id>&coId=<id>`
- `POST /api/v1/departments`
- `GET /api/v1/employees?tenantId=<id>&coId=<id>&deptId=<id>`
- `POST /api/v1/employees`
- `GET /api/v1/menus?mnuScopeCd=<scope>`
- `GET /api/v1/menus/visible?roleCd=<roleCd>&mnuScopeCd=<scope>`
- `GET /api/v1/menus/visible-by-user?usrId=<id>&mnuScopeCd=<scope>`
- `POST /api/v1/menus`
- `GET /api/v1/role-menu-auths?roleCd=<roleCd>`
- `POST /api/v1/role-menu-auths`
- `GET /api/v1/subscription-plans`
- `POST /api/v1/subscription-plans`
- `GET /api/v1/subscriptions?tenantId=<id>`
- `POST /api/v1/subscriptions`
- `GET /api/v1/auth/oauth/providers`
- `GET /api/v1/auth/oauth/authorize/{registrationId}?tenantCd=<tenantCd>`
- `GET /api/v1/auth/me`

## OAuth Login Bridge

The next-generation platform now supports an OAuth login bridge:

- provider login through Spring Security OAuth2 client
- tenant-aware user linking by `tenantCd`
- automatic user provisioning when the tenant user does not exist yet
- SINWOO access token and refresh token issue after provider callback
- frontend login page at `http://localhost:3000/login`
- frontend callback page at `http://localhost:3000/auth/callback`

Provider registration is intentionally configuration-driven.

Recommended environment variables:

```text
FRONTEND_BASE_URL=http://localhost:3000

OAUTH_GOOGLE_CLIENT_ID=...
OAUTH_GOOGLE_CLIENT_SECRET=...

OAUTH_MICROSOFT_CLIENT_ID=...
OAUTH_MICROSOFT_CLIENT_SECRET=...
OAUTH_MICROSOFT_TENANT_ID=common
```

Without provider credentials, the backend still starts normally and the frontend login page remains available, but the provider list is empty until a provider is configured.
- `GET /api/v1/payment-transactions?tenantId=<id>`
- `POST /api/v1/payment-transactions`

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
- The current schema includes tenant, company, user, role, user-role mapping, menu authorization, subscription billing, and per-table history tables.
- Company to department to employee hierarchy is now part of the running next-gen base.
- Change history is recorded automatically by MariaDB triggers, not by application service logic.
- Request/access logs are stored separately in `TB_ACCESS_LOG` by a common web filter.
- Menu visibility is controlled by `TB_ROLE_MNU_AUTH`, not by hard-coded frontend assumptions.
- Internal company usage is modeled as a billing-free internal tenant instead of a special-case code path.
- The app is structured for future tenant-aware security, auditing, localization, and module boundaries.
- Frontend API contract types can be shared from `frontend/lib/api`.
