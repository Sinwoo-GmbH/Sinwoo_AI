# SINWOO Current Table Specification

## 1. Scope

This document describes the current standard bootstrap schema for SINWOO.

## 2. Migration File

- [`V1__init_schema.sql`](C:\Users\JuyongLee\Sinwoo_AI\src\main\resources\db\migration\V1__init_schema.sql)

## 3. Tables

- `TB_TENANT`
- `TB_CO`
- `TB_USR`
- `TB_ROLE`
- `TB_USR_ROLE`
- `TB_CHG_HIST`

## 4. Table Details

### 4.1 TB_TENANT

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Tenant primary key |
| TENANT_CD | VARCHAR(100) | N | Tenant code |
| TENANT_NM | VARCHAR(255) | N | Tenant name |
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

### 4.3 TB_USR

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | User primary key |
| TENANT_ID | BIGINT | N | Tenant primary key reference |
| CO_ID | BIGINT | Y | Company primary key reference |
| EML | VARCHAR(255) | N | User email |
| PWD_HASH | VARCHAR(255) | N | Password hash |
| DSP_NM | VARCHAR(255) | N | Display name |
| LOCL_CD | VARCHAR(10) | N | Locale code |
| STS_CD | VARCHAR(20) | N | Status code |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_USR`
- `UK_TB_USR_EML`
- `FK_TB_USR_01`
- `FK_TB_USR_02`
- `IX_TB_USR_TENANT_ID`
- `IX_TB_USR_CO_ID`

### 4.4 TB_ROLE

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Role primary key |
| ROLE_CD | VARCHAR(100) | N | Role code |
| ROLE_NM | VARCHAR(255) | N | Role name |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Constraints / Indexes:

- `PK_TB_ROLE`
- `UK_TB_ROLE_ROLE_CD`

### 4.5 TB_USR_ROLE

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

### 4.6 TB_CHG_HIST

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| ID | BIGINT | N | Change history primary key |
| TBL_NM | VARCHAR(100) | N | Changed table name |
| ENT_NM | VARCHAR(100) | N | Changed entity name |
| ROW_ID | BIGINT | N | Changed row primary key |
| CHG_TP | VARCHAR(20) | N | Change type |
| TENANT_ID | BIGINT | Y | Tenant primary key reference |
| SNAP_JSON | LONGTEXT | N | Serialized entity snapshot |
| CHG_BY | VARCHAR(100) | N | Change actor |
| CHG_DTM | TIMESTAMP | N | Change datetime |
| CRT_BY | VARCHAR(100) | N | Created by |
| CRT_DTM | TIMESTAMP | N | Created datetime |
| UPD_BY | VARCHAR(100) | N | Updated by |
| UPD_DTM | TIMESTAMP | N | Updated datetime |

Indexes:

- `PK_TB_CHG_HIST`
- `IX_TB_CHG_HIST_ENT_NM_ROW_ID`
- `IX_TB_CHG_HIST_TENANT_ID`
- `IX_TB_CHG_HIST_CHG_DTM`

## 5. Engineering Rule

Any new table or schema change must be aligned with:

- [`02_database_naming_standard.md`](C:\Users\JuyongLee\Sinwoo_AI\docs\02_database_naming_standard.md)
