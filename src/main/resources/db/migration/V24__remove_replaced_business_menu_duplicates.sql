-- Removes business menu rows that were replaced by existing TO-BE customer menus.
-- Remaining MNU_BIZ_* entries are only the AS-IS business leaves that do not have
-- an existing TO-BE menu equivalent and are already attached under Team/HR/Finance.

SET @MNU_GRP_ID = (SELECT ID FROM TB_CD_GRP WHERE GRP_CD = 'MNU_NM' LIMIT 1);

DELETE a
FROM TB_ROLE_MNU_AUTH a
JOIN TB_MNU m ON m.ID = a.MNU_ID
WHERE m.MNU_CD IN (
    'MNU_BIZ_REQUESTS',
    'MNU_BIZ_MY_CLAIMS',
    'MNU_BIZ_REPORTS',
    'MNU_BIZ_MASTER',
    'MNU_BIZ_REQ_WORK_TIME',
    'MNU_BIZ_REQ_LEAVE',
    'MNU_BIZ_MST_EMP',
    'MNU_BIZ_MST_DEPT',
    'MNU_BIZ_FIN_MGT_EXPENSE'
);

DELETE FROM TB_MNU
WHERE MNU_CD IN (
    'MNU_BIZ_REQ_WORK_TIME',
    'MNU_BIZ_REQ_LEAVE',
    'MNU_BIZ_MST_EMP',
    'MNU_BIZ_MST_DEPT',
    'MNU_BIZ_FIN_MGT_EXPENSE'
);

DELETE FROM TB_MNU
WHERE MNU_CD IN (
    'MNU_BIZ_REQUESTS',
    'MNU_BIZ_MY_CLAIMS',
    'MNU_BIZ_REPORTS',
    'MNU_BIZ_MASTER'
);

DELETE FROM TB_CD
WHERE GRP_ID = @MNU_GRP_ID
  AND CD IN (
      'MNU_BIZ_REQUESTS',
      'MNU_BIZ_MY_CLAIMS',
      'MNU_BIZ_REPORTS',
      'MNU_BIZ_MASTER',
      'MNU_BIZ_REQ_WORK_TIME',
      'MNU_BIZ_REQ_LEAVE',
      'MNU_BIZ_MST_EMP',
      'MNU_BIZ_MST_DEPT',
      'MNU_BIZ_FIN_MGT_EXPENSE'
  );
