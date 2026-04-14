# Source-of-Truth Cleanup Map

This note classifies the hardcoded structures identified during the source-of-truth audit.

Goal:
- reduce frontend hardcoded fallback/demo structures safely
- preserve current login, dashboard boot, workspace shell, menu authorization, and localization behavior
- prepare the codebase for gradual migration toward DB/API-backed source of truth

Classification legend:
- `temporary fallback`: may remain for now to keep boot/login/shell safe when backend data is missing
- `demo/mock only`: should be isolated from real runtime behavior and never act as business truth
- `must be removed`: directly blocks DB/API-driven architecture and should be retired in the cleanup path
- `ui-only metadata`: may remain in frontend/backend config if it is presentation-only and not business truth

## 1. Menu Hardcoding

### `frontend/lib/workspace/platform-shell-data.ts`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| `baseWorkspaceModes.menus` | temporary fallback -> must be removed later | protects shell boot when menu API is unavailable, but duplicates real menu tree | menu API | keep only minimum boot-safe fallback, then shrink aggressively |
| `defaultTabId` values | temporary fallback | needed for first render and tab restore safety | menu API + per-mode policy | keep until menu API always returns a guaranteed home/default menu |
| `mobileQuickMenus` | temporary fallback | current mobile shell depends on it, but it duplicates menu structure | menu API or menu policy table | replace with API-provided quick-entry metadata when available |
| menu titles inside fallback tree | must be removed | DB menu names already exist; title duplication causes drift | menu API | fallback tree should use ids only, or a minimum emergency label set |
| report menu ids like `MNU_ADMIN_REPORTS` / `MNU_CUSTOMER_REPORTS` | temporary fallback | currently safe because they align with DB ids | menu API | keep only if boot path still requires them |

### `frontend/components/workspace/platform-shell.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| `LEGACY_MENU_ID_ALIASES` | must be removed | this is transitional glue that lets legacy ids coexist with DB ids; long-term it keeps the model inconsistent | menu API | remove after all persisted/open tab ids are normalized to DB menu ids |
| `REVERSE_LEGACY_MENU_ID_ALIASES` | must be removed | same reason as above | menu API | retire together with legacy alias map |
| `iconMap` | ui-only metadata | icons are presentation metadata, not business data | menu API may provide icon name; frontend keeps renderer map | acceptable to keep as frontend registry if API only provides icon key |

### `src/main/java/com/sinwoo/menu/service/MenuServiceImpl.java`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| scope codes `ADMIN`, `CUSTOMER`, `COMMON` | can remain as UI/backend integration constants for now | these are contract-level identifiers already used in API and DB | menu API / menu policy table | acceptable until a dedicated scope code table is introduced |
| billing gate code `PAID_CUSTOMER_ADMIN` | must be removed from service literals later | gate logic belongs to billing/menu policy, not service string comparisons | policy table | replace with policy-driven lookup when billing/menu policy expands |
| `"Y"` / `"N"` checks | can remain temporarily | DB convention is currently Y/N | common code table or entity helpers | low urgency unless boolean normalization starts |

### `src/main/java/com/sinwoo/menu/controller/MenuController.java`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| locale normalization (`ko`, `de`, default `en`) | can remain as backend config-driven logic | not business data, but should not spread further | backend config | centralize only if more controllers start duplicating it |

## 2. Localization Hardcoding

### `frontend/components/workspace/platform-shell.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| `uiMessages` | must be removed | workspace labels live inside a component and are acting as scattered source of truth | i18n file | move to shared workspace i18n module |
| `tabContextMenuLabels` | must be removed | same labels are UI text, not component-local business data | i18n file | merge into shared workspace i18n map |

### `frontend/components/workspace/workspace-attendance-card.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| `attendanceMessages` | must be removed | component-local localization fragments the UI text system | i18n file | move to attendance/workspace i18n module |
| weekday labels | can remain temporarily, then move | pure UI labels, but should not stay inside feature component | i18n file | centralize with attendance i18n |

### `frontend/components/workspace/workspace-work-time-history-page.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| `uiText` | must be removed | report page text is page-local and duplicates the localization pattern | i18n file | move to shared reports/workspace i18n module |

### `frontend/app/auth/callback/page.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| callback page labels | must be removed | currently English-skewed and outside the shared localization path | i18n file | align with login/auth i18n |

### `frontend/lib/i18n/login-content.ts`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| login page text maps | can remain | this is already the correct pattern for UI-only text | i18n file | use this as the reference pattern for workspace/report text cleanup |
| `LOGIN_LOCALES` | can remain | supported locales are app-level UI config, not DB business data | i18n file or backend config | acceptable if kept centralized |

## 3. Select / Combo Hardcoding

### `frontend/lib/i18n/login-content.ts`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| locale option list (`de`, `en`, `ko`) | can remain | locale availability is app config, not business master data | backend config or centralized i18n config | only move if tenant-specific locale policy is introduced |

### `frontend/components/workspace/workspace-attendance-card.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| default times `09:00`, `18:00` | must be removed | these are acting like work policy defaults | policy table | use company/work-location policy when available |
| month navigation label timezone assumptions | must be removed | current rendering assumes Berlin business context | policy table | use HQ/work-location timezone from backend |

### `frontend/components/common/locale-combobox.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| locale option rendering | can remain | rendering layer is UI-only | centralized i18n config | safe to keep if options are injected from one shared source |

## 4. Business Constants

### `frontend/lib/api/attendance-contract.ts`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| `attndStsCd` literal union (`CHECKED_IN`, `CHECKED_OUT`, `LEAVE`, `BUSINESS_TRIP`) | must be removed | common code values are being duplicated in TS types and can drift from DB-managed flags | common code table | switch to backend contract-driven string with shared code constraints later |

### `frontend/components/workspace/workspace-attendance-card.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| status checks for `LEAVE`, `BUSINESS_TRIP`, `CHECKED_IN`, `CHECKED_OUT` | must be removed | frontend is treating status literals as business truth | common code table | keep only if mapped through a shared code adapter, not raw literals |
| timezone `Europe/Berlin` | must be removed | business date/time should come from company/work-location context | policy table | replace with backend-provided timezone/business date context |

### `src/main/java/com/sinwoo/auth/service/AuthServiceImpl.java`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| user status `ACTIVE` | should move later | user state is business code data | common code table | low-risk to defer, but should align with code table strategy |
| provider/auth group codes (`SINWOO`, `OAUTH`, `INTERNAL`) | can remain temporarily | some are integration/config identifiers, some are business codes | common code table or backend config | separate true business codes from environment/config constants in a later pass |
| generated OAuth password prefix `OAUTH2-` | ui-only/config metadata | implementation detail, not business truth | backend config | safe to keep |

### `src/main/java/com/sinwoo/common/config/LocaleConfig.java`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| supported locales + default locale | can remain | application config, not DB business data | backend config | keep centralized here |

### `src/main/java/com/sinwoo/common/security/SecurityConfig.java`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| localhost origins list | can remain | local dev config | backend config | acceptable in config layer |
| `permitAll` path list | can remain | security routing policy belongs to backend config/code | backend config | not a DB target |

## 5. Mock / Demo Data

### `frontend/lib/workspace/platform-shell-data.ts`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| `baseViewModels` | demo/mock only -> must be isolated | dashboard/report cards use static KPI and activity data that looks like real product truth | backend API | keep only as clearly named mock scaffold or remove from runtime path |
| `viewTranslations` | temporary fallback | titles/descriptions are currently coupled to mock view models | i18n file + backend API | separate UI labels from data payloads |
| `modeTranslations` | temporary fallback | shell-level labels are UI text, but not DB data | i18n file | move out of this file to reduce mixed responsibilities |
| placeholder/shadow menu ids | must be removed | transitional placeholders pollute the menu model | menu API | remove after menu API coverage is complete |

### `frontend/components/workspace/platform-shell.tsx`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| `DEMO_USER_KEY` | must be removed | local storage key currently depends on a demo identity, not the actual authenticated user | backend API | replace with current user id/email from `sinwoo.currentUser` |

### `frontend/lib/checkpoint-data.ts`

| Structure | Classification | Why | Target source of truth | Cleanup note |
| --- | --- | --- | --- | --- |
| first-tenant/first-company/first-user selection logic | demo/mock only | this acts like a fake current context | backend API | isolate as development-only checkpoint utility or retire |
| localhost API base fallback | can remain | dev convenience, not business truth | backend config | safe in non-production helper code |

## 6. Safe Cleanup Order

1. Normalize runtime identifiers
   - remove dependency on legacy/local menu ids in persisted tab state
   - replace `DEMO_USER_KEY` with authenticated user-based storage keys

2. Shrink menu fallback
   - keep only minimum boot-safe fallback ids
   - remove titles and nested structure once menu API is always available for logged-in users

3. Move workspace/report text to shared i18n
   - migrate `uiMessages`, `attendanceMessages`, `uiText`, `tabContextMenuLabels`
   - keep `login-content.ts` as the reference pattern

4. Separate mock view content from runtime content
   - isolate `baseViewModels` into explicit development/demo scaffolding
   - stop using static KPI/list values as real content source

5. Replace business literals with code/policy driven values
   - attendance status flags from common code table
   - work timezone/default work times from policy table
   - menu gate/scope hardcoded strings from policy/config where appropriate

## 7. Intentionally Deferred

These are not immediate cleanup targets because removing them now would risk breaking current behavior:

- `SecurityConfig` route authorization declarations
- `LocaleConfig` supported locale list
- frontend icon registry (`iconMap`) as a renderer concern
- current menu scope strings until policy abstraction is ready

## 8. Immediate Red Flags

These items are the biggest blockers to DB/API-driven architecture and should be addressed first in future steps:

1. `frontend/lib/workspace/platform-shell-data.ts` fallback menu tree
2. `frontend/lib/workspace/platform-shell-data.ts` static dashboard/report content
3. `frontend/components/workspace/platform-shell.tsx` legacy menu alias maps
4. `frontend/components/workspace/platform-shell.tsx` demo user storage key
5. `frontend/lib/api/attendance-contract.ts` hardcoded attendance flag literals
6. `frontend/components/workspace/workspace-attendance-card.tsx` hardcoded status/timezone/work-time defaults

