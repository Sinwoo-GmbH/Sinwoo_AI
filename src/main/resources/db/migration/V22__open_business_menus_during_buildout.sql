-- Opens the newly added AS-IS business menus for the TO-BE buildout phase.
-- The platform billing gate remains unchanged; only MNU_BIZ_* business menus are affected.

UPDATE TB_MNU
SET BILL_GATE_CD = NULL,
    UPD_BY = 'SYSTEM'
WHERE MNU_CD LIKE 'MNU_BIZ_%';
