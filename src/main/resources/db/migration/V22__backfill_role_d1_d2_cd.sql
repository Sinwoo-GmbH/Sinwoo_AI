-- ============================================================
-- V22: V19에서 누락된 ROLE_D1_CD/ROLE_D2_CD/ROLE_GRP_CD 채움
-- ============================================================
-- 사유: 옛 14개 ROLE에는 ROLE_D1_CD/D2_CD가 있었고, MnuServiceImpl.hasCustomerAdminRole()
--       및 BILL_GATE 체크에서 이를 사용. V19에서 새 5개 ROLE을 만들 때 NULL로 둬서
--       Team Management / HR / Finance 등 12개 메뉴가 안 보이게 되는 회귀 발생.
-- 매핑:
--   PADM  → D1=PLATFORM,  D2=ADMIN
--   CADM  → D1=CUSTOMER,  D2=ADMIN
--   HR    → D1=CUSTOMER,  D2=HR
--   FI    → D1=CUSTOMER,  D2=FI
--   USR   → D1=CUSTOMER,  D2=USER
-- ============================================================

UPDATE TB_ROLE SET ROLE_D1_CD = 'PLATFORM', ROLE_D2_CD = 'ADMIN', ROLE_GRP_CD = 'PLATFORM' WHERE ROLE_CD = 'PADM';
UPDATE TB_ROLE SET ROLE_D1_CD = 'CUSTOMER', ROLE_D2_CD = 'ADMIN', ROLE_GRP_CD = 'CUSTOMER' WHERE ROLE_CD = 'CADM';
UPDATE TB_ROLE SET ROLE_D1_CD = 'CUSTOMER', ROLE_D2_CD = 'HR',    ROLE_GRP_CD = 'CUSTOMER' WHERE ROLE_CD = 'HR';
UPDATE TB_ROLE SET ROLE_D1_CD = 'CUSTOMER', ROLE_D2_CD = 'FI',    ROLE_GRP_CD = 'CUSTOMER' WHERE ROLE_CD = 'FI';
UPDATE TB_ROLE SET ROLE_D1_CD = 'CUSTOMER', ROLE_D2_CD = 'USER',  ROLE_GRP_CD = 'CUSTOMER' WHERE ROLE_CD = 'USR';
