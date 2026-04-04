# SINWOO Current Table Specification

## 1. Scope

This document describes the current next-generation standard schema for SINWOO.

## 2. Migration Files

- [`V1__init_schema.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V1__init_schema.sql)
- [`V2__add_access_log.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V2__add_access_log.sql)
- [`V3__extend_core_domain_for_nextgen.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V3__extend_core_domain_for_nextgen.sql)
- [`V4__add_b2b_authorization_menu_and_billing.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V4__add_b2b_authorization_menu_and_billing.sql)
- [`V5__add_department_and_employee_hierarchy.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V5__add_department_and_employee_hierarchy.sql)

## 3. Tables

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
- `TB_ACCESS_LOG`

## 4. Table Details

### 4.1 TB_TENANT

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Tenant primary key |
| TENANT_CD | VARCHAR(100) | N | Tenant code |
| TENANT_NM | VARCHAR(255) | N | Tenant name |
| TENANT_TP_CD | VARCHAR(20) | N | Tenant type code |
| BILL_FREE_YN | CHAR(1) | N | Billing free yes or no |
| STS_CD | VARCHAR(20) | N | Status code |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_TENANT`
- `UK_TB_TENANT_TENANT_CD`

### 4.2 TB_CO

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Company primary key |
| TENANT_ID | BIGINT | N | Tenant primary key reference |
| CO_CD | VARCHAR(100) | N | Company code |
| CO_NM | VARCHAR(255) | N | Company name |
| REG_NO | VARCHAR(100) | Y | Registration number |
| STS_CD | VARCHAR(20) | N | Status code |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_CO`
- `FK_TB_CO_01`
- `IX_TB_CO_TENANT_ID`
- `UK_TB_CO_TENANT_ID_CO_CD`

### 4.3 TB_USR

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | User primary key |
| TENANT_ID | BIGINT | N | Tenant primary key reference |
| CO_ID | BIGINT | Y | Company primary key reference |
| LGN_ID | VARCHAR(100) | N | Login identifier |
| EML | VARCHAR(255) | N | User email |
| PWD_HASH | VARCHAR(255) | N | Password hash |
| DSP_NM | VARCHAR(255) | N | Display name |
| LOCL_CD | VARCHAR(10) | N | Locale code |
| TEL_NO | VARCHAR(30) | Y | Telephone number |
| AUTH_GRP_CD | VARCHAR(20) | Y | Authorization group code |
| AUTH_LVL_CD | VARCHAR(20) | Y | Authorization level code |
| STS_CD | VARCHAR(20) | N | Status code |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_USR`
- `FK_TB_USR_01`
- `FK_TB_USR_02`
- `IX_TB_USR_TENANT_ID`
- `IX_TB_USR_CO_ID`
- `UK_TB_USR_TENANT_ID_LGN_ID`
- `UK_TB_USR_TENANT_ID_EML`

### 4.4 TB_ROLE

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Role primary key |
| ROLE_CD | VARCHAR(100) | N | Role code |
| ROLE_NM | VARCHAR(255) | N | Role name |
| ROLE_SCOPE_CD | VARCHAR(20) | Y | Role scope code |
| ROLE_D1_CD | VARCHAR(30) | Y | Role depth 1 code |
| ROLE_D2_CD | VARCHAR(30) | Y | Role depth 2 code |
| ROLE_D3_CD | VARCHAR(30) | Y | Role depth 3 code |
| ROLE_GRP_CD | VARCHAR(20) | Y | Role group code |
| ROLE_LVL_CD | VARCHAR(20) | Y | Role level code |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_ROLE`
- `UK_TB_ROLE_ROLE_CD`

### 4.5 TB_DEPT

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Department primary key |
| TENANT_ID | BIGINT | N | Tenant primary key reference |
| CO_ID | BIGINT | N | Company primary key reference |
| DEPT_CD | VARCHAR(100) | N | Department code |
| DEPT_NM | VARCHAR(255) | N | Department name |
| UP_DEPT_ID | BIGINT | Y | Parent department primary key |
| DEPT_LVL_NO | INT | N | Department level number |
| STS_CD | VARCHAR(20) | N | Status code |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_DEPT`
- `UK_TB_DEPT_TENANT_ID_CO_ID_DEPT_CD`
- `FK_TB_DEPT_01`
- `FK_TB_DEPT_02`
- `FK_TB_DEPT_03`
- `IX_TB_DEPT_TENANT_ID`
- `IX_TB_DEPT_CO_ID`
- `IX_TB_DEPT_UP_DEPT_ID`

### 4.6 TB_EMP

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Employee primary key |
| TENANT_ID | BIGINT | N | Tenant primary key reference |
| CO_ID | BIGINT | N | Company primary key reference |
| USR_ID | BIGINT | Y | User primary key reference |
| DEPT_ID | BIGINT | Y | Department primary key reference |
| MGR_EMP_ID | BIGINT | Y | Manager employee primary key reference |
| EMP_NO | VARCHAR(100) | N | Employee number |
| EMP_NM | VARCHAR(255) | N | Employee name |
| TEAM_ROLE_CD | VARCHAR(20) | N | Team role code |
| JOB_TTL_NM | VARCHAR(255) | Y | Job title name |
| HIRE_DT | DATE | Y | Hire date |
| RETR_DT | DATE | Y | Retire date |
| STS_CD | VARCHAR(20) | N | Status code |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_EMP`
- `UK_TB_EMP_TENANT_ID_CO_ID_EMP_NO`
- `UK_TB_EMP_USR_ID`
- `FK_TB_EMP_01`
- `FK_TB_EMP_02`
- `FK_TB_EMP_03`
- `FK_TB_EMP_04`
- `FK_TB_EMP_05`
- `IX_TB_EMP_TENANT_ID`
- `IX_TB_EMP_CO_ID`
- `IX_TB_EMP_USR_ID`
- `IX_TB_EMP_DEPT_ID`
- `IX_TB_EMP_MGR_EMP_ID`

### 4.7 TB_MNU

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Menu primary key |
| MNU_CD | VARCHAR(100) | N | Menu code |
| MNU_NM | VARCHAR(255) | N | Menu name |
| MNU_SCOPE_CD | VARCHAR(20) | N | Menu scope code |
| UP_MNU_ID | BIGINT | Y | Parent menu primary key |
| PATH_URI | VARCHAR(500) | Y | Menu path uri |
| ICON_NM | VARCHAR(100) | Y | Menu icon name |
| DSP_ORD | INT | N | Display order |
| USE_YN | CHAR(1) | N | Use yes or no |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_MNU`
- `FK_TB_MNU_01`
- `UK_TB_MNU_MNU_CD`
- `IX_TB_MNU_UP_MNU_ID`
- `IX_TB_MNU_SCOPE_CD`

### 4.8 TB_ROLE_MNU_AUTH

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Role-menu auth primary key |
| ROLE_ID | BIGINT | N | Role primary key reference |
| MNU_ID | BIGINT | N | Menu primary key reference |
| VIEW_YN | CHAR(1) | N | View yes or no |
| CRT_YN | CHAR(1) | N | Create yes or no |
| UPD_YN | CHAR(1) | N | Update yes or no |
| DEL_YN | CHAR(1) | N | Delete yes or no |
| APRV_YN | CHAR(1) | N | Approve yes or no |
| EXPRT_YN | CHAR(1) | N | Export yes or no |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_ROLE_MNU_AUTH`
- `UK_TB_ROLE_MNU_AUTH_ROLE_ID_MNU_ID`
- `FK_TB_ROLE_MNU_AUTH_01`
- `FK_TB_ROLE_MNU_AUTH_02`
- `IX_TB_ROLE_MNU_AUTH_ROLE_ID`
- `IX_TB_ROLE_MNU_AUTH_MNU_ID`

### 4.9 TB_SUBS_PLAN

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Subscription plan primary key |
| PLAN_CD | VARCHAR(100) | N | Plan code |
| PLAN_NM | VARCHAR(255) | N | Plan name |
| TENANT_TP_CD | VARCHAR(20) | N | Tenant type code |
| BILL_CYCL_CD | VARCHAR(20) | N | Billing cycle code |
| CURR_CD | VARCHAR(10) | N | Currency code |
| BASE_AMT | DECIMAL(15,2) | N | Base amount |
| USR_LMT_CNT | INT | Y | User limit count |
| USE_YN | CHAR(1) | N | Use yes or no |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_SUBS_PLAN`
- `UK_TB_SUBS_PLAN_PLAN_CD`

### 4.10 TB_SUBS

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Subscription primary key |
| TENANT_ID | BIGINT | N | Tenant primary key reference |
| PLAN_ID | BIGINT | N | Subscription plan primary key reference |
| SUBS_STS_CD | VARCHAR(20) | N | Subscription status code |
| BILL_FREE_YN | CHAR(1) | N | Billing free yes or no |
| AUTO_PAY_YN | CHAR(1) | N | Auto pay yes or no |
| STR_DT | DATE | N | Start date |
| END_DT | DATE | Y | End date |
| NEXT_BILL_DT | DATE | Y | Next billing date |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_SUBS`
- `FK_TB_SUBS_01`
- `FK_TB_SUBS_02`
- `IX_TB_SUBS_TENANT_ID`
- `IX_TB_SUBS_PLAN_ID`

### 4.11 TB_PAY_TXN

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Payment transaction primary key |
| TENANT_ID | BIGINT | N | Tenant primary key reference |
| SUBS_ID | BIGINT | N | Subscription primary key reference |
| PAY_TP_CD | VARCHAR(20) | N | Payment type code |
| PAY_STS_CD | VARCHAR(20) | N | Payment status code |
| PAY_AMT | DECIMAL(15,2) | N | Payment amount |
| CURR_CD | VARCHAR(10) | N | Currency code |
| PG_CD | VARCHAR(30) | Y | Payment gateway code |
| PG_TXN_NO | VARCHAR(100) | Y | Payment gateway transaction number |
| APRV_DTM | TIMESTAMP | Y | Approved datetime |
| FAIL_MSG | VARCHAR(1000) | Y | Failure message |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_PAY_TXN`
- `FK_TB_PAY_TXN_01`
- `FK_TB_PAY_TXN_02`
- `IX_TB_PAY_TXN_TENANT_ID`
- `IX_TB_PAY_TXN_SUBS_ID`

### 4.12 TB_USR_ROLE

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | User-role mapping primary key |
| USR_ID | BIGINT | N | User primary key reference |
| ROLE_ID | BIGINT | N | Role primary key reference |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_USR_ROLE`
- `UK_TB_USR_ROLE_USR_ID_ROLE_ID`
- `FK_TB_USR_ROLE_01`
- `FK_TB_USR_ROLE_02`
- `IX_TB_USR_ROLE_USR_ID`
- `IX_TB_USR_ROLE_ROLE_ID`

### 4.13 History Table Pattern

Every business table has its own history table, created and maintained by MariaDB triggers.

Common history metadata columns:

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| HIST_ID | BIGINT | N | History primary key |
| HIST_TP | CHAR(1) | N | History type (`I`, `U`, `D`) |
| HIST_DTM | TIMESTAMP | N | History datetime |
| HIST_BY | VARCHAR(100) | N | History actor |

History tables currently in scope:

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

Each history table also stores a full snapshot of the source row at the time of change.

Related trigger examples:

- `TR_TB_TENANT_AI`
- `TR_TB_TENANT_AU`
- `TR_TB_TENANT_BD`
- `TR_TB_CO_AI`
- `TR_TB_CO_AU`
- `TR_TB_CO_BD`
- `TR_TB_DEPT_AI`
- `TR_TB_EMP_AI`
- `TR_TB_MNU_AI`
- `TR_TB_ROLE_MNU_AUTH_AI`
- `TR_TB_SUBS_AI`
- `TR_TB_PAY_TXN_AI`

## 5. Current API Coverage

The current next-gen port exposes the following master APIs:

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
- `GET /api/v1/payment-transactions?tenantId=<id>`
- `POST /api/v1/payment-transactions`

## 6. B2B Authorization and Billing Rules

- Tenant type:
  - `INTERNAL`
  - `CUSTOMER`
- Internal tenant policy:
  - billing free enforced
  - paid payment transaction blocked
- Role depth model:
  - depth 1: `ADMIN`, `CUSTOMER`
  - depth 2 admin: `SUPER_ADMIN`, `NORMAL_ADMIN`
  - depth 2 customer: `USER`, `FINANCE_ADMIN`, `HR_ADMIN`, `ADMIN`
  - depth 3 customer: `TEAM_MEMBER`, `TEAM_LEADER`
- Company hierarchy model:
  - `TB_CO`
  - `TB_DEPT`
  - `TB_EMP`
- Menu visibility is controlled by `TB_ROLE_MNU_AUTH`
- Menu scope is split by `ADMIN` and `CUSTOMER`

### 6.1 TB_ACCESS_LOG

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Access log primary key |
| REQ_ID | VARCHAR(36) | N | Request correlation id |
| TENANT_KEY | VARCHAR(100) | Y | Tenant request key |
| USR_KEY | VARCHAR(255) | Y | User principal key |
| HTTP_MTHD_CD | VARCHAR(10) | N | HTTP method code |
| REQ_URI | VARCHAR(500) | N | Request URI |
| REQ_QS_CNTS | LONGTEXT | Y | Request query string contents |
| REQ_BODY_CNTS | LONGTEXT | Y | Request body contents |
| REQ_HDR_CNTS | LONGTEXT | Y | Request header contents |
| CLNT_IP | VARCHAR(100) | Y | Client IP address |
| RESP_STS_CD | VARCHAR(3) | N | Response status code |
| SUCC_YN | CHAR(1) | N | Success yes or no |
| ERR_MSG | VARCHAR(2000) | Y | Error message |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Indexes:

- `PK_TB_ACCESS_LOG`
- `IX_TB_ACCESS_LOG_REQ_ID`
- `IX_TB_ACCESS_LOG_TENANT_KEY`
- `IX_TB_ACCESS_LOG_USR_KEY`
- `IX_TB_ACCESS_LOG_CRT_DTM`

## 7. Engineering Rule

Any new table or schema change must be aligned with:

- [`02_database_naming_standard.md`](C:\Users\JuyongLee\Sinwoo_AI\docs\02_database_naming_standard.md)
