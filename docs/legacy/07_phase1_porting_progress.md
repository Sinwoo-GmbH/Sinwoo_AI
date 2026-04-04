# SINWOO Next-Gen Phase 1 Porting Progress

## 1. Scope

This document records the first real delivery step after the legacy assessment phase.

Phase 1 is no longer only analysis. The next-gen bootstrap now contains an executable common-axis port for:

- tenant
- company
- role
- user
- access log
- per-table history

## 2. Implemented Deliverables

### 2.1 Common-Axis Schema Extension

Applied through:

- [`V3__extend_core_domain_for_nextgen.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V3__extend_core_domain_for_nextgen.sql)

Key additions:

- `TB_CO.CO_CD`
- `TB_USR.LGN_ID`
- `TB_USR.TEL_NO`
- `TB_USR.AUTH_GRP_CD`
- `TB_USR.AUTH_LVL_CD`
- `TB_ROLE.ROLE_GRP_CD`
- `TB_ROLE.ROLE_LVL_CD`

Additional unique keys:

- `UK_TB_CO_TENANT_ID_CO_CD`
- `UK_TB_USR_TENANT_ID_LGN_ID`
- `UK_TB_USR_TENANT_ID_EML`

### 2.2 Master APIs

Implemented endpoints:

- `POST /api/v1/companies`
- `GET /api/v1/companies`
- `POST /api/v1/roles`
- `GET /api/v1/roles`
- `POST /api/v1/users`
- `GET /api/v1/users`

Existing tenant APIs remain active:

- `POST /api/v1/tenants`
- `GET /api/v1/tenants`

### 2.3 Security Bridge

For the current dev phase, the master APIs are temporarily exposed through the security configuration so the migration can proceed without waiting for the final JWT/tenant auth redesign.

### 2.4 History and Audit Continuity

Per-table history remains database-driven:

- `TB_TENANT_HIST`
- `TB_CO_HIST`
- `TB_USR_HIST`
- `TB_ROLE_HIST`
- `TB_USR_ROLE_HIST`

The V3 migration recreates the affected triggers so the expanded fields are captured in history rows.

## 3. Verification Results

Verified on April 4, 2026:

- Gradle test suite passed
- Flyway V3 applied successfully on MariaDB
- `tenant -> company -> user` creation flow executed successfully
- role seed data was available
- history rows were created in:
  - `TB_TENANT_HIST`
  - `TB_CO_HIST`
  - `TB_USR_HIST`
  - `TB_USR_ROLE_HIST`
- request log rows accumulated in `TB_ACCESS_LOG`

## 4. Current Assessment

The project has now moved from:

- analysis only

to:

- executable next-gen common-axis foundation

This is the first point where the legacy upgrade became an actual running delivery, not just a strategy document.

## 5. Next Recommended Porting Target

The next execution block should continue from the common axis and move into one of these areas:

1. authentication redesign bridge
2. company-department-employee hierarchy
3. attendance domain port
4. finance/request bridge APIs from legacy SQL assets
