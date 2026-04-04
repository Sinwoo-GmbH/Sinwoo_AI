# SINWOO Next-Gen Phase 1 Porting Progress

## 1. Scope

This document records the first real delivery step after the legacy assessment phase.

Phase 1 is no longer analysis only. The next-gen bootstrap now contains an executable common-axis port for:

- tenant
- company
- role
- user
- menu authorization
- subscription billing
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

### 2.2 B2B Authorization and Billing Extension

Applied through:

- [`V4__add_b2b_authorization_menu_and_billing.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V4__add_b2b_authorization_menu_and_billing.sql)

Key additions:

- `TB_TENANT.TENANT_TP_CD`
- `TB_TENANT.BILL_FREE_YN`
- `TB_ROLE.ROLE_SCOPE_CD`
- `TB_ROLE.ROLE_D1_CD`
- `TB_ROLE.ROLE_D2_CD`
- `TB_ROLE.ROLE_D3_CD`
- `TB_MNU`
- `TB_ROLE_MNU_AUTH`
- `TB_SUBS_PLAN`
- `TB_SUBS`
- `TB_PAY_TXN`

Seeded role hierarchy:

- platform admin
  - `ROLE_PLATFORM_SUPER_ADMIN`
  - `ROLE_PLATFORM_ADMIN`
- customer user
  - `ROLE_CUSTOMER_USER_MEMBER`
  - `ROLE_CUSTOMER_USER_LEADER`
- customer finance admin
  - `ROLE_CUSTOMER_FINANCE_ADMIN_MEMBER`
  - `ROLE_CUSTOMER_FINANCE_ADMIN_LEADER`
- customer HR admin
  - `ROLE_CUSTOMER_HR_ADMIN_MEMBER`
  - `ROLE_CUSTOMER_HR_ADMIN_LEADER`
- customer integrated admin
  - `ROLE_CUSTOMER_ADMIN_MEMBER`
  - `ROLE_CUSTOMER_ADMIN_LEADER`

Seeded menu scope:

- admin menus
- customer menus

Seeded billing plans:

- `PLAN_INTERNAL_FREE`
- `PLAN_B2B_BASIC`
- `PLAN_B2B_PRO`

### 2.3 Master APIs

Implemented core APIs:

- `POST /api/v1/tenants`
- `GET /api/v1/tenants`
- `POST /api/v1/companies`
- `GET /api/v1/companies`
- `POST /api/v1/roles`
- `GET /api/v1/roles`
- `POST /api/v1/users`
- `GET /api/v1/users`

New authorization and billing APIs:

- `POST /api/v1/menus`
- `GET /api/v1/menus`
- `GET /api/v1/menus/visible`
- `POST /api/v1/role-menu-auths`
- `GET /api/v1/role-menu-auths`
- `POST /api/v1/subscription-plans`
- `GET /api/v1/subscription-plans`
- `POST /api/v1/subscriptions`
- `GET /api/v1/subscriptions`
- `POST /api/v1/payment-transactions`
- `GET /api/v1/payment-transactions`

### 2.4 Security Bridge

For the current dev phase, the master APIs are temporarily exposed through the security configuration so the migration can proceed without waiting for the final JWT and tenant auth redesign.

### 2.5 History and Audit Continuity

Per-table history remains database-driven:

- `TB_TENANT_HIST`
- `TB_CO_HIST`
- `TB_USR_HIST`
- `TB_ROLE_HIST`
- `TB_USR_ROLE_HIST`
- `TB_MNU_HIST`
- `TB_ROLE_MNU_AUTH_HIST`
- `TB_SUBS_PLAN_HIST`
- `TB_SUBS_HIST`
- `TB_PAY_TXN_HIST`

The V3 and V4 migrations recreate the affected triggers so the expanded fields are captured in history rows.

## 3. Verification Results

Verified on April 4, 2026:

- Gradle test suite passed
- Flyway V3 and V4 applied successfully on MariaDB
- `tenant -> company -> user` creation flow executed successfully
- role hierarchy seed data was available
- internal tenant automatically forced to billing free
- paid payment request was blocked for internal tenant
- visible menus were returned differently by role scope and role depth
- history rows were created in:
  - `TB_TENANT_HIST`
  - `TB_CO_HIST`
  - `TB_USR_HIST`
  - `TB_ROLE_HIST`
  - `TB_USR_ROLE_HIST`
  - `TB_MNU_HIST`
  - `TB_ROLE_MNU_AUTH_HIST`
  - `TB_SUBS_PLAN_HIST`
  - `TB_SUBS_HIST`
  - `TB_PAY_TXN_HIST`
- request log rows accumulated in `TB_ACCESS_LOG`

## 4. Current Assessment

The project has moved from:

- analysis only

to:

- executable next-gen common-axis foundation with B2B authorization and billing control

This is the point where the legacy upgrade stopped being only a strategy track and became an actual running next-generation delivery.

## 5. Next Recommended Porting Target

The next execution block should continue from the common axis and move into one of these areas:

1. authentication redesign bridge
2. company-department-employee hierarchy
3. attendance domain port
4. finance and request bridge APIs from legacy SQL assets
