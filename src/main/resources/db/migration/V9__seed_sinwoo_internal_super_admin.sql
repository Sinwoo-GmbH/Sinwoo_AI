UPDATE TB_TENANT
SET TENANT_TP_CD = 'INTERNAL',
    BILL_FREE_YN = 'Y',
    EML_DOMN = 'sinwoo-itc.com',
    UPD_BY = 'SYSTEM'
WHERE TENANT_CD = 'SINWOO_INTERNAL';

INSERT INTO TB_USR (
    TENANT_ID,
    CO_ID,
    LGN_ID,
    EML,
    PWD_HASH,
    DSP_NM,
    LOCL_CD,
    TEL_NO,
    AUTH_GRP_CD,
    AUTH_LVL_CD,
    STS_CD,
    CRT_BY,
    UPD_BY
)
SELECT
    t.ID,
    NULL,
    'GGAMGANG',
    'ggamgang@sinwoo-itc.com',
    '$2a$10$HBCOVsUYyfjM2vURH1hir.1s8GB53rxsEh04POAm3/G8m19PIkSG6',
    'Ggamgang Super Admin',
    'ko',
    NULL,
    'ADMIN',
    'PASSWORD',
    'ACTIVE',
    'SYSTEM',
    'SYSTEM'
FROM TB_TENANT t
WHERE t.TENANT_CD = 'SINWOO_INTERNAL'
  AND NOT EXISTS (
      SELECT 1
      FROM TB_USR u
      WHERE u.TENANT_ID = t.ID
        AND u.EML = 'ggamgang@sinwoo-itc.com'
  );

UPDATE TB_USR u
JOIN TB_TENANT t ON t.ID = u.TENANT_ID
SET u.LGN_ID = 'GGAMGANG',
    u.EML = 'ggamgang@sinwoo-itc.com',
    u.PWD_HASH = '$2a$10$HBCOVsUYyfjM2vURH1hir.1s8GB53rxsEh04POAm3/G8m19PIkSG6',
    u.DSP_NM = 'Ggamgang Super Admin',
    u.LOCL_CD = 'ko',
    u.AUTH_GRP_CD = 'ADMIN',
    u.AUTH_LVL_CD = 'PASSWORD',
    u.STS_CD = 'ACTIVE',
    u.UPD_BY = 'SYSTEM'
WHERE t.TENANT_CD = 'SINWOO_INTERNAL'
  AND u.EML = 'ggamgang@sinwoo-itc.com';

INSERT INTO TB_USR_ROLE (
    USR_ID,
    ROLE_ID,
    CRT_BY,
    UPD_BY
)
SELECT
    u.ID,
    r.ID,
    'SYSTEM',
    'SYSTEM'
FROM TB_USR u
JOIN TB_TENANT t ON t.ID = u.TENANT_ID
JOIN TB_ROLE r ON r.ROLE_CD = 'ROLE_PLATFORM_SUPER_ADMIN'
WHERE t.TENANT_CD = 'SINWOO_INTERNAL'
  AND u.EML = 'ggamgang@sinwoo-itc.com'
  AND NOT EXISTS (
      SELECT 1
      FROM TB_USR_ROLE ur
      WHERE ur.USR_ID = u.ID
        AND ur.ROLE_ID = r.ID
  );
