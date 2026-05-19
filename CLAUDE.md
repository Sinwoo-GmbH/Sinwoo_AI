# Sinwoo AI — to-be 프로젝트 가이드

> 독일 소규모 한인 업체 대상 북키핑 SaaS의 차세대 백엔드/프론트엔드.
> as-is(`Sinwoo.git`, Private) → to-be(`Sinwoo_AI.git`, Public) 마이그레이션 진행 중.

---

## 프로젝트 개요
- **개발자**: 1인 (20년 경력, 독일 현지)
- **상태**: 시범 서비스 (무료)
- **as-is 위치**: `C:\Users\JuyongLee\Desktop\office\Sinwoo-New\sinwoo\` (읽기 전용)
- **to-be 위치**: `C:\Users\JuyongLee\Sinwoo_AI\`

## 스택

| | as-is | to-be |
|--|-------|-------|
| Java | 17 | **21** |
| 빌드 | Maven, WAR | **Gradle**, JAR |
| 프레임워크 | Spring Boot 3.3 | Spring Boot 3.3 |
| ORM | MyBatis + XML | **Spring Data JPA** (완전 전환) |
| 마이그레이션 | 없음 | **Flyway** |
| 인증 | 세션 | **JWT + Spring Security** |
| 캐시 | Redis | **제거** |
| 스토리지 | AWS S3 + Textract | **AWS S3 + Claude Vision** |
| OCR | AWS Textract | **Claude Vision** |
| 프론트 | Thymeleaf + jQuery | **Next.js + Tailwind + shadcn** |
| DB | MariaDB (AWS EC2 52.58.145.241) | MariaDB (로컬 Docker) |
| 인프라 | EC2 Tomcat | **AWS (처음부터)** |

## 작업 원칙
- **as-is는 읽기만**. 절대 수정 금지.
- 작업 단위 **작게 쪼개서 검증**하면서 진행.
- 순서: as-is 분석 → 테이블 설계 → 백엔드 → UI
- as-is 범위 외 파일 건드리지 말 것.
- 작업 후 빌드 + 서버 기동 검증 필수.
- **시키지 않은 건 하지 말 것.** 지시한 범위만 정확히 수행.
- **이력 기록 의무**: 코드 추가/수정/삭제 발생 시 **반드시 `CLAUDE.md`에 이력 기록**. 변경한 파일, 메서드/함수, 변경 사유, 핵심 로직을 도메인 "진행 현황" 섹션에 추가. 빠뜨리면 다음 세션에서 컨텍스트 손실 발생.

---

## 개발 정책 (필수 준수 — 별도 지시 없어도 모든 작업에 무조건 적용)

### ID 필드
- **모든 비즈니스 ID 필드**(TENANT_ID, CO_ID, EMP_ID 등)는 **영문+숫자 조합**, 타입 **VARCHAR(20)** 고정.
- DB 컬럼, JPA Entity, DTO 전부 `String` 타입으로 통일.
- PK(ID)는 BIGINT AUTO_INCREMENT — 비즈니스 식별자와 혼동 금지.

### 백엔드 DTO 구조
- **Controller 1개당 Request record 1개, Response record 1개**. 파일도 각 1개씩만.
- 내부에 필요한 하위 구조는 **같은 파일 안에 nested record**로 선언.
- 예: `LeaveRequest.java` 안에 `LeaveRequest`, `LeaveRequest.AprvStep`, `LeaveRequest.Calc` 등 전부 포함.
- 예: `LeaveResponse.java` 안에 `LeaveResponse`, `LeaveResponse.Item`, `LeaveResponse.Balance`, `LeaveResponse.Context` 등 전부 포함.
- DTO 파일을 도메인별로 산만하게 쪼개지 말 것.
- ListResponse 계열 record의 리스트 필드명은 **`itemList`** (절대 `items` 아님).
- 프론트엔드 contract도 동일하게 `itemList` 사용.

### 공통코드 (TB_CD_GRP / TB_CD) — 모든 테이블의 `*_CD` 컬럼에 일괄 적용
- **모든 CD 값은 영문 대문자 2~4자리, 언더바(`_`) 금지**. 예외 없음.
- 적용 대상: `ROLE_CD`, `ROLE_SCOPE_CD`, `ROLE_D1_CD`, `ROLE_D2_CD`, `ROLE_GRP_CD`, `ROLE_LVL_CD`, `MNU_CD`, `MNU_SCOPE_CD`, `BILL_GATE_CD`, `LEAVE_TYPE_CD`, `LEAVE_UNIT_CD`, `DEDUCTION_TYPE_CD`, `LEAVE_STATUS_CD`, `REQ_TP_CD`, `GRANT_TIMING_CD`, `GRANT_TYPE_CD`, `EXPIRE_ACTION_CD`, `HTTP_MTHD_CD` 등 **`*_CD`로 끝나는 모든 컬럼**.
- 긴 단어 약어 표준:
  - `PLATFORM`→`PLT`, `CUSTOMER`→`CST`, `ADMIN`→`ADM`
  - `LEAVE`→`LV`, `TRIP`→`TR`, `EXPENSE`→`EX`
  - `WORK_TIME`→`WT`, `POLICY`→`PLC`, `GRANT`→`GR`
  - `FISCAL`→`FSC`, `HIRE`→`HIR`, `FLAT`→`FLT`, `TIERED`→`TRD`
  - `AUTO_ZERO`→`ZERO`, `CARRY`→`CARY`
- 합성 CD도 언더바 없이 붙임: 예) `MYWT` (My Working Time), `TMWT` (Team Working Time), `RQLV` (Request Leave), `LVPL` (Leave Policy), `LVGR` (Leave Grants), `COHL` (Company Holidays), `PCST` (Paid Customer Admin gate).
- 이미 짧은 기존 CD 값(`PADM`, `CADM`, `HR`, `FI`, `USR`, `AN`, `SK`, `FD`, `AM`, `PM`, `DD`, `ND`, `DRF`, `REQ`, `WAT`, `APR`, `REJ`, `CAN` 등)은 그대로 유지.
- GRP_CD(공통코드 그룹)는 **언더스코어 허용** (예: `VAC_TP`, `APRV_STS`) — CD 값과 다른 규칙.
- 새 CD 추가 시 **반드시 4자 이내 + 언더바 없음** 확인. 길거나 언더바 들어가면 즉시 줄임 검토.
- 백엔드 비교문자열, 프론트 alias 매핑(`*_CD_ALIAS`) 모두 이 규칙 따라야 함.
- **CD 값은 모든 테이블/모든 컬럼에 걸쳐 글로벌 유니크**. 동일 문자열이 두 군데 이상 의미가 다르게 쓰이면 안 됨 (예: `PADM`을 ROLE_CD에 쓰면 다른 테이블에서 같은 의미가 아닌 한 사용 금지).
- **충돌 시 예외**: 4자로 유니크 확보 불가하면 **최대 6자까지 허용**. 예) `ADMIN` (Admin 메뉴 코드 — `ADM` 은 SCOPE_CD에 이미 사용).
- 단, **같은 의미를 표현하는 경우는 공유 허용**: 예) `PLT`/`CST`/`ADM` 은 ROLE_SCOPE_CD / MNU_SCOPE_CD / ROLE_D1_CD 등에서 모두 "스코프"라는 동일 의미로 쓰이므로 OK.
- **MNU_NM_CD (i18n 번역 키)**: dotted path 표기 유지하되 각 segment는 위 약어 규칙 따름. 예) `MNU.CUSTOMER.ADMIN.LEAVE_POLICY` → `MNU.CST.ADM.LVPL`.
- **ROLE_LVL_CD**: 숫자 문자열 (`'100'`/`'50'`/`'30'`/`'20'`/`'10'`) 그대로 유지 — 정렬 가능성 때문에 예외.

### 결재선
- **TB_APRV_LINE은 통합 결재선** — REQ_TP_CD(LEAVE/TRIP/EXPENSE 등)로 도메인 구분.
- 도메인별 결재 테이블 따로 만들지 않음.

### 날짜 범위 유효성 (공통 — 모든 도메인 적용)
- **From/To(시작일/종료일) 쌍이 있는 곳은 반드시 From <= To 유효성 체크**.
- 프론트: 입력 시 자동 보정(Start > End이면 End를 Start로 맞춤) + Submit 시 유효성 에러 표시.
- 백엔드: Service 레이어에서도 동일 검증 (프론트만 믿지 말 것).
- 적용 대상: 휴가 신청, 출장 신청, 출퇴근 조회, 경비 조회, 필터 검색 등 **기간 입력이 있는 모든 곳**.

### 영업일 계산 (공통 — 모든 일수 계산 적용)
- 일수 계산은 **영업일(Business Day) 기준**. 단순 캘린더 일수가 아님.
- 영업일 = **실제 근무하는 날만 카운트**. 아래 3가지 모두 제외:
  1. **주말** (토요일, 일요일)
  2. **지역 공휴일** (TB_RGN_HOL — 독일 주(州)별 공휴일, Nager.Date API 동기화)
  3. **회사 자체 휴일** (TB_CO_HOL — 회사가 직접 등록한 창립기념일 등)
- 예시: 월~금 5일인데 수요일이 공휴일이면 → 영업일 **4일**.
- **프론트**: 즉시 프리뷰용으로 주말(토/일)만 제외하여 계산 (공휴일 데이터 없으므로 근사값).
- **백엔드**: 주말 + 지역공휴일 + 회사휴일 전부 제외한 **최종 정확한 일수** 산출. **서버 계산이 정답**.
- 백엔드 계산 엔드포인트: `/api/v1/leaves/calculate` (POST).

### 폼 유효성 검사 (공통 — 모든 입력 화면 적용)
- **필수 필드에는 반드시 `*` 마커** 표시 (라벨 옆 빨간색 asterisk).
- Submit 시 필수값 누락이면:
  1. **toast.error()** 로 "필수 항목을 확인해주세요" 알림 (화면 상단 토스트).
  2. 해당 필드에 **빨간 보더 + 필드 아래 에러 메시지** 표시.
  3. Submit **차단** (서버 요청 보내지 않음).
- 필드 수정 시 해당 필드의 에러는 **실시간 클리어**.
- 임시저장(Save Draft)은 유효성 검사 없이 그대로 저장.
- 브라우저 기본 유효성(HTML5 required 등)에 의존하지 말고, **커스텀 유효성 로직** 사용.

### 다국어 (ko/en/de) — 모든 UI 텍스트 적용
- **모든 화면의 모든 텍스트**는 다국어 LABELS 객체 기반. 하드코딩된 한국어/영어/독일어 문자열 금지.
- i18n 파일 위치: `frontend/lib/i18n/` (도메인별 `*-cnt.ts` 파일).
- `LoginLocale` 타입(`"ko" | "en" | "de"`)을 최상위 페이지에서 받아 **모든 하위 컴포넌트에 `locale` prop으로 전파**.
- **Combobox, Select, Radio, Chip 등 옵션 UI**:
  - 내부 value는 영문 코드(예: `"Annual Leave"`, `"DRF"`) 유지.
  - **화면에 보이는 텍스트는 사용자 언어에 맞는 description** 표시 (예: ko→"연차", de→"Jahresurlaub").
  - `leaveTypeLabel(locale, cd)`, `deductionTypeLabel(locale, cd)` 같은 함수로 변환.
- **테이블/그리드**:
  - 헤더(No, 휴가유형, 상태 등), 액션 버튼(수정, 조회, 승인 등), 빈 데이터 메시지, 페이징 라벨 전부 다국어.
- **상태 배지**: `LeaveStatusBadge`, `LeaveApproverStatusBadge` 등 배지 텍스트도 `locale` 받아서 다국어 표시.
- **다이얼로그/모달**: 제목, 설명, 버튼 텍스트, placeholder 전부 다국어.
- **필터 바**: 라벨(시작일, 상태 등), 버튼(검색, 생성) 다국어.
- **잔여일수 요약**: "잔여일수", "신청 후" 등 라벨 다국어.

### Soft Delete
- 모든 도메인 테이블에 `DEL_YN CHAR(1) DEFAULT 'N'` 사용.
- 물리 삭제 금지, UPDATE SET DEL_YN='Y' 처리.

### Flyway
- **이미 적용된 마이그레이션 파일은 절대 수정 금지**. 수정 필요 시 새 버전(V+1) ALTER 마이그레이션 생성.
- 수정하면 checksum 불일치로 서버 기동 실패함.

### UI/UX 공통
- **엔터프라이즈급 디자인 유지**. 브라우저 기본 UI(alert, confirm, prompt) 절대 사용 금지.
- 모든 확인/경고 다이얼로그는 커스텀 컴포넌트 사용:
  - 공통: `@/components/ui/confirm-dialog`
  - 도메인별: `LeaveConfirmDialog` 등
- 토스트 알림: `@/components/ui/toast`의 `toast.success()`, `toast.error()`, `toast.info()` 사용.
- 색상 기준: 기본 액션 `#23468F`, 위험/삭제 `#C53030`, 배경 `white`, 텍스트 `slate` 계열.
- 애니메이션: fade + scale 트랜지션 (200ms). 과하지 않게.
- 장난스럽거나 유치한 디자인 요소 금지 — 40억짜리 프로젝트에 맞는 품격.

---

## 완료된 인프라 정비 (2026-05-12)

### 백엔드
- **Phase 1**: Redis/MinIO 잔재 제거, 빈 마커 클래스 10개 삭제, 패키지명 일관화(`dept`→`department`, `emp`→`employee`, `mnu`→`menu`)
- **Phase 2**: `SecurityConfig.permitAll()` 정리(허용 11개로 축소), `JwtAuthFilt` silent fail 로그, CORS origin 환경변수화
- **Phase 3**: 공통 헬퍼 추출
  - `common/util/StringNormalizer` — `blankToNull`, `blankToNullUpper`, `trimAndUpper`, `defaultIfBlankUpper`, `normalizeYn`
  - `platform/support/PlatformRefValidator` — `requireTenantExists`, `requireCoInTenant`, `requireOptionalCoInTenant`
- **Phase 4a**: `AuditorAwareConfig` + `BaseEntity` `@CreatedBy`/`@LastModifiedBy` — CRT_BY/UPD_BY 자동 채움
- **Phase 4b**: 예외 통일 ⏭️ skip (`ApiException`=에러코드 필요 시, `ResponseStatusException`=단순 검증 — 역할 분리)
- **Phase 4c**: `TenantCtx` JWT 기반 재설계 (헤더 폐기) — `tenantId` + `tenantCd` 둘 다 보관
- **Phase 5a**: MDC 확장 — `[reqId]` + `[tenant]` + `[usr]`
- **Phase 5b**: `AuthenticatedUsr.resolveKey()` 통합 — **eml 우선** (CRT_BY/UPD_BY/USR_KEY에 이메일 박힘)
- **Phase 5c**: i18n 인프라(LocaleConfig 이미 존재) + 공통 에러 메시지 시드(ko/en/de)

### 프론트엔드
- **F1**: `components/features/` 빈 폴더 삭제
- **F2**: `features/common/business-module/` 빈 폴더 삭제 (`biz-mod`로 통일)
- **F3**: 빈 라우트 폴더 유지 (도메인 작업 계획 가시화용)
- **F4**: `lib/auth/security/` 평탄화 → `lib/auth/`

### 시드 데이터
| 테이블 | 값 |
|--------|-----|
| TB_TENANT | id=1, SINWOO / Sinwoo International / INTERNAL / BILL_FREE |
| TB_CO | id=1, SINWOO / Sinwoo |
| TB_DEPT | id=1, HQ / Head Office |
| TB_USR | id=1, GGAMGANG / **ggamgang@sinwoo-itc.com** / 이주용 / ko / ADMIN |
| TB_EMP | id=1, EMP001 / 이주용 / TEAM_LEADER / 부장 |
| TB_USR_ROLE | usr=1 ↔ PADM (V19에서 마이그레이션됨) |

**로그인 계정**: `ggamgang@sinwoo-itc.com` / `1234`

> ⚠️ to-be `TB_EMP` 스키마에 **성별/지역 컬럼 없음**. as-is `tb_employee_master`에는 SEX_CD, REGION 있었음. 직원 도메인 마이그레이션 시 결정 필요.

---

## 휴가 도메인 (Request Leave) 진행 현황

### 완료
- V16~V18 Flyway (TB_LEAVE_REQ + TB_APRV_LINE 생성, VARCHAR(20) 전환)
- Entity: `LeaveReq`, `AprvLine`
- Repository: `LeaveReqRepository`, `AprvLineRepository`
- DTO: `LeaveRequest.java` (nested: AprvStep, Calc), `LeaveResponse.java` (nested: Item, ItemList, Balance, Applicant, Part, Org, AprvStep, Context, CalcResult, Dup)
- Service: `LeaveReqService` 인터페이스 + `LeaveReqServiceImpl` (getContext, getMyLeaves, getLeave, createLeave, updateLeave, deleteLeave, cancelLeave, calculateDays)
- Controller: `LeaveReqController` (`/api/v1/leaves`)
- 프론트엔드: 모달 유효성 검사 (필수 `*` 마커, toast, 필드별 에러)
- 프론트엔드: 영업일 기준 일수 계산 (주말 제외 — 즉시 프리뷰용 근사값)
- 프론트엔드: 전체 다국어 (ko/en/de) — 모달, 테이블, 필터, 배지, 잔여일수, 다이얼로그
- 프론트엔드: i18n 파일 `frontend/lib/i18n/leave-cnt.ts`
- 프론트엔드: `LeaveReqModal.onCalculate` prop — 날짜/유형/단위 변경 시 400ms 디바운스 후 `POST /api/v1/leaves/calculate` 호출. 서버 응답의 `days`/`afterRequestDays` 우선 표시, 로딩 전까지 로컬 프리뷰 fallback.
- 프론트엔드: Draft 행 **삭제** 버튼 — 빨간색 Trash2 아이콘, `LeaveConfirmDialog` 확인 후 `DELETE /api/v1/leaves/{id}` 호출. i18n(actDelete, confirmDeleteTitle/Desc/Btn) 완비.
- 프론트엔드: 모달 헤더 드래그 — `mousedown`/`mousemove`/`mouseup` + `transform: translate(x,y)`. 헤더 `cursor-move`, `select-none`. 모달 재오픈 시 위치 리셋.
- **휴가신청 모달 회귀 fix + 휴가단위 기본값 처리** — `leave-mock-data.ts`, `leave-req-modal.tsx`:
  1. **`calculateLeaveDays` 양쪽 표기 인식** — leaveUnit이 "Full Day"(풀네임) 또는 "FD"(짧은 CD) 모두 종일로 인식. 백엔드가 짧은 CD `"FD"`로 옵션을 응답하는 환경에서 칩 선택 시 leaveUnit="FD" → 기존 비교 실패로 0.5만 반환되던 회귀 해결. **이걸로 #1(일수 계산 0만 나옴) + #2(신청 후 일수 오류) 동시 해결** — afterRequestDays는 days를 입력으로 받으므로 days가 정확해지면 자동으로 정합.
  2. **모달 진입 시 leaveUnit 자동 보정** — useEffect에서 `initialValue.leaveUnit`이 백엔드 옵션(`leaveUnitOpts`)에 없으면 `allowedLeaveUnits(leaveType, leaveUnitOpts)`의 첫 번째 값(종일=Full Day/FD)으로 자동 세팅. Create 모드 디폴트 `"Full Day"` 풀네임이 백엔드 옵션 `["FD","AM","PM"]`에 없어 칩 미활성 상태였던 문제 해결.
  3. **휴가유형 변경 시 휴가단위 강제 리셋** — `updateField("leaveType",...)`에서 무조건 첫 허용 옵션(=종일)으로 초기화. 기존 "허용 목록에 없을 때만 보정" 조건 제거 — 사용자 요구: 휴가유형 변경 = 휴가단위 종일 기본.
- **휴가신청 모달 폼 정책 추가 변경** — `leave-req-modal.tsx`, `leave-req-page.tsx`, `leave-mock-data.ts`, `LeaveReqServiceImpl.java`:
  1. **사유 필수 검사 전면 제거** — validate에서 reason 체크 삭제, FieldLabel `required` prop 제거. 사유는 항상 선택 입력.
  2. **신청 후 휴가일수(afterRequestDays) 차감 인식 양쪽 표기 지원** — `calculateAfterRequest`(프론트) + `calculateDays`(백엔드) 둘 다 deductionType이 "Deducted Leave"(풀네임) 또는 "DD"(짧은 CD)일 때 차감. 기존엔 한쪽만 비교 → 백엔드가 짧은 CD 보내는 환경에서 차감 미동작 문제 해결.
  3. **임시저장(Save Draft) 후 모달 유지** — `handleSave`에서 `nextStatus==="Draft"`이면 `closeDialog()` 호출 안 하고, 서버 응답 record로 모달 폼 동기화(`mode:"edit"`, id 등 채워짐) 후 toast로 알림. 사용자가 계속 입력 가능.
  4. **임시저장 toast 알림** — `toast.success(L.toastDraftSaved)` 호출. i18n: ko="임시저장 되었습니다" / en="Draft saved" / de="Als Entwurf gespeichert".
- **휴가신청 모달 폼 정책 정리** — `leave-req-modal.tsx`:
  1. 차감유형 자동 계산: 휴가유형 변경 시 `autoDeductionType(leaveType)`로 자동 설정 (Annual Leave=Deducted, 나머지=Non-deducted). ChoiceChipGroup 버튼 → `ReadOnlyField`로 변경.
  2. 휴가 단위 옵션 동적: `allowedLeaveUnits<LeaveUnit>(leaveType, leaveUnitOpts)` — Annual Leave/"AN"만 Half Day AM/PM 노출, 나머지는 Full Day만. **백엔드가 짧은 CD("FD"/"AM"/"PM")로 옵션을 보내는 점을 고려해 헬퍼 내부에서 양쪽 표기(`FULL_DAY_ALIASES`/`HALF_DAY_ALIASES`)를 모두 인식**하고 `availableOpts` 인자를 받아 백엔드 옵션 그대로 필터링. 휴가유형 변경 시 leaveUnit이 허용 외이면 첫 번째 허용 옵션으로 자동 보정.
  3. 시작일/종료일 HTML5 유효성: `<input type=date>`에 `max={endDate}` / `min={startDate}` 속성 추가. 기존 onChange auto-correct + submit validate(`valEndDateAfterStart`) 유지.
  4. 사유 필수 조건 확장: 기존 Special Leave 외에 **차감유형=Non-deducted Leave(ND)** 일 때도 필수. `FieldLabel` `*` 마커도 동적.
  5. 삭제 버튼: 기존 `canDelete` prop 그대로 — Edit 모드 + id + onDelete + canDelete=true 시 좌측 하단 빨간 Delete 버튼 노출.
  - 헬퍼: `leave-mock-data.ts`에 `autoDeductionType()` / `allowedLeaveUnits()` 추가.
- **삭제(canDelete) 정책 강화** — 삭제 가능 조건을 (Draft) OR (No Approver + 휴가 시작일 전 + 활성상태)로 확장. 백엔드 `LeaveReqServiceImpl.isDeletable()` 로직 추가. 응답 `LeaveResponse.Item.canDelete` 필드 신설 — `toItem()`에서 isOwner + 조건 만족 시 true. 삭제 시 결재선도 함께 soft delete. 본인 신청 건이 아니면 403. 프론트는 `LeaveReqRec.canDelete` 기반으로:
  - 테이블 Delete 버튼 활성화 (Draft 외에 No-Approver-자동승인 건도 시작일 전이면 표시)
  - Edit 모달의 Delete 버튼 노출 (`canDelete` prop 추가)
  - 잔여일수 자동 복구는 `calcBalance()` 실제 잔여일수 로직 구현 시 자동 반영 — DEL_YN='N' 조건만 합산하면 자동.
- **백엔드: 승인(confirm) 엔드포인트** — `POST /api/v1/leaves/{id}/confirm` + `confirmLeave(usr, leaveId)`. 결재자 본인 AprvLine을 WAT→APR로 변경, 모든 결재 라인 APR이면 LeaveReq를 APR로 전이. 결재자 아니거나 WAT 상태 아니면 403.
- **백엔드: 반려(reject) 엔드포인트** — `POST /api/v1/leaves/{id}/reject` + `rejectLeave(usr, leaveId, rejectReason)`. DTO `LeaveRequest.Reject(rejectReason)` 추가. 결재자 본인 라인 REJ + LeaveReq.reject("REJ", reason) — REJ_REASON 컬럼에 사유 저장. 빈 사유는 400.
- **프론트엔드: cancel 메서드 PATCH→POST 통일** — 백엔드(`POST /{id}/cancel`)와 일치.
- **프론트엔드: i18n CD 별칭 매핑** — 백엔드가 DB CD값(짧은 코드 "AN"/"DD"/"FD"/"DRF" 등)을 응답으로 보내도 라벨 함수가 desc로 변환. `leaveTypeLabel`, `deductionTypeLabel`, `leaveUnitLabel`, `leaveStatusLabel` 각각에 `*_CD_ALIAS` 매핑 추가. 영문 풀네임("Annual Leave")과 짧은 CD("AN") 모두 동일 라벨 반환.
- **프론트엔드: Edit 모달 내부 Delete 버튼** — `LeaveReqModal.onDelete?` prop. Edit 모드 + 저장된 id 있을 때만 좌측 하단 빨간 버튼 노출. 클릭 → 별도 빨간색 큰 확인 다이얼로그(타이틀 16px, 본문 14px, 버튼 13px, 휴지통 아이콘, #C53030) → 확인 시 페이지의 `DELETE /api/v1/leaves/{id}` 호출 → 모달 닫기 + 목록 갱신.
- **백엔드: 영업일 계산 (주말 + 지역공휴일 + 회사휴일 제외)** — `LeaveReqServiceImpl.calcBusinessDays(usr, emp, co, leaveUnit, strDt, endDt)`:
  - `loadExcludedDays()`로 기간 내 제외 일자 Set 구성
    1. 주말: `DayOfWeek.SATURDAY/SUNDAY`
    2. 지역 공휴일: `RgnHolRepository.findAllByHolidayDtBetweenAndRegionCdIn(from, to, regions)` — 부서 `REGION_CD` + `"ALL"` 둘 다 조회
    3. 회사 휴일: `CoHolRepository.findAllActiveByPeriod(tenantId, coId, yr, from, to)` 연도 루프 + 일자 펼치기 (annualYn='Y' 매년 반복 포함)
  - Half Day(HA/HP): 해당 1일이 영업일이면 0.5, 아니면 0
  - 호출부 3곳(createLeave / updateLeave / calculateDays) 모두 새 메서드 사용
  - 시드 검증: TB_DEPT id=1 REGION_CD='HE', TB_REGION_HOLIDAY 2026/5/25 Pfingstmontag=ALL → Hessen 사용자가 5/25 신청 시 자동 0일 차감

- **백엔드: 비즈니스 검증 (`validateLeaveRequest`)** (2026-05-18) — `LeaveReqServiceImpl.validateLeaveRequest()` 신설, createLeave/updateLeave에서 status=REQ일 때 호출:
  1. 날짜 범위: endDt >= strDt
  2. 중복 신청: `findOverlapping()` @Query + CAN/REJ 필터 + 수정 시 자기 자신 제외
  3. 잔여일수 초과: DD(차감유형)일 때 useDays > available이면 400
  4. Special Leave(SP)/Non-deducted(ND) 사유 필수
  5. 결재선 최대 3단계 + 중복 결재자 체크
- **백엔드: 이월 만료 체크** (2026-05-18) — `calcBalance()`에서 LeaveCoPolicy 조회, today > carryover expiry date이면 carryover=0 처리
- **프론트엔드: reject 다이얼로그 필수 유효성** (2026-05-18) — `leave-req-page.tsx`: rejectReasonErr state, 빈 사유 시 빨간 보더 + 에러 메시지 + submit 차단. i18n `rejectReasonLabel`/`rejectReasonRequired` (ko/en/de)
- **Repository 메서드명 전면 리팩토링** (2026-05-18) — Spring Data JPA 파생 쿼리(100자 초과 메서드명) → `@Query` + 최대 15자 약어로 전환. 변경 대상:
  - `LeaveReqRepository`: findOne, findByEmp, findByEmpPeriod, findByCo, findOverlapping(신규)
  - `LeaveGrantRepository`: findOne, findByEmp, findByCoYear
  - `AprvLineRepository`: findByReq, findByEmpSts, findOne
  - `WrkTmRepository`: findOne, findByEmpPeriod, findByCoPeroid, findOneById
  - `DeptRepository`: existsByCd, findByTenant, findByCo, findByCoIds, findOne
  - `EmpRepository`: existsByNo, findByTenant, findByCo, findByCoIds, findByDept, findOne
  - `ExpAccRepository`: findByCo, findOne
  - `RgnHolRepository`: findByRegion, findByRegions, findByPeriod
  - `CoHolRepository`: findByCo, findByPeriod, findOne
  - `CommonCdRepository`: findByGrp, findAllSorted
  - 호출하는 ServiceImpl 전부 동기 수정: LeaveReqServiceImpl, WrkTmServiceImpl, DeptServiceImpl, EmpServiceImpl, CoHolServiceImpl, RgnHolServiceImpl, CommonCdServiceImpl

### 미완료 (백엔드)
- CD ↔ 풀네임 매핑은 프론트 i18n alias로 임시 처리 중. 장기적으로 `LeaveResponse.Context`의 옵션 리스트를 `{cd, label}` 구조로 변경 검토.

### 미완료 (프론트엔드)
- Half Day 시간 필드 (필요 여부 결정)
- 전년도 이월일수 표시

---

## 부서관리 도메인 (Department Management) 진행 현황

### 완료 (백엔드)
- **Dept.java** 확장: `create()` / `update()` / `updateDspOrd()` / `softDelete()` — regionCd, vacCnt, vacInc, dspOrd 파라미터 추가
- **DeptRequest.java** (신규): 통합 Request DTO — tenantId, coId, deptCd, deptNm, upDeptId, stsCd, regionCd, vacCnt, vacInc, dspOrd. `@NotBlank`/`@Size`/`@Pattern` 유효성.
- **DeptResponse.java** (전면 재작성): nested records — `Node`(트리 노드 + childList), `ListWrap`, `TreeWrap`, `EmpCount`. regionCd, vacCnt, vacInc, dspOrd 필드 추가.
- **삭제된 DTO**: CreateDeptRequest, DeptListResponse, DeptNodeResponse, DeptTreeResponse (통합 DTO로 대체)
- **DeptRepository**: `findChildren(tid, cid, pid)`, `countChildren(tid, cid, pid)` `@Query` 추가
- **DeptServiceImpl** (전면 재작성): as-is 비즈니스 로직 완전 복제
  - CRUD: createDept / updateDept / deleteDept / getDepts / getDeptTree / getEmpCount
  - **Sort 관리**: `bumpSortAfter()` (insert mode: 같은 부모 하위 형제 dspOrd +1), `shrinkSortAfter()` (delete mode: 형제 dspOrd -1)
  - **부모 변경**: 새 부모 쪽 bump + 옛 부모 쪽 shrink
  - **유효성**: 부서코드 중복 체크(`existsByCd`), 삭제 시 자식 부서 존재 체크(`countChildren`), 소속 직원 존재 체크(`empRepository.findByDept`)
  - **물리적 삭제** (`deptRepository.delete()`) — as-is와 동일
  - 모든 메서드 `AuthenticatedUsr` 기반 (tenantId + coId JWT 추출)
- **DeptController**: REST 6개 엔드포인트 — `POST /api/v1/departments`, `PUT /{deptId}`, `DELETE /{deptId}`, `GET`, `GET /tree`, `GET /{deptId}/emp-count`
- **V28 Flyway** (`V28__add_dept_emp_admin_menu.sql`): DEPT/EMPL 메뉴를 Customer Admin(ADMIN) 하위에 추가
  - TB_MNU INSERT (MNU_CD='DEPT' DSP_ORD=40, MNU_CD='EMPL' DSP_ORD=50)
  - TB_CD 번역 (MNU.CST.ADM.DEPT/EMPL — ko/en/de 3개국어, **한 레코드에 CD_NM_KO/CD_NM_EN/CD_NM_DE 컬럼 구조**)
  - TB_ROLE_MNU_AUTH 매핑 (PADM/CADM/HR에 CRUD 부여)
  - ⚠️ **V28 초기 실패 원인**: TB_MNU의 `MNU_URL` → 실제 `PATH_URI`, `MNU_NM` NOT NULL 누락, TB_CD_GRP에 `LANG_CD` 컬럼 없음 (한 레코드에 3개국어 컬럼 구조). 3번 수정 후 repair+재적용.

### 완료 (프론트엔드)
- **dept-mgt-page.tsx** (신규): 부서관리 메인 페이지
  - 2패널 레이아웃: `lg:grid-cols-[59fr_41fr]` (좌: 조직도 트리 / 우: 상세 폼) — as-is deptList.html 비율 복제
  - **트리 패널**: `GET /tree` API로 DeptNodeResponse 재귀 렌더링, expand/collapse, 노드 선택 시 상세 폼 자동 채움
  - **상세 폼**: Create/Edit 모드 — 부서코드(생성시만 입력, edit시 disabled), 부서명, 상위부서(읽기전용), 지역코드(독일 16개 주 select), 상태(Active/Inactive), 기본 휴가일수, 근속 가산일수, 정렬순서
  - **소속 직원 수**: `GET /{deptId}/emp-count` 호출, 상세 폼 하단 표시
  - **삭제**: ConfirmDialog(danger variant), 백엔드 에러 메시지 파싱 (child dept / employee 존재 시 toast 안내)
  - **hover 액션**: 트리 노드 hover 시 하위부서 추가(FolderPlus) + 삭제(Trash2) 아이콘
  - 필수필드 `*` 마커 + toast.error + 필드별 빨간 보더 에러
- **dept-cnt.ts** (신규): 부서관리 i18n — `DeptMgtMsgs` 타입 (37개 라벨 ko/en/de)
  - `regionLabel()`: 독일 16개 주 3개국어 라벨 (`HE`→헤센/Hessen, `BY`→바이에른/Bayern 등)
  - `REGION_OPTIONS`: 지역 선택 옵션 배열
- **dept-contract.ts** (전면 재작성): 백엔드 DeptResponse.java와 정확히 매칭
  - `DeptRequest`, `DeptResponse`, `DeptNodeResponse`, `DeptListResponse`, `DeptTreeResponse`, `DeptEmpCountResponse`
- **wsp-body.tsx**: `DEPT` / `MNU_CUSTOMER_DEPARTMENTS` → DeptMgtPage 라우팅 추가, BIZ_MNU_MOD_MAP에서 제거
- **platform-shell-data.ts**: `DEPT`(building 아이콘) + `EMPL`(usrs 아이콘) fallback metadata + 번역(ko/en/de) 추가

### 미완료 (프론트엔드)
- **V28 서버 적용 후 실제 화면 테스트**: 서버 재기동 → 사이드바 Admin → 부서관리 클릭 → API 연동 확인
- **직원관리 페이지**: 사용자가 "부서관리부터" 요청 → 부서관리 완료 후 직원관리 진행 예정

---

## 휴가 도메인 추가 변경 (2026-05-19)

### 중복 휴가 신청 감지 + toast 알림
- **leave-req-modal.tsx**: `hasOverlap` state 추가. `calculateDays` 결과에서 `resultCd === "DUP"` 감지 시 `setHasOverlap(true)` + `toast.error(L.toastOverlap + 날짜범위)`. Submit 버튼 클릭 시 `hasOverlap`이면 toast 에러 + return (제출 차단). 모달 open 시 `setHasOverlap(false)` 리셋.
- **leave-cnt.ts**: `toastOverlap` 라벨 추가 — ko:"선택한 기간에 이미 휴가 신청이 있습니다" / en:"A leave request already exists for the selected dates" / de:"Für den gewählten Zeitraum liegt bereits ein Urlaubsantrag vor"

### CAN/REJ 물리적 삭제
- **LeaveReqServiceImpl.deleteLeave()**: 취소(CAN)/반려(REJ) 건은 `leaveReqRepository.delete(req)`로 물리적 삭제. DRF/기타 상태는 기존 soft delete 유지.
- **LeaveReqServiceImpl.isDeletable()**: CAN/REJ를 삭제 가능 상태로 추가 (DRF와 함께 즉시 true 반환)
- **LeaveReqServiceImpl.toItem()**: canDelete 플래그에 CAN/REJ 포함

---

## 로컬 개발 환경

### Docker
```bash
docker-compose up -d   # MariaDB만 (sinwoo-mariadb:3306)
```
- **Redis 제거**됨, **MinIO 제거**됨 (AWS S3로 이동 예정)

### Gradle (Windows + Java 21 loopback 이슈 회피)
`gradle.properties`:
```properties
org.gradle.jvmargs=-Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.WindowsSelectorProvider
```
> Claude Code 환경에서는 Gradle 데몬이 loopback 소켓 실패 — IDE/터미널에서 직접 실행 권장.

### 프로파일
- `local` — 개발 PC (logging debug 켜져있음)
- `dev` — 환경변수 기반
- 기본 `local`

### 로그
- `com.sinwoo: debug`
- `org.springframework.web: debug`
- `org.springframework.security: debug`
- **프론트엔드: V23 메뉴 단축 CD 적응 (Dashboard 탭 X 제거, 깜빡임 fix)**:
  - `wsp-body.tsx`: 새 단축 CD(`DASH`/`MYWT`/`TMWT`/`RQLV`/`RPWT`/`COHL`) 라우팅 + 미구현 메뉴(`RQTR`/`RQEX`/`RQIN`/`CLMS`/`CMEX`/`CMTR`/`CMIN`/`RPRT`/`RPEX`/`FNC`/`ADMIN`/`LVPL`/`LVGR`)는 `UnderConstruction` placeholder.
  - `components/ui/under-construction.tsx` 신규 (ko/en/de "공사중" + lucide `Construction` 아이콘).
  - `wsp-mnu-compat.ts`: 옛 `MNU_CUSTOMER_*` ↔ 새 단축 CD alias 양방향 매핑 (저장된 탭/세션 안전 마이그레이션).
  - `use-wsp-mnus.ts`: 메뉴 API scope 쿼리 `"ADMIN"/"CUSTOMER"` → `"ADM"/"CST"`.
  - `platform-shell-data.ts`:
    - `defaultTabId` `MNU_CUSTOMER_DASH`→`DASH`, `admin-overview`→`ADSH` — **Dashboard 탭 닫기(X) 버튼 제거** (`tab.id === defaultTabId` 일 때 X 숨김 기준 충족).
    - `fallbackMnuPresentationMetadata`에 새 단축 CD 21개 + 아이콘 + DASH/ADSH `closable: false` 추가.
    - `fallbackMnuTitleTranslations`에 새 단축 CD 25개(ADM 4개 포함) ko/en/de 번역 추가 — **새로고침 시 "DASH" 코드값 잠깐 노출 후 "Dashboard"로 바뀌는 깜빡임 fix**.
  - `wsp-mnu-utils.ts: resolveWspTabTitle()` 우선순위 재정렬: API → **탭 기존 제목 유지** → 하드코딩 fallback → tabId. 옛 풀네임 fallback이 상위/옛 메뉴명을 잠깐 표시하던 회귀 차단.
  - `leave-req-page.tsx`: 백엔드가 보내는 단축 상태 CD(`DRF`/`REQ`/`WAT`/`APR`/`REJ`/`CAN`/`ACN`) → 풀네임 매핑 `STATUS_CD_ALIAS` 추가. `statusOpts` Set으로 dedupe (`REQ`/`WAT`가 둘 다 `Requested`로 매핑되는 중복 키 경고 해결).
- **V24: stale ADMIN sub-menu 정리** (`V24__cleanup_stale_admin_submenus.sql`):
  - V14에서 시드된 17개 ADMIN 하위 메뉴(`MNU_ADMIN_AUDIT`/`_TENANT_LIST`/`_TENANT_SETTINGS`/`_ROLE_POLICY`/`_PLAN_CATALOG`/`_PAYMENT_GATES`/`_UPGRADE_QUEUE`/`_MENU_TREE`/`_TAB_POLICY`/`_DEPTH_POLICY`/`_COMPANY_PROFILE`/`_WORKSPACE_POLICY`/`_MENU_POLICY`/`_DEPTH_EDITOR`/`_CHANGE_HISTORY`/`_ACCESS_LOGS`/`_COMPLIANCE`) — UI 라우팅/컴포넌트 없는 stale 데이터 일괄 삭제.
  - 처리 순서: ① 자식 `UP_MNU_ID` NULL → ② `TB_ROLE_MNU_AUTH` 정리 → ③ `TB_CD` 번역 정리 → ④ `TB_MNU` 본체 삭제.
  - `MENU`(=`MNU_ADMIN_MENU`)의 `UP_MNU_ID`가 V4 시드 오류로 `AUTH`(3) 자식으로 매달려 있던 것 → NULL로 top-level 복원.
  - 최종 ADM scope: 5개 top-level (`ADSH`/`TNT`/`AUTH`/`BILL`/`MENU`).
  - Orphan 검증: TB_CD(MNU_NM 그룹) / TB_ROLE_MNU_AUTH / TB_CD_GRP 모두 0건 (V23에서 이미 정리됨).
- **V27: 부서/직원 시드 마이그레이션** (`V27__seed_departments_and_employees.sql`):
  - as-is `tb_dept_master`(7건) + `tb_employee_master`(20건) → to-be `TB_DEPT`/`TB_EMP`로 시드.
  - 부서 7건: SWH(기존 업데이트), IT, SEG, SSEG, QEG, QCL, HMSG. 모두 level 1, UP_DEPT_ID=NULL. '111' 테스트 부서 제외.
  - 직원 20건: RANK_CD→JOB_TTL_CD, RESIGN_YN='Y'→STS_CD='RESG', 빈 SEX_CD/BIRTH_DATE/TEL_NO→NULL.
  - SW0004(ggamgang) USR_ID=1 보존. DEPT_ID는 @변수로 부서 PK 조회 후 매핑.
  - `ON DUPLICATE KEY UPDATE`로 idempotent.
  - Entity 변경 없음.
- **V26: 경비 계정과목 시드** (`V26__seed_expense_account_master.sql`):
  - as-is `tb_expense_account_master` 전체 데이터 (~110건) → to-be `TB_EXP_ACC`로 시드.
  - SKR03 일반 계정(300~9009), 채권AR(10xxx), 채무AP(70xxx), Personenkonto(80xxx~81xxx) 포함.
  - `ON DUPLICATE KEY UPDATE`로 idempotent.
- **V25: 마스터 테이블 확장 + 경비 계정과목 신규** (`V25__extend_master_tables_and_add_exp_acc.sql`):
  - TB_CO ALTER: `EXP_ACC_CD`(INT), `COUNTRY`(VARCHAR 5), `ADDRESS`(VARCHAR 500) 추가
  - TB_DEPT ALTER: `VAC_CNT`(DECIMAL 5,1), `VAC_INC`(DECIMAL 5,1), `DSP_ORD`(INT) 추가
  - TB_EMP ALTER: `JOB_TTL_NM`→`JOB_TTL_CD`(VARCHAR 20) 변경, `EML`, `EXP_ACC_CD`, `SEX_CD`(CHAR 1), `TEL_NO`, `BIRTH_DT` 추가
  - TB_LEAVE_CO_POLICY ALTER: `VAC_INC_DAYS`(DECIMAL 4,1), `VAC_INC_MAX_DAYS`(DECIMAL 4,1) 추가
  - TB_EXP_ACC 신규 생성 + HIST + AI/AU/BD 트리거
  - 공통코드 시드: JOB_TTL(CE/MG/DR/IN), EXP_CATG(TRFC/MEAL/ENTM/OTHR/NONE) — ko/en/de 3개국어
  - 기존 시드 UPDATE: CO_CD='SW', EXP_ACC_CD=80000, COUNTRY='HE' / DEPT_CD='SWH', REGION_CD='HE', VAC_CNT=24.0 / EMP_NO='SW0004', JOB_TTL_CD='DR', EXP_ACC_CD=80008 / LEAVE_CO_POLICY FLAT_DAYS=24.0, VAC_INC_DAYS=0.5
  - 전체 idempotent (`IF NOT EXISTS`/`IF EXISTS`/`OR REPLACE`/`ON DUPLICATE KEY UPDATE`)
  - **Entity 변경**: Co.java(+3 필드), Dept.java(+3 필드), Emp.java(rename+5 필드), LeaveCoPolicy.java(+2 필드)
  - **신규**: `ExpAcc.java` (경비 계정과목 Entity, CHAR(1) columnDefinition 사용), `ExpAccRepository.java`
  - **jobTtlNm→jobTtlCd 전파**: CreateEmpRequest, EmpResponse, EmpServiceImpl, LeaveReqServiceImpl, emp-contract.ts
  - ⚠️ Hibernate CHAR(1) 타입: DB `CHAR(1)` 컬럼은 Entity에서 `columnDefinition = "CHAR(1)"` 필수 (length=1 → VARCHAR(1) 불일치)
- **V23: 모든 `*_CD` 값 간소화 + CUSTOMER 메뉴 트리 전면 재구성** (`V23__shorten_cd_values_and_restructure_customer_menu.sql`):
  - **CD 값 일괄 단축** (CLAUDE.md "공통코드" 섹션 규칙 적용):
    - `ROLE_SCOPE_CD`/`MNU_SCOPE_CD`/`ROLE_D1_CD`/`ROLE_GRP_CD`: `PLATFORM`→`PLT`, `CUSTOMER`→`CST`, `ADMIN`→`ADM`
    - `ROLE_D2_CD`: `ADMIN`→`ADM`, `USER`→`USR` (HR/FI 유지)
    - `BILL_GATE_CD`: `PAID_CUSTOMER_ADMIN`→`PCST`
    - `TENANT_TP_CD`: `INTERNAL`→`INTL`, `CUSTOMER`→`CST`
    - `REQ_TP_CD`: `LEAVE`→`LV`, `TRIP`→`TR`, `EXPENSE`→`EX`
    - `GRANT_TIMING_CD`: `FISCAL`→`FSC`, `HIRE`→`HIR` / `GRANT_TYPE_CD`: `FLAT`→`FLT`, `TIERED`→`TRD` / `EXPIRE_ACTION_CD`: `AUTO_ZERO`→`ZERO`, `CARRY`→`CARY`
    - ADMIN scope MNU_CD: `MNU_ADMIN_DASH`→`ADSH`, `MNU_ADMIN_TENANT`→`TNT`, `MNU_ADMIN_AUTH`→`AUTH`, `MNU_ADMIN_BILL`→`BILL`, `MNU_ADMIN_MENU`→`MENU` (+ `MNU_NM_CD` dotted path `MNU.ADM.*` 으로 단축)
  - **CUSTOMER scope 메뉴 트리 재구성** (기존 11개 메뉴 DELETE → 새 21개 INSERT):
    ```
    Dashboard (DASH) — 모든 사용자
    Working Time (WT) — 모든 사용자
      ├─ My Working Time (MYWT)   — 모든 사용자
      └─ Team Working Time (TMWT) — CADM/PADM (팀장급은 추후 직원 직급으로 별도 판정)
    Request (RQST)
      ├─ Leave (RQLV)            — 모든 사용자
      ├─ Business Trip (RQTR)    — 모든 사용자 (공사중)
      ├─ Expense (RQEX)          — 모든 사용자 (공사중)
      └─ Inbox (RQIN)            — 모든 사용자 (공사중)
    My Claims (CLMS)
      ├─ Expense (CMEX)          — 모든 사용자 (공사중)
      ├─ Business Trip (CMTR)    — 모든 사용자 (공사중)
      └─ Inbox (CMIN)            — 모든 사용자 (공사중)
    Reports (RPRT)
      ├─ Work Time (RPWT)        — 모든 사용자 (공사중)
      └─ Monthly Expenses (RPEX) — 모든 사용자 (공사중)
    Finance (FNC) — FI/CADM/PADM (공사중)
    Admin (ADMIN) — CADM/PADM (기존 Customer Admin 트리 유지)
      ├─ Leave Policy (LVPL)
      ├─ Leave Grants (LVGR)
      └─ Company Holidays (COHL)
    ```
  - **TB_ROLE_MNU_AUTH 매핑 재구성**:
    - PADM/CADM: 전체 CUSTOMER 메뉴
    - HR/FI: ADMIN 트리 + Team Working Time 제외 (FI는 Finance 포함, USR는 Finance 제외)
    - USR: ADMIN/TMWT/FNC 제외, VIEW만 (CRUD/APRV 권한 X)
  - **TB_CD 번역 시드** (`MNU_NM` 그룹): 21개 새 메뉴 ko/en/de 번역 삽입 (`ON DUPLICATE KEY UPDATE`).
  - **기존 메뉴 정리**: CUSTOMER scope `MNU_CUSTOMER_TEAM`/`MNU_CUSTOMER_HR`/`MNU_CUSTOMER_FIN`/`MNU_CUSTOMER_PAY`/`MNU_CUSTOMER_MY_TIME`/`MNU_CUSTOMER_TEAM_TIME`/`MNU_CUSTOMER_LEAVE`/`MNU_CUSTOMER_EMPLOYEES`/`MNU_CUSTOMER_DEPARTMENTS`/`MNU_CUSTOMER_ROLES`/`MNU_CUSTOMER_OCR_INBOX`/`MNU_CUSTOMER_EXPENSE_REVIEW`/`MNU_CUSTOMER_ARCHIVE`/`MNU_CUSTOMER_SUBSCRIPTION`/`MNU_CUSTOMER_PAYMENTS`/`MNU_CUSTOMER_ADMIN`/`MNU_CUSTOMER_ADMIN_LEAVE_POLICY`/`MNU_CUSTOMER_ADMIN_LEAVE_GRANT`/`MNU_CUSTOMER_ADMIN_CO_HOLIDAY` 모두 삭제 후 새 단축 코드로 재INSERT.
  - **백엔드 상수 업데이트** (V23 데이터 변경과 매칭):
    - `AuthBizConst.java`: TENANT_TP_CD_*/AUTH_GRP_CD_* 단축
    - `MnuBizConst.java`: MNU_SCOPE_CD_*/BILL_GATE_CD_*/ROLE_D1_CD_*/ROLE_D2_CD_* 단축
    - `MnuServiceImpl.java`: `hasPlatformAdminRole()` 비교문자열 `"PLATFORM"`→`"PLT"`
    - `TenantServiceImpl.java`: `normalizeTenantType()` 기본값 `"CUSTOMER"`→`"CST"`, `normalizeBillFreeYn()` `"INTERNAL"`→`"INTL"`
    - `Tenant.java`: `create()` 기본 `tenantTpCd` `"CUSTOMER"`→`"CST"`
    - `BillAccessPolicyService.java`: `"INTERNAL"`→`"INTL"`
    - `LeaveReqServiceImpl.java`: `REQ_TP_LEAVE` `"LEAVE"`→`"LV"`
  - **프론트엔드 업데이트** (`use-wsp-mnus.ts`): API 호출 시 scope 쿼리 `"ADMIN"/"CUSTOMER"` → `"ADM"/"CST"`
  - ⚠️ 테스트 파일들 다수에 옛 CD 하드코딩 잔존 — `gradle test` 시 영향 (main 빌드는 통과). V19에서 안내된 항목과 동일, 추후 정리 필요.
  - ⚠️ 프론트 `wsp-mnu-utils.ts`/`wsp-mnu-compat.ts`/`wsp-body.tsx`에 옛 `MNU_CUSTOMER_*` ID 다수 — 다음 단계(프론트 라우팅 마이그레이션)에서 새 단축 CD로 교체 예정.
- **V22: 누락된 ROLE_D1_CD/ROLE_D2_CD/ROLE_GRP_CD 백필 + 메뉴 BILL_GATE 회귀 fix**:
  - V19에서 새 5개 ROLE을 시드할 때 ROLE_D1_CD/D2_CD를 NULL로 둔 실수 발견.
  - `MnuServiceImpl.hasCustomerAdminRole()`이 `ROLE_D1_CD='CUSTOMER'` + `ROLE_D2_CD != 'USER'`로 판정 → 새 ROLE 모두 false → `BILL_GATE_CD='PAID_CUSTOMER_ADMIN'` 메뉴 12개(Team Management/HR/Finance/My Time/Team Time/Leave Requests/Employees/Departments/Roles/OCR Inbox/Expense Review/Archive) **사이드바에서 사라짐**.
  - V22로 ROLE_D1_CD/D2_CD/GRP_CD 백필: PADM(PLATFORM/ADMIN), CADM(CUSTOMER/ADMIN), HR(CUSTOMER/HR), FI(CUSTOMER/FI), USR(CUSTOMER/USER).
  - `MnuServiceImpl.isBillingGateSatisfied()`에 **Platform Admin bypass** 추가 — `hasPlatformAdminRole()` 헬퍼 신설 (ROLE_D1_CD='PLATFORM' or ROLE_CD='PADM'이면 BILL_GATE 무조건 통과). ggamgang(PADM) 같은 시스템 최상위 권한자가 모든 메뉴를 볼 수 있도록 보장.
- **V21: TB_LEAVE_CO_POLICY 월/일 컬럼 TINYINT → SMALLINT** (`V21__alter_leave_policy_month_columns_to_smallint.sql`) — V20 적용 후 서버 기동 시 Hibernate schema validation 실패(`wrong column type: found TINYINT, but expecting SMALLINT`) 수정. Entity 필드 `Short` 타입(=SMALLINT)과 일치시킴. `FISCAL_START_MM`/`CARRYOVER_EXPIRE_MM`/`CARRYOVER_EXPIRE_DD` 3개 컬럼 + HIST 테이블 동시 변경.
- **V20: 휴가 정책 + 부여 도메인** (`V20__add_leave_policy_and_grant_domain.sql`):
  - `TB_LEAVE_CO_POLICY` (회사별 1행) — `GRANT_TIMING_CD`/`FISCAL_START_MM`/`GRANT_TYPE_CD`/`FLAT_DAYS`/`CARRYOVER_ENABLED_YN`/`CARRYOVER_MAX_DAYS`/`CARRYOVER_EXPIRE_MM`/`CARRYOVER_EXPIRE_DD`/`EXPIRE_ACTION_CD` 컬럼. UK(TENANT_ID, CO_ID).
  - `TB_LEAVE_GRANT` (직원×연도) — `GRANT_DAYS`/`CARRYOVER_DAYS`/`USED_DAYS`/`EXPIRED_DAYS`/`REMARK`. UK(TENANT_ID, CO_ID, EMP_ID, GRANT_YR).
  - HIST 테이블 + AI/AU 트리거.
  - 기본 시드: Sinwoo 회사 (FLAT 20일 / 이월 전부 / 3월 31일 만료 / AUTO_ZERO). ggamgang 2026년 부여 20일 + 이월 5일 시드.
  - Entity `LeaveCoPolicy`/`LeaveGrant`, Repository 2개 추가.
  - `LeaveReqServiceImpl.calcBalance()` 실제 구현 — TB_LEAVE_GRANT 조회 + 실시간 used 합산 (DD + 활성상태 + 당해연도). 반환 `availableDays/afterRequestDays/previousYearDays(=이월잔여)/currentYearDays(=당해부여)`.
  - 프론트 `LeaveBalSum`: `previousYearDays > 0`이면 잔여일수 옆에 `15.0 (5.0)` 형식으로 괄호 안 이월분 표시. `leave-req-page.tsx`에서 prop 전달.
  - 미완: Service/Controller (정책 조회/수정, 부여 CRUD), Customer Admin 프론트 UI, 만료 처리 배치 job.
- **V19: Role 단순화 + Customer Admin 메뉴 트리** (`V19__simplify_roles_and_add_customer_admin_menu.sql`):
  - **기존 14개 ROLE → 5개**: `PADM`(LVL 100, Platform Admin) / `CADM`(50, Customer Admin) / `HR`(30) / `FI`(20) / `USR`(10)
  - Member/Leader 구분 폐지. ROLE_LVL_CD 위계 부여 (높을수록 권한 큼)
  - `TB_USR_ROLE`/`TB_ROLE_MNU_AUTH` 전부 클리어 후 재구성. ggamgang → PADM 부여
  - 새 메뉴(CUSTOMER 스코프, DSP_ORD=60):
    ```
    Customer Admin                                  ← CADM + HR (메뉴 진입)
      ├─ Leave Policy   (/customer/admin/leave-policy)  ← CADM + HR (LVL >= 30)
      ├─ Leave Grants   (/customer/admin/leave-grants)  ← CADM only
      └─ Company Holidays (/customer/admin/holidays)    ← CADM only
    ```
  - 기존 메뉴 권한 매핑: **PADM에만** 모든 메뉴 부여 (사용자가 추후 메뉴별로 CADM/HR/FI/USR 권한 지정 예정)
  - 코드 정리:
    - `application.yml`: `customer-default-role-cd: USR`, `internal-default-role-cd: PADM`
    - `CoController.isPlatformSuperAdmin`: `"PADM"` 비교
    - `UsrServiceImpl.resolveRoles`: 기본 역할 `"USR"`
    - 프론트 `wsp-mnu-utils.ts`: `CUSTOMER_ADMIN_ROLE_IDS = ["CADM","PADM"]`
  - ⚠️ 테스트 파일(`MnuServiceImplTest` 등)에 옛 ROLE_CD 하드코딩 존재 — 빌드 시 메인은 통과하나 `gradle test` 시 영향. 추후 정리 필요.
- **로그인/새로고침 시 마지막 활성 모드+탭 복원, 이력 없을 때만 고객 대시보드** (이전 "무조건 client 강제" 정책 철회):
  - `lib/utils/wsp/wsp-storage.ts`: `PersistedWspShellState.lastActiveAt?: number` 필드 신설. `writePersistedWspState()`가 저장 시 `Date.now()` 자동 stamp.
  - `components/layout/wsp/use-wsp-shell-state.ts`: `preferredMode` 결정 로직을 **`lastActiveAt` 비교**로 변경 — adminStamp > clientStamp면 admin, 아니면 client 우선. 둘 다 없는 신규 사용자만 "client"(고객 dashboard) fallback.
  - `clearAllPersistedWspState()` 함수는 유지(향후 명시적 reset용)하되 로그인 시 호출은 제거. `cred-login-panel.tsx` / `auth/callback/page.tsx`에서 clear 호출 삭제.
  - 결과: 새로고침 → 직전 모드+탭 복원. 재로그인 → 마지막 활성 모드 복원. 한 번도 사용한 적 없는 사용자만 고객 dashboard로 진입.
- **SQL 로깅 ON (local 프로파일 한정)** — `application-local.yml`:
  - `spring.jpa.show-sql=true`, `format_sql=true`, `use_sql_comments=true`, `highlight_sql=true`
  - `org.hibernate.SQL: debug` (실행 SQL)
  - `org.hibernate.orm.jdbc.bind: trace` (바인딩 파라미터 값)
  - `org.hibernate.orm.jdbc.extract: trace` (SELECT 결과 값)
  - dev/prod 프로파일은 보안상 끈 상태 유지
- 패턴: `[reqId:...] [tenant:SINWOO] [usr:ggamgang@sinwoo-itc.com]`

---

## as-is → to-be 마이그레이션 대상 (우선순위)

| 우선순위 | 도메인 | as-is 테이블 |
|---------|--------|-------------|
| **1** | 출퇴근 | `tb_daily_work_time` |
| **1** | 휴가/출장 결재 | `tb_plan_vacation`, `tb_plan_business_trip` (+ approval) |
| 1 부속 | 휴일 캐시 | `tb_company_holiday`, `tb_region_holiday` |
| 2 | 경비 청구 (핵심) | `tb_expense_monthly_header/detail/...` |
| 2 | 계정과목 | `tb_expense_account_master` |
| 3 | 법인계좌 | `tb_corporate_account*` |
| 3 | 운영 매출/비용 (recurring) | `tb_operating_*` |
| 4 | 자산 | `tb_asset*` |
| 5 | 재무제표 | `tb_fi_stmt_*` |
| 5 | 월마감 | `tb_period_close` |
| 별도 | 급여 | `tb_payroll_*` |

도메인 1개 작업 단위:
1. as-is 분석 (Mapper XML / VO / Service / 화면)
2. to-be 테이블 설계 (컬럼 매핑 + tenantId 등 새 컬럼)
3. Flyway V16+ 작성 + 적용 검증
4. Entity + Repository
5. DTO + Service
6. Controller + API 검증 (curl/Postman)
7. 프론트엔드 연결
각 단계 후 빌드+서버 검증.

---

## 다음 세션 시작 시 할 일

> 마지막 세션: 2026-05-19 / 커밋: `c2c7fdf` / 브랜치: `main`

### 1. 🔴 서버 재기동 + V28 적용 (최우선)
- IDE에서 `SinwooBackendApplication.main` 실행
- **확인 사항**:
  - V28 Flyway 마이그레이션 정상 적용 (DEPT/EMPL 메뉴 + 번역 + 권한 매핑)
  - ⚠️ V28은 이전 세션에서 3번 실패 후 repair한 상태. `flyway_schema_history`에서 V28 실패 기록을 수동 DELETE했으므로, 재기동 시 V28이 처음부터 다시 적용됨.
  - DEPT 메뉴가 사이드바 Admin 하위에 나타나는지 확인
  - `GET /api/v1/departments/tree` API 호출하여 부서 트리 7건 응답 확인
- **V28 실패 시**: `flyway_schema_history`에서 실패 기록 삭제 → SQL 수정 → 재기동. V28 SQL은 이미 3번 수정 완료 상태이므로 성공할 것.

### 2. 🔴 프론트엔드 부서관리 화면 테스트
- `frontend/` 디렉토리에서 `npm run dev`
- 로그인 → 사이드바 Admin → 부서관리(DEPT) 클릭
- **확인 사항**:
  - 좌측 트리 패널에 7개 부서 표시 (SWH, IT, SEG, SSEG, QEG, QCL, HMSG)
  - 부서 클릭 → 우측 상세 폼에 정보 채워짐
  - 신규 부서 생성 (최상위 / 하위)
  - 부서 수정 (이름, 지역, 휴가일수 등)
  - 부서 삭제 (자식 부서/직원 있으면 에러 메시지)
  - 정렬순서 변경 후 트리 반영

### 3. 🟡 직원관리 (Employee Management) 페이지
- 부서관리와 동일한 패턴으로 진행
- 백엔드: EmpController/EmpService 이미 기본 구조 있음, CRUD 확장 필요
- 프론트: `frontend/features/admin/emp/emp-mgt-page.tsx` 신규
- V28에서 EMPL 메뉴 + 권한 이미 시드됨 → wsp-body.tsx에 EMPL 라우팅 추가만 하면 됨

### 4. 🟡 미리팩토링 Repository 정리 (platform 외)
- `platform/` 핵심 도메인은 리팩토링 완료. 아래는 아직 긴 메서드명 남아있음:
  - `UsrRepository` / `UsrServiceImpl` — `findAllByTenantIdAndCoIdOrderByCreatedAtDescIdDesc` 등
  - `CoRepository` / `CoServiceImpl` — `existsByTenantIdAndCoCdIgnoreCase`, `findAllByTenantIdOrderByCreatedAtDescIdDesc`
  - `TenantRepository` / `TenantServiceImpl` — `findAllByOrderByCreatedAtDescIdDesc`
  - `MnuRepository` / `MnuServiceImpl` — `findAllByOrderByMnuScopeCdAscDspOrdAscIdAsc` 등
  - `RoleMnuAuthRepository`, `RoleRepository`, `CdGroupRepository`
  - `PayTxnRepository`, `SubscrRepository`, `SubscrPlanRepository`
  - `AuthServiceImpl` — `findAllByUsrId`, `findAllByEmlIgnoreCase`
- **동일 패턴**: 파생 쿼리 → `@Query` + 15자 이내 메서드명

### 5. 🟡 테스트 파일 정리
- `src/test/` 내 테스트 파일들이 옛 긴 메서드명과 옛 CD값(`PLATFORM`/`CUSTOMER` 등) 사용 중
- `gradle test` 실행하면 컴파일 에러 발생할 수 있음
- V19/V23에서 CD 단축 + Repository 리팩토링 반영 필요

### 6. 🟢 휴가 도메인 잔여 미완료
- **백엔드**: CD ↔ 풀네임 매핑 — 프론트 i18n alias로 임시 처리 중. `LeaveResponse.Context` 옵션을 `{cd, label}` 구조로 변경 검토
- **프론트**: Half Day 시간 필드 (AM/PM 선택 시 시간대 표시 필요 여부), 전년도 이월일수 표시

### 7. 🟢 다음 도메인: 출장(Business Trip)
- as-is: `tb_plan_business_trip` + 결재 로직
- 휴가 도메인과 유사 구조 (TB_APRV_LINE 공유, REQ_TP_CD='TR')
- 순서: as-is 분석 → 테이블 설계 → Flyway → Entity/Repo → Service/DTO → Controller → 프론트

## 자주 쓰는 명령
```bash
# DB 컬럼 확인
docker exec sinwoo-mariadb mariadb -u sinwoo -psinwoo sinwoo -e "DESC TB_XXX"

# Access Log 조회
docker exec sinwoo-mariadb mariadb -u sinwoo -psinwoo sinwoo -e \
  "SELECT REQ_ID, TENANT_KEY, USR_KEY, HTTP_MTHD_CD, REQ_URI, RESP_STS_CD FROM TB_ACCESS_LOG ORDER BY CRT_DTM DESC LIMIT 10"

# BCrypt 해시 생성 (1234)
docker run --rm httpd:alpine sh -c "htpasswd -nbBC 10 admin 1234"
```

## 주요 파일 위치

### 백엔드
- 엔트리: `src/main/java/com/sinwoo/SinwooBackendApplication.java`
- 보안: `src/main/java/com/sinwoo/common/security/`
- 멀티테넌시: `src/main/java/com/sinwoo/common/security/tenant/`
- 공통 유틸: `src/main/java/com/sinwoo/common/util/StringNormalizer.java`
- 참조 검증: `src/main/java/com/sinwoo/platform/support/PlatformRefValidator.java`
- 메시지 카탈로그: `src/main/resources/i18n/messages*.properties`
- Flyway: `src/main/resources/db/migration/V1~V26`
- 휴가 도메인: `src/main/java/com/sinwoo/platform/leave/` (controller, service, dto, domain, repository)
- 결재선 도메인: `src/main/java/com/sinwoo/platform/aprv/` (domain, repository)
- 경비 계정과목: `src/main/java/com/sinwoo/platform/expense/` (domain/ExpAcc.java, repository/ExpAccRepository.java)

### 프론트엔드
- 로그인: `frontend/features/auth/login-page-shell.tsx`
- 워크스페이스 레이아웃: `frontend/app/(wsp)/layout.tsx`
- 휴가 신청 페이지: `frontend/features/requests/leave/leave-req-page.tsx`
- 휴가 신청 모달: `frontend/features/requests/leave/leave-req-modal.tsx`
- 휴가 테이블: `frontend/features/requests/leave/leave-req-table.tsx`
- 휴가 상태 배지: `frontend/features/requests/leave/leave-status-badge.tsx`
- 휴가 필터 바: `frontend/features/requests/leave/leave-filt-bar.tsx`
- 휴가 잔여일수: `frontend/features/requests/leave/leave-bal-sum.tsx`
- 휴가 타입/목데이터: `frontend/features/requests/leave/leave-mock-data.ts`
- 휴가 i18n: `frontend/lib/i18n/leave-cnt.ts`
- 휴가 API 계약: `frontend/lib/api/leave-contract.ts`
- 커스텀 토스트: `frontend/components/ui/toast.tsx`
- 커스텀 확인 다이얼로그: `frontend/components/ui/confirm-dialog.tsx`
