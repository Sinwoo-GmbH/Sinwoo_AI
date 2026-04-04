# SINWOO Next-Gen Phase 1 Porting Progress

## 1. Scope

This document records the first real delivery step after the legacy assessment phase.

Phase 1 is no longer analysis only. The next-gen bootstrap now contains an executable common-axis port for:

- tenant
- company
- department
- employee
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

### 2.3 Company Department Employee Hierarchy Extension

Applied through:

- [`V5__add_department_and_employee_hierarchy.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V5__add_department_and_employee_hierarchy.sql)

Key additions:

- `TB_DEPT`
- `TB_EMP`
- `TB_DEPT_HIST`
- `TB_EMP_HIST`
- department tree structure by `UP_DEPT_ID`
- employee reporting structure by `MGR_EMP_ID`
- user to employee link by `USR_ID`

### 2.4 Master APIs

Implemented core APIs:

- `POST /api/v1/tenants`
- `GET /api/v1/tenants`
- `POST /api/v1/companies`
- `GET /api/v1/companies`
- `POST /api/v1/departments`
- `GET /api/v1/departments`
- `GET /api/v1/departments/tree`
- `POST /api/v1/employees`
- `GET /api/v1/employees`
- `POST /api/v1/roles`
- `GET /api/v1/roles`
- `POST /api/v1/users`
- `GET /api/v1/users`

New authorization and billing APIs:

- `POST /api/v1/menus`
- `GET /api/v1/menus`
- `GET /api/v1/menus/visible`
- `GET /api/v1/menus/visible-by-user`
- `POST /api/v1/role-menu-auths`
- `GET /api/v1/role-menu-auths`
- `POST /api/v1/subscription-plans`
- `GET /api/v1/subscription-plans`
- `POST /api/v1/subscriptions`
- `GET /api/v1/subscriptions`
- `POST /api/v1/payment-transactions`
- `GET /api/v1/payment-transactions`

### 2.5 Security Bridge

For the current dev phase, the master APIs are temporarily exposed through the security configuration so the migration can proceed without waiting for the final JWT and tenant auth redesign.

### 2.6 History and Audit Continuity

Per-table history remains database-driven:

- `TB_TENANT_HIST`
- `TB_CO_HIST`
- `TB_DEPT_HIST`
- `TB_EMP_HIST`
- `TB_USR_HIST`
- `TB_ROLE_HIST`
- `TB_USR_ROLE_HIST`
- `TB_MNU_HIST`
- `TB_ROLE_MNU_AUTH_HIST`
- `TB_SUBS_PLAN_HIST`
- `TB_SUBS_HIST`
- `TB_PAY_TXN_HIST`

The V3, V4, and V5 migrations recreate the affected triggers so the expanded fields are captured in history rows.

## 3. Verification Results

Verified on April 4, 2026:

- Gradle test suite passed
- Flyway V3 and V4 applied successfully on MariaDB
- Flyway V5 applied successfully in test and runtime validation
- `tenant -> company -> department -> employee -> user` hierarchy is now modeled in schema and APIs
- role hierarchy seed data was available
- internal tenant automatically forced to billing free
- paid payment request was blocked for internal tenant
- visible menus were returned differently by role scope, role depth, and assigned user
- history rows were created in:
  - `TB_TENANT_HIST`
  - `TB_CO_HIST`
  - `TB_DEPT_HIST`
  - `TB_EMP_HIST`
  - `TB_USR_HIST`
  - `TB_ROLE_HIST`
  - `TB_USR_ROLE_HIST`
  - `TB_MNU_HIST`
  - `TB_ROLE_MNU_AUTH_HIST`
  - `TB_SUBS_PLAN_HIST`
  - `TB_SUBS_HIST`
  - `TB_PAY_TXN_HIST`
- request log rows accumulated in `TB_ACCESS_LOG`
- OAuth bridge schema migrated with `TB_USR_OAUTH` and `TB_USR_OAUTH_HIST`
- OAuth provider listing endpoint available
- tenant email domain mapping added through `TB_TENANT.EML_DOMN`
- direct credential login endpoint available through `eml + pwd`
- login tenant is resolved automatically from the email domain
- OAuth callback can link or provision a tenant user and issue SINWOO JWT tokens
- integrated login page now supports both credential login and OAuth login

## 4. Current Assessment

The project has moved from:

- analysis only

to:

- executable next-gen common-axis foundation with B2B authorization and billing control

This is the point where the legacy upgrade stopped being only a strategy track and became an actual running next-generation delivery.

German-law compliance is now a mandatory baseline for future delivery, not a later documentation task.

Reference:

- [`08_german_compliance_baseline.md`](C:\Users\JuyongLee\Sinwoo_AI\docs\legacy\08_german_compliance_baseline.md)

## 5. Next Recommended Porting Target

The next execution block should continue from the common axis and move into one of these areas:

1. authentication redesign bridge
2. attendance domain port under German-law assumptions
3. finance and request bridge APIs from legacy SQL assets with GoBD and e-invoice constraints
4. payroll and HR bridge around employee master with privacy and AI controls

The authentication redesign bridge is no longer only a planning item. The first executable integrated login bridge now exists and should be hardened next.
