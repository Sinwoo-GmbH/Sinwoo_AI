# SINWOO 공통축 필드 매핑 초안

## 1. 목적

레거시 공통 테이블과 차세대 표준 모델 사이의 1차 매핑 기준을 정의한다.

현재 목적은 다음 두 가지다.

- 기존 데이터 의미를 잃지 않는다.
- 차세대 표준 구조로 점진 이행 가능한 연결고리를 만든다.

## 2. 사용자 테이블 매핑

### 2.1 레거시 기준

레거시 `tb_user` 주요 컬럼:

- `SEQ`
- `ID`
- `PASSWORD`
- `LANG_CD`
- `COMPANY_CD`
- `COMPANY_NM`
- `EMAIL`
- `TEL`
- `AUTH_GROUP`
- `AUTH_LEVEL`
- `DEL_FLAG`
- `CREATE_DATE`
- `UPDATE_DATE`
- `CREATE_USER`

### 2.2 차세대 기준

차세대 `TB_USR` 기본 컬럼:

- `ID`
- `TENANT_ID`
- `CO_ID`
- `EML`
- `PWD_HASH`
- `DSP_NM`
- `LOCL_CD`
- `STS_CD`
- `CRT_BY`
- `CRT_DTM`
- `UPD_BY`
- `UPD_DTM`

### 2.3 매핑 원칙

| 레거시 | 차세대 | 비고 |
| --- | --- | --- |
| `SEQ` | `ID` | PK 체계 전환 필요 |
| `ID` | 별도 로그인 ID 후보 | 차세대에서는 이메일 기반 로그인 여부 결정 필요 |
| `PASSWORD` | `PWD_HASH` | 그대로 계승 가능 |
| `LANG_CD` | `LOCL_CD` | 다국어 표준으로 전환 |
| `COMPANY_CD` | `CO_ID` 또는 `CO_CD` 브리지 | 회사 마스터 정리 필요 |
| `COMPANY_NM` | 회사 마스터 참조 | 중복 데이터 제거 대상 |
| `EMAIL` | `EML` | 표준 약어 적용 |
| `TEL` | `TEL_NO` 후보 | 차세대 확장 컬럼 검토 |
| `AUTH_GROUP` | Role/Authority 체계 | 권한모델 재설계 필요 |
| `AUTH_LEVEL` | Role Level 또는 승인레벨 | 직접 치환보다 규칙 재정의 권장 |
| `DEL_FLAG` | `STS_CD` 또는 `DEL_YN` | 삭제정책 통일 필요 |
| `CREATE_DATE` | `CRT_DTM` | 감사 컬럼 표준 |
| `UPDATE_DATE` | `UPD_DTM` | 감사 컬럼 표준 |
| `CREATE_USER` | `CRT_BY` | 감사 컬럼 표준 |

## 3. 접근 로그 / 변경 이력 분리 원칙

### 3.1 레거시 로그 테이블

`tb_log_history`는 다음 성격을 가진다.

- 요청 헤더
- 원격 IP
- 요청 URL
- 요청 파라미터
- 엔드포인트
- 인터페이스 성공 여부
- 사용자
- 회사
- 권한

즉 이 테이블은 **데이터 변경이력**이 아니라 **접근/호출 로그**에 가깝다.

### 3.2 차세대 분리 원칙

차세대에서는 로그를 두 종류로 분리한다.

1. **데이터 변경이력**
   - `TB_*_HIST`
   - DB Trigger 기반
   - C/U/D 원장

2. **요청/접근 로그**
   - 별도 `TB_API_LOG` 또는 `TB_ACCESS_LOG` 계열
   - Java단 AOP/Interceptor 기반
   - 화면/요청/사용자 문맥 저장

즉 `tb_log_history`는 `_HIST`로 흡수하는 것이 아니라,
차세대에서는 별도 운영 로그 영역으로 분리하는 것이 맞다.

## 4. 회사 / 테넌트 매핑 방향

레거시 구조는 `COMPANY_CD`, `COMPANY_NM`가 여러 테이블에 직접 반복되는 형태다.

차세대에서는 이를 다음처럼 분리한다.

- `TB_TENANT`
- `TB_CO`
- `TB_USR`

즉 회사/사용자/권한 관계를 공통 마스터로 끌어올리고,
개별 업무 테이블은 참조형으로 전환한다.

## 5. 권한 체계 매핑 방향

레거시:

- `AUTH_GROUP`
- `AUTH_LEVEL`

차세대:

- `TB_ROLE`
- `TB_USR_ROLE`

즉 문자열 플래그 중심 권한을
역할 기반 권한모델로 승격시키는 것이 목표다.

## 6. 결론

공통축 업그레이드의 핵심은 다음이다.

- 사용자 테이블을 바로 갈아엎지 않는다.
- 로그인/회사/권한 의미를 먼저 분해한다.
- 그 다음 차세대 마스터 구조로 안전하게 재배치한다.

즉 첫 번째 실제 포팅 작업은 테이블 rename보다
**의미 체계 정리와 브리지 설계**가 먼저다.
