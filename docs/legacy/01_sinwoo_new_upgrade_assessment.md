# SINWOO-NEW 업그레이드 분석서

## 1. 목적

본 문서는 `C:\Users\JuyongLee\Sinwoo_AI\legacy\Sinwoo-New\sinwoo` 프로젝트를
현재 Sinwoo 표준 구조로 **재개발이 아닌 업그레이드**하기 위한 1차 분석 결과를 정리한다.

핵심 원칙은 다음과 같다.

- 기존 기능과 업무 흐름은 최대한 유지한다.
- 동작 중인 회계/근태/급여/OCR 자산은 버리지 않는다.
- 구조, 표준명, 이력, 보안, 프론트 분리 전략은 점진적으로 상향 정렬한다.

## 2. 현재 프로젝트 요약

### 2.1 기술 스택

- Backend: Spring Boot `3.3.12`
- Build: Maven
- Java: `17`
- Persistence: MyBatis
- Database: MariaDB
- View: Thymeleaf
- Security: Spring Security + Form Login
- Batch: Spring Batch
- OCR/AI: AWS Textract
- 정적 UI: Bootstrap + jQuery + DataTables + FullCalendar

### 2.2 현재 구조 성격

현재 프로젝트는 **백엔드와 프론트가 완전히 분리된 SPA 구조가 아니라**,  
Spring Boot 애플리케이션 안에 다음이 함께 들어 있는 서버 렌더링형 모놀리식 구조다.

- HTML 템플릿
- 정적 JavaScript
- MyBatis SQL 매퍼
- 배치 작업
- 보안/세션/인터셉터
- OCR 연동

즉, 현재 자산은 “레거시”이지만 상당히 많은 기능이 이미 들어가 있으므로,
전체 폐기보다 **도메인별 업그레이드**가 훨씬 유리하다.

## 3. 확인된 자산 현황

### 3.1 소스 규모

- Java 파일: 약 `201`
- Controller: 약 `36`
- Service/Impl: 약 `63`
- HTML 템플릿: 약 `102`
- 정적 JS: 약 `300`

### 3.2 주요 업무 도메인

이미 아래 업무 영역이 코드와 화면 단위로 존재한다.

- Admin
- Common
- Finance
- MyClaim
- Report
- Request
- User
- Scheduler / Batch
- OCR / AnalyzeDocument

대표 컨트롤러 예시:

- `AccMappingController`
- `EmployeeMgtController`
- `PayrollController`
- `AssetMgtController`
- `ClosingController`
- `VacController`
- `WorktimeController`
- `AnalyzeDocumentController`

### 3.3 DB 및 SQL 자산

MyBatis 매퍼가 이미 업무 단위로 분리되어 있다.

대표 매퍼:

- `payroll.xml`
- `vac.xml`
- `workTime.xml`
- `expenseMgt.xml`
- `assetMgt.xml`
- `closing.xml`
- `corporate.xml`
- `History.xml`

별도 SQL 덤프 파일도 존재한다.

- `tb_user.sql`
- `tb_employee.sql`
- `tb_salary.sql`
- `tb_code_master.sql`
- `tb_log_history.sql`

### 3.4 OCR / AI 자산

`AnalyzeDocumentConfig.java` 기준으로 AWS Textract 연동이 이미 들어가 있다.

- `analyzeDocument`
- `analyzeExpense`
- PDF -> JPG 변환
- Query 기반 결과 추출

즉 OCR은 새로 붙이는 것이 아니라, **이미 있는 것을 정리하고 안정화하는 방향**이 맞다.

## 4. 현재 구조의 장점

### 4.1 도메인 기능이 이미 깊게 구현되어 있음

회계, 급여, 자산, 근태, 출장, 경비, 리포트, 배치까지 이미 구현 흔적이 많다.

### 4.2 MariaDB + MyBatis 조합이 현재 업무 성격과 잘 맞음

회계/집계/월마감/전표성 SQL은 JPA보다 MyBatis가 다루기 편한 구간이 많다.

### 4.3 OCR/보안/배치 자산이 존재함

AWS Textract, Spring Security, Batch Tasklet이 이미 있어
플랫폼 확장에 필요한 기반은 적지 않다.

### 4.4 Thymeleaf 템플릿과 정적 JS로 기능이 이미 가시화됨

화면이 존재한다는 것은 요구사항이 코드와 UX 흐름으로 이미 한 번 해석되었다는 뜻이다.

## 5. 현재 구조의 문제점

### 5.1 컨트롤러가 너무 많은 책임을 가짐

예: `PayrollController.java`

현재 컨트롤러가 다음을 동시에 수행한다.

- 요청 파싱
- 로그인 컨텍스트 주입
- JSON 조립
- 엑셀 파싱
- 서비스 호출 조합
- 하위 도메인 동기화

이 구조는 기능은 되지만 유지보수성이 낮고 테스트가 어렵다.

### 5.2 Service는 얇고 SQL/Controller에 로직이 치우침

`PayrollServiceImpl`처럼 대부분의 ServiceImpl이 매퍼 호출 위임 중심이라
비즈니스 규칙이 Controller 또는 MyBatis XML에 흩어져 있다.

### 5.3 표준명과 네이밍 규칙이 현재 Sinwoo 표준과 다름

예:

- 기존: `tb_payroll_header`, `company_id`, `create_date`
- 목표: `TB_*`, 약어 기반 컬럼, 감사 컬럼 표준화

즉 DB 표준화는 필요하지만, 기존 테이블을 한 번에 깨면 안 된다.

### 5.4 프론트가 현대적 분리형 구조가 아님

현재 화면은 `Thymeleaf + static js` 기반이다.
즉 현재 Sinwoo의 `Next.js + Tailwind + shadcn/ui` 방향과 바로 동일하지 않다.

### 5.5 구조 오염 흔적이 존재함

다음과 같은 비정상 흔적이 보인다.

- `.git` 포함 ZIP
- `.idea` 포함
- `target` 산출물 포함
- `src/main/java` 아래 이상한 중첩 디렉터리 흔적
- 정적 라이브러리와 산출물이 함께 커밋된 정황

이건 기능 문제보다 **저장소 위생과 구조 관리 문제**다.

## 6. 유지 / 개선 / 위험 구간

### 6.1 유지 권장

- 업무 도메인 분류 자체
- MyBatis 매퍼 자산
- OCR/Textract 자산
- 배치(Tasklet) 자산
- 기존 화면 흐름과 메뉴 구조
- 보안/세션 개념

### 6.2 개선 필요

- DB 네이밍 표준화
- 공통 감사 컬럼 표준화
- 이력 테이블/트리거 전략 도입
- Controller 책임 분리
- DTO/Command/Result 계층 도입
- 도메인별 패키지 재정렬
- 설정 파일 분리 (`application-local`, `dev`, `prod`)
- Maven -> Gradle 여부 재검토 또는 브리지 전략 수립

### 6.3 위험 구간

- 기존 SQL이 많은 화면/기능
- 복잡한 월마감/급여/자산 계산 로직
- 화면 JS와 서버 템플릿이 강하게 결합된 페이지
- 네이밍 변경이 직접 쿼리에 영향을 주는 매퍼
- 로그/이력/권한 로직이 암묵적으로 섞여 있는 부분

## 7. 권장 업그레이드 전략

## 7.1 원칙

- “전면 교체” 금지
- “기능 유지 + 구조 개선” 우선
- DB/백엔드/프론트 분리를 동시에 하지 말고 단계적으로 진행
- 기존 API/화면을 깨지 않는 브리지 전략 허용

### 7.2 1단계: 분석 고정화

- 전체 도메인 맵 작성
- 매퍼/컨트롤러/템플릿 대응표 작성
- 핵심 테이블 목록 정리
- 공통 로그인/권한/회사/부서/직원 컨텍스트 파악

### 7.3 2단계: 공통 표준 레이어 도입

- 표준 감사 컬럼
- 표준 이력 구조
- 공통 응답 규격
- 공통 예외 처리
- 공통 DTO/Request/Response 패턴

이 단계에서는 기존 기능을 깨지 않고 **공통 인프라만 추가**한다.

### 7.4 3단계: 도메인별 업그레이드

우선순위 추천:

1. 사용자 / 권한 / 회사 / 부서
2. 근태 / 휴가
3. 경비 / 출장 / 개인청구
4. 급여
5. 자산 / 마감 / 재무리포트
6. OCR / 문서관리

각 도메인마다 다음 순서로 진행한다.

- 기존 SQL/화면/컨트롤러 분석
- 표준 DTO 설계
- 서비스 계층 보강
- DB 표준화 계획 수립
- 프론트 분리 필요성 판단

### 7.5 4단계: 프론트 현대화

현재 화면을 한 번에 Next.js로 갈아타기보다 아래처럼 간다.

- 1차: 기존 Thymeleaf 유지
- 2차: 신규 화면만 Next.js로 분리 가능
- 3차: 도메인별로 점진 전환

즉 프론트는 **병행 운용 전략**이 더 안전하다.

## 8. 현재 Sinwoo 표준과 연결 가능한 지점

다음은 비교적 자연스럽게 현재 표준과 연결 가능하다.

- MariaDB 유지
- 회계/근태/급여 도메인 유지
- OCR/Textract 유지
- Spring Boot 유지
- 보안/권한 개념 유지
- 이력/감사 컬럼 표준 도입
- 테이블 명세/공식 문서화 도입

즉 “완전 다른 프로젝트”가 아니라,
현재 Sinwoo 표준은 이 레거시를 **정리하고 제품화하는 기준틀**로 쓰면 된다.

## 9. 1차 결론

이 프로젝트는 다음으로 판단한다.

- 폐기 대상 아님
- 기능 자산 가치 높음
- 구조는 오염됐지만 회복 가능
- 현재 Sinwoo 방향으로 업그레이드 가능
- 단, 전면 재작성보다 단계적 상향 정렬이 맞음

## 10. 바로 다음 작업 추천

다음 턴에서 바로 진행할 작업:

1. 레거시 도메인-테이블-화면 매핑 문서 작성
2. 핵심 테이블 목록 추출
3. 업그레이드 우선순위 도메인 선정
4. 첫 번째 업그레이드 대상 모듈 착수

추천 첫 대상은 아래 중 하나다.

- 사용자/권한/회사 공통축
- 근태
- 급여

현재 코드 밀도와 비즈니스 중요도를 보면,
**사용자/권한/회사 공통축**부터 잡는 것이 가장 안전하다.
