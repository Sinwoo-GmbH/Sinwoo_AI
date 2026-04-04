# SINWOO Legacy Upgrade Docs

## 목적

이 디렉터리는 `Sinwoo-New.zip` 기반 레거시 프로젝트를
현재 Sinwoo 차세대 표준으로 업그레이드하기 위한 공식 분석/전략 문서를 관리한다.

## 문서 목록

- `01_sinwoo_new_upgrade_assessment.md`
  - 레거시 기술 스택, 자산 규모, 구조 오염 지점, 유지/개선/위험 구간 분석

- `02_nextgen_upgrade_strategy.md`
  - 차세대 업그레이드 전략, 목표 아키텍처, 단계별 이행 원칙

- `03_legacy_asset_inventory.md`
  - 현재 레거시 프로젝트의 컨트롤러/화면/매퍼/SQL 자산 인벤토리

- `04_domain_porting_matrix.md`
  - 도메인별 유지/포팅/표준화 우선순위 매트릭스

- `05_core_domain_field_mapping.md`
  - 핵심 공통축(사용자/회사/권한/로그)의 레거시-차세대 매핑 초안

## 운영 원칙

- 레거시 원본은 분석용 기준 자산으로만 사용한다.
- 업그레이드는 “재개발”이 아니라 “차세대 이행”으로 본다.
- 기존 기능을 깨지 않는 점진 이행이 원칙이다.
