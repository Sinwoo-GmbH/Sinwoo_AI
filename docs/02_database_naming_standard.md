# SINWOO Database Naming Standard

## 1. Purpose

This document defines the official abbreviation-based naming standard for SINWOO database objects.

## 2. General Principles

- Use uppercase letters only
- Separate words with underscore `_`
- Prefer approved abbreviations over long full names
- Keep the same abbreviation consistently across DB, backend API contracts, and frontend integration types
- Use singular table names

## 3. Table Naming

Format:

```text
TB_<ENTITY>
```

Examples:

- `TB_TENANT`
- `TB_CO`
- `TB_USR`
- `TB_ROLE`
- `TB_MNU`
- `TB_SUBS`
- `TB_PAY_TXN`
- `TB_USR_ROLE`
- `TB_TENANT_HIST`
- `TB_CO_HIST`
- `TB_USR_HIST`

## 4. Column Naming

### 4.1 Core Rules

- Primary key: `ID`
- Foreign key: `<ENTITY>_ID`
- Code: `_CD`
- Name: `_NM`
- Status code: `STS_CD`
- Number: `_NO`
- DateTime: `_DTM`
- JSON snapshot: `_JSON`

### 4.2 Audit Columns

Every business table must append the following columns at the end:

- `CRT_BY`
- `CRT_DTM`
- `UPD_BY`
- `UPD_DTM`

## 5. Constraint Naming

### Primary Key

```text
PK_<TABLE>
```

Example:

- `PK_TB_TENANT`

### Foreign Key

```text
FK_<TABLE>_<SEQ>
```

Examples:

- `FK_TB_CO_01`
- `FK_TB_USR_01`

### Normal Index

```text
IX_<TABLE>_<COLUMN>
```

Examples:

- `IX_TB_CO_TENANT_ID`
- `IX_TB_USR_CO_ID`

### Unique Key

```text
UK_<TABLE>_<COLUMN>
```

Examples:

- `UK_TB_TENANT_TENANT_CD`
- `UK_TB_CO_TENANT_ID_CO_CD`
- `UK_TB_USR_TENANT_ID_LGN_ID`
- `UK_TB_USR_TENANT_ID_EML`

### Trigger

```text
TR_<TABLE>_<EVENT>
```

Examples:

- `TR_TB_TENANT_AI`
- `TR_TB_TENANT_AU`
- `TR_TB_TENANT_BD`

Event codes:

- `AI`: after insert
- `AU`: after update
- `BD`: before delete

## 6. History Table Standard

- Every business table must have its own history table.
- History table format: `TB_<ENTITY>_HIST`
- History is recorded by database triggers, not by application service code.
- Base row snapshot columns should be copied into the history table together with the following metadata:
  - `HIST_ID`
  - `HIST_TP`
  - `HIST_DTM`
  - `HIST_BY`

Examples:

- `TB_TENANT` -> `TB_TENANT_HIST`
- `TB_CO` -> `TB_CO_HIST`
- `TB_USR` -> `TB_USR_HIST`
- `TB_ROLE` -> `TB_ROLE_HIST`
- `TB_USR_ROLE` -> `TB_USR_ROLE_HIST`
- `TB_MNU` -> `TB_MNU_HIST`
- `TB_ROLE_MNU_AUTH` -> `TB_ROLE_MNU_AUTH_HIST`
- `TB_SUBS_PLAN` -> `TB_SUBS_PLAN_HIST`
- `TB_SUBS` -> `TB_SUBS_HIST`
- `TB_PAY_TXN` -> `TB_PAY_TXN_HIST`

## 7. Approved Abbreviation Dictionary

| Full Term | Standard |
| --- | --- |
| TENANT | TENANT |
| COMPANY | CO |
| USER | USR |
| ROLE | ROLE |
| MENU | MNU |
| HISTORY | HIST |
| ACCESS | ACCESS |
| LOG | LOG |
| BILLING | BILL |
| FINANCE | FIN |
| CODE | CD |
| NAME | NM |
| STATUS | STS |
| NUMBER | NO |
| REGISTRATION | REG |
| AUTHORIZATION | AUTH |
| APPROVAL | APRV |
| GROUP | GRP |
| LEVEL | LVL |
| DISPLAY | DSP |
| PASSWORD | PWD |
| HASH | HASH |
| EMAIL | EML |
| LOCALE | LOCL |
| CREATED | CRT |
| UPDATED | UPD |
| DATE TIME | DTM |
| SNAPSHOT | SNAP |
| TABLE | TBL |
| ENTITY | ENT |
| ROW | ROW |
| TYPE | TP |
| SUBSCRIPTION | SUBS |
| PLAN | PLAN |
| PAYMENT | PAY |
| TRANSACTION | TXN |
| CYCLE | CYCL |
| CURRENCY | CURR |
| EXPORT | EXPRT |
| JSON | JSON |
| REQUEST | REQ |
| RESPONSE | RESP |
| HEADER | HDR |
| BODY | BODY |
| CONTENTS | CNTS |
| METHOD | MTHD |
| CLIENT | CLNT |
| ERROR | ERR |
| SUCCESS | SUCC |
| URI | URI |

## 8. API Contract Naming

Backend response/request fields exposed for frontend integration should align with the same standard where it improves consistency.

Examples:

- `tenantCd`
- `tenantNm`
- `coCd`
- `coNm`
- `lgnId`
- `authGrpCd`
- `authLvlCd`
- `tenantTpCd`
- `billFreeYn`
- `roleScopeCd`
- `roleD1Cd`
- `roleD2Cd`
- `roleD3Cd`
- `mnuCd`
- `mnuScopeCd`
- `subsStsCd`
- `payTpCd`
- `payStsCd`
- `stsCd`
- `crtDtm`
- `updDtm`
- `totCnt`
- `itemList`

## 9. Standard Application

All new migration scripts, entity mappings, request/response DTOs, and frontend API types must follow this document.
