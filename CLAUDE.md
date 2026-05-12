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
| TB_USR_ROLE | usr=1 ↔ role=5 (ROLE_PLATFORM_SUPER_ADMIN) |

**로그인 계정**: `ggamgang@sinwoo-itc.com` / `1234`

> ⚠️ to-be `TB_EMP` 스키마에 **성별/지역 컬럼 없음**. as-is `tb_employee_master`에는 SEX_CD, REGION 있었음. 직원 도메인 마이그레이션 시 결정 필요.

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
- SQL/JDBC bind는 보안상 끈 상태
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
1. **출퇴근 도메인 마이그레이션 시작** — as-is `tb_daily_work_time` 분석부터
2. (필요 시) `TB_EMP`에 `SEX_CD`, `REGION_CD` 추가 결정
3. (선택) 메뉴 시드 — `TB_MNU`에 워크스페이스 메뉴 트리

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
- 백엔드 엔트리: `src/main/java/com/sinwoo/SinwooBackendApplication.java`
- 보안: `src/main/java/com/sinwoo/common/security/`
- 멀티테넌시: `src/main/java/com/sinwoo/common/security/tenant/`
- 공통 유틸: `src/main/java/com/sinwoo/common/util/StringNormalizer.java`
- 참조 검증: `src/main/java/com/sinwoo/platform/support/PlatformRefValidator.java`
- 프론트 로그인: `frontend/features/auth/login-page-shell.tsx`
- 프론트 워크스페이스 레이아웃: `frontend/app/(wsp)/layout.tsx`
- 메시지 카탈로그: `src/main/resources/i18n/messages*.properties`
- Flyway: `src/main/resources/db/migration/V1~V15`
