# SINWOO 레거시 자산 인벤토리

## 1. 대상 경로

- `legacy/Sinwoo-New/sinwoo`

## 2. 기술 자산 요약

- Java 파일: 약 `201`
- Controller: 약 `36`
- Service/Impl: 약 `63`
- HTML 템플릿: 약 `102`
- 정적 JS: 약 `300`
- MyBatis Mapper XML: `34`
- 개별 SQL 덤프 파일: `5`

## 3. Controller 분포

| 영역 | 개수 |
| --- | ---: |
| root | 1 |
| admin | 7 |
| common | 3 |
| finance | 13 |
| myClaim | 2 |
| report | 2 |
| request | 4 |
| user | 2 |

대표 컨트롤러:

- `HomeController`
- `AnalyzeDocumentController`
- `EmployeeMgtController`
- `PayrollController`
- `AssetMgtController`
- `ClosingController`
- `VacController`
- `WorktimeController`

## 4. 템플릿 분포

주요 화면 디렉터리:

- `template/admin/*`
- `template/common/*`
- `template/finance/mgt/*`
- `template/finance/trans/*`
- `template/finance/reports/*`
- `template/myClaim/*`
- `template/request/*`
- `template/report/*`
- `template/popup/*`

특징:

- 팝업 화면이 많음
- 화면과 JS 파일이 디렉터리 단위로 강하게 결합
- 관리/전표/청구/출장/근태 영역이 이미 UX 흐름으로 구현됨

## 5. MyBatis 매퍼 자산

매퍼 XML 목록:

- `accLedger`
- `accMapping`
- `accMgt`
- `annReports`
- `assetMgt`
- `balance`
- `batch`
- `buTrip`
- `closing`
- `common`
- `corpCalMgt`
- `corporate`
- `dept`
- `deptMst`
- `dlyExpMst`
- `empMgt`
- `expenseMgt`
- `fiStatements`
- `History`
- `Login`
- `monthExp`
- `monthExpRpt`
- `opexp`
- `opexpMgt`
- `opSalesMgt`
- `payroll`
- `proFile`
- `purchase`
- `requestExp`
- `sales`
- `travelExp`
- `vac`
- `workTime`
- `workTimeRpt`

## 6. Mapper Interface 자산

대표 Mapper Interface:

- `LoginMapper`
- `HistoryMapper`
- `PayrollMapper`
- `AssetMgtMapper`
- `WorkTimeMapper`
- `VacMapper`
- `ExMapper`
- `MxMapper`
- `TxMapper`
- `CorporateAccMapper`

## 7. 별도 SQL 덤프 자산

- `tb_code_master.sql`
- `tb_employee.sql`
- `tb_log_history.sql`
- `tb_salary.sql`
- `tb_user.sql`

이 파일들은 레거시 스키마와 초기 데이터 의미를 해석하는 데 유용하다.

## 8. 확인된 공통 인프라

- `WebSecurityConfig`
- `SessionInterceptor`
- `LocaleConfig`
- `MultipartFileConfig`
- `AnalyzeDocumentConfig`
- `BatchConfig`

## 9. 핵심 업무 도메인

### 공통축

- 로그인
- 사용자/프로필
- 부서/직원
- 공통코드
- 권한/세션

### 재무

- 계정코드
- 계정매핑
- 법인전표
- 매출/매입
- 급여
- 자산
- 마감
- 리포트

### 인사/근태

- 근무시간
- 휴가
- 직원관리
- 부서관리

### 청구/요청

- 개인경비
- 출장
- 월경비

### OCR/문서

- Textract 기반 문서분석

## 10. 구조상 주의점

- 정적 산출물(`target`)이 포함됨
- IDE 설정(`.idea`) 포함
- 분석용 ZIP 안에 `.git` 포함
- `src/main/java` 아래 중복/오염 디렉터리 흔적 존재

즉 기능 자산은 풍부하지만, 저장소와 패키지 정리는 차세대 전환 시 반드시 필요하다.
