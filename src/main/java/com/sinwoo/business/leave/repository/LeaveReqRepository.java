package com.sinwoo.business.leave.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LeaveReqRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Optional<EmpCtx> findEmpCtx(Long tenantId, Long usrId) {
        String sql = """
                SELECT
                    e.ID AS EMP_ID,
                    e.TENANT_ID,
                    e.CO_ID,
                    e.USR_ID,
                    e.DEPT_ID,
                    e.EMP_NO,
                    e.EMP_NM,
                    e.JOB_TTL_NM,
                    d.DEPT_NM,
                    c.HQ_CTRY_CD,
                    c.HQ_REGION_CD,
                    hp.ID AS PROFILE_ID,
                    hp.STD_VAC_CNT,
                    hp.INC_VAC_CNT,
                    hp.CURR_VAC_CNT,
                    hp.PRE_VAC_CNT
                FROM TB_EMP e
                LEFT JOIN TB_DEPT d ON d.ID = e.DEPT_ID AND d.TENANT_ID = e.TENANT_ID AND d.CO_ID = e.CO_ID
                LEFT JOIN TB_CO c ON c.ID = e.CO_ID AND c.TENANT_ID = e.TENANT_ID
                LEFT JOIN TB_EMP_HR_PROFILE hp ON hp.EMP_ID = e.ID
                WHERE e.TENANT_ID = :tenantId
                  AND e.USR_ID = :usrId
                  AND e.STS_CD = 'ACTIVE'
                ORDER BY e.ID
                LIMIT 1
                """;
        List<EmpCtx> rows = jdbcTemplate.query(sql, Map.of("tenantId", tenantId, "usrId", usrId), empContextMapper());
        return rows.stream().findFirst();
    }

    public EmpCtx lockEmpProfile(Long tenantId, Long coId, Long empId) {
        String sql = """
                SELECT
                    e.ID AS EMP_ID,
                    e.TENANT_ID,
                    e.CO_ID,
                    e.USR_ID,
                    e.DEPT_ID,
                    e.EMP_NO,
                    e.EMP_NM,
                    e.JOB_TTL_NM,
                    d.DEPT_NM,
                    c.HQ_CTRY_CD,
                    c.HQ_REGION_CD,
                    hp.ID AS PROFILE_ID,
                    hp.STD_VAC_CNT,
                    hp.INC_VAC_CNT,
                    hp.CURR_VAC_CNT,
                    hp.PRE_VAC_CNT
                FROM TB_EMP e
                LEFT JOIN TB_DEPT d ON d.ID = e.DEPT_ID AND d.TENANT_ID = e.TENANT_ID AND d.CO_ID = e.CO_ID
                LEFT JOIN TB_CO c ON c.ID = e.CO_ID AND c.TENANT_ID = e.TENANT_ID
                JOIN TB_EMP_HR_PROFILE hp ON hp.EMP_ID = e.ID
                WHERE e.TENANT_ID = :tenantId
                  AND e.CO_ID = :coId
                  AND e.ID = :empId
                FOR UPDATE
                """;
        return jdbcTemplate.queryForObject(
                sql,
                Map.of("tenantId", tenantId, "coId", coId, "empId", empId),
                empContextMapper()
        );
    }

    public void createDefaultHrProfile(EmpCtx emp) {
        String sql = """
                INSERT INTO TB_EMP_HR_PROFILE (
                    TENANT_ID, CO_ID, EMP_ID, LEGACY_EMP_ID, WORK_STR_TM, WORK_END_TM,
                    STD_VAC_CNT, INC_VAC_CNT, CURR_VAC_CNT, PRE_VAC_CNT,
                    LEGACY_RESIGN_YN, STS_CD, CRT_BY, UPD_BY
                ) VALUES (
                    :tenantId, :coId, :empId, :legacyEmpId, '08:00:00', '17:00:00',
                    24.0, 0.0, 24.0, 0.0,
                    'N', 'ACTIVE', 'SYSTEM', 'SYSTEM'
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("tenantId", emp.tenantId())
                .addValue("coId", emp.coId())
                .addValue("empId", emp.empId())
                .addValue("legacyEmpId", emp.empNo()));
    }

    public void updateHrProfileBalance(Long profileId, BigDecimal preVacCnt, BigDecimal currVacCnt, String actor) {
        String sql = """
                UPDATE TB_EMP_HR_PROFILE
                SET PRE_VAC_CNT = :preVacCnt,
                    CURR_VAC_CNT = :currVacCnt,
                    UPD_BY = :actor,
                    UPD_DTM = CURRENT_TIMESTAMP
                WHERE ID = :profileId
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("profileId", profileId)
                .addValue("preVacCnt", preVacCnt)
                .addValue("currVacCnt", currVacCnt)
                .addValue("actor", actor));
    }

    public List<EmpCtx> findEmps(Long tenantId, Long coId) {
        String sql = """
                SELECT
                    e.ID AS EMP_ID,
                    e.TENANT_ID,
                    e.CO_ID,
                    e.USR_ID,
                    e.DEPT_ID,
                    e.EMP_NO,
                    e.EMP_NM,
                    e.JOB_TTL_NM,
                    d.DEPT_NM,
                    c.HQ_CTRY_CD,
                    c.HQ_REGION_CD,
                    hp.ID AS PROFILE_ID,
                    hp.STD_VAC_CNT,
                    hp.INC_VAC_CNT,
                    hp.CURR_VAC_CNT,
                    hp.PRE_VAC_CNT
                FROM TB_EMP e
                LEFT JOIN TB_DEPT d ON d.ID = e.DEPT_ID AND d.TENANT_ID = e.TENANT_ID AND d.CO_ID = e.CO_ID
                LEFT JOIN TB_CO c ON c.ID = e.CO_ID AND c.TENANT_ID = e.TENANT_ID
                LEFT JOIN TB_EMP_HR_PROFILE hp ON hp.EMP_ID = e.ID
                WHERE e.TENANT_ID = :tenantId
                  AND e.CO_ID = :coId
                  AND e.STS_CD = 'ACTIVE'
                ORDER BY d.DEPT_NM ASC, e.EMP_NM ASC, e.ID ASC
                """;
        return jdbcTemplate.query(sql, Map.of("tenantId", tenantId, "coId", coId), empContextMapper());
    }

    public List<DeptRow> findDepts(Long tenantId, Long coId) {
        String sql = """
                SELECT ID, DEPT_NM, UP_DEPT_ID
                FROM TB_DEPT
                WHERE TENANT_ID = :tenantId
                  AND CO_ID = :coId
                  AND STS_CD = 'ACTIVE'
                ORDER BY DEPT_LVL_NO ASC, DEPT_NM ASC, ID ASC
                """;
        return jdbcTemplate.query(sql, Map.of("tenantId", tenantId, "coId", coId), (rs, rowNum) ->
                new DeptRow(
                        rs.getLong("ID"),
                        rs.getString("DEPT_NM"),
                        nullableLong(rs, "UP_DEPT_ID")
                ));
    }

    public List<LeaveRow> findLeaveRows(
            Long tenantId,
            Long coId,
            Long empId,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            String statusCd
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    r.*,
                    e.EMP_NM,
                    e.EMP_NO,
                    d.DEPT_NM,
                    CASE
                        WHEN r.EMP_ID = :empId THEN 'RQT'
                        WHEN EXISTS (
                            SELECT 1
                            FROM TB_LEAVE_APRV a
                            WHERE a.LEAVE_REQ_ID = r.ID
                              AND a.APRV_EMP_ID = :empId
                              AND a.APRV_TP_CD = 'APP'
                        ) THEN 'APR'
                        WHEN EXISTS (
                            SELECT 1
                            FROM TB_LEAVE_APRV a
                            WHERE a.LEAVE_REQ_ID = r.ID
                              AND a.APRV_EMP_ID = :empId
                              AND a.APRV_TP_CD = 'REF'
                        ) THEN 'RFR'
                        ELSE 'RQT'
                    END AS MY_ROLE_CD
                FROM TB_LEAVE_REQ r
                LEFT JOIN TB_EMP e ON e.ID = r.EMP_ID
                LEFT JOIN TB_DEPT d ON d.ID = r.DEPT_ID
                WHERE r.TENANT_ID = :tenantId
                  AND r.CO_ID = :coId
                  AND (
                      r.EMP_ID = :empId
                      OR EXISTS (
                          SELECT 1
                          FROM TB_LEAVE_APRV a
                          WHERE a.LEAVE_REQ_ID = r.ID
                            AND a.APRV_EMP_ID = :empId
                      )
                  )
                """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("coId", coId)
                .addValue("empId", empId);
        if (startDateFrom != null) {
            sql.append(" AND r.STR_DT >= :startDateFrom");
            params.addValue("startDateFrom", startDateFrom);
        }
        if (startDateTo != null) {
            sql.append(" AND r.STR_DT <= :startDateTo");
            params.addValue("startDateTo", startDateTo);
        }
        if (statusCd != null && !statusCd.isBlank()) {
            if ("CANCELLED".equalsIgnoreCase(statusCd)) {
                sql.append(" AND r.STS_CD = 'CANCELLED'");
            } else {
                sql.append(" AND r.STS_CD = 'ACTIVE' AND r.APRV_STS_CD = :statusCd");
                params.addValue("statusCd", statusCd);
            }
        }
        sql.append(" ORDER BY r.STR_DT DESC, r.ID DESC");
        return jdbcTemplate.query(sql.toString(), params, leaveRowMapper());
    }

    public Optional<LeaveRow> findLeaveRow(Long tenantId, Long coId, Long leaveId) {
        String sql = """
                SELECT r.*, e.EMP_NM, e.EMP_NO, d.DEPT_NM, 'RQT' AS MY_ROLE_CD
                FROM TB_LEAVE_REQ r
                LEFT JOIN TB_EMP e ON e.ID = r.EMP_ID
                LEFT JOIN TB_DEPT d ON d.ID = r.DEPT_ID
                WHERE r.TENANT_ID = :tenantId
                  AND r.CO_ID = :coId
                  AND r.ID = :leaveId
                """;
        List<LeaveRow> rows = jdbcTemplate.query(
                sql,
                Map.of("tenantId", tenantId, "coId", coId, "leaveId", leaveId),
                leaveRowMapper()
        );
        return rows.stream().findFirst();
    }

    public LeaveRow lockLeaveRow(Long tenantId, Long coId, Long leaveId) {
        String sql = """
                SELECT r.*, e.EMP_NM, e.EMP_NO, d.DEPT_NM, 'RQT' AS MY_ROLE_CD
                FROM TB_LEAVE_REQ r
                LEFT JOIN TB_EMP e ON e.ID = r.EMP_ID
                LEFT JOIN TB_DEPT d ON d.ID = r.DEPT_ID
                WHERE r.TENANT_ID = :tenantId
                  AND r.CO_ID = :coId
                  AND r.ID = :leaveId
                FOR UPDATE
                """;
        return jdbcTemplate.queryForObject(
                sql,
                Map.of("tenantId", tenantId, "coId", coId, "leaveId", leaveId),
                leaveRowMapper()
        );
    }

    public Long insertLeave(LeaveWriteValues values) {
        String sql = """
                INSERT INTO TB_LEAVE_REQ (
                    TENANT_ID, CO_ID, DEPT_ID, EMP_ID,
                    LEGACY_DEPT_ID, LEGACY_EMP_ID,
                    VAC_TP_CD, DAY_TP_CD, DEDUCT_YN,
                    CURR_USE_VAC_CNT, PRE_USE_VAC_CNT, USE_VAC_CNT,
                    STR_DT, STR_TM, END_DT, END_TM,
                    APRV_TOT_LVL_NO, APRV_CURR_LVL_NO, APRV_STS_CD,
                    REQ_RSN_CNTS, REJ_RSN_CNTS, ATCH_FILE_NM,
                    STS_CD, CRT_BY, UPD_BY
                ) VALUES (
                    :tenantId, :coId, :deptId, :empId,
                    :legacyDeptId, :legacyEmpId,
                    :vacTpCd, :dayTpCd, :deductYn,
                    :currUseVacCnt, :preUseVacCnt, :useVacCnt,
                    :startDate, :startTime, :endDate, :endTime,
                    :aprvTotLvlNo, :aprvCurrLvlNo, :aprvStsCd,
                    :requestReason, '', :attachmentName,
                    'ACTIVE', :actor, :actor
                )
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, writeParams(values), keyHolder, new String[]{"ID"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Leave request key was not returned");
        }
        return key.longValue();
    }

    public void updateLeave(Long leaveId, LeaveWriteValues values) {
        String sql = """
                UPDATE TB_LEAVE_REQ
                SET VAC_TP_CD = :vacTpCd,
                    DAY_TP_CD = :dayTpCd,
                    DEDUCT_YN = :deductYn,
                    CURR_USE_VAC_CNT = :currUseVacCnt,
                    PRE_USE_VAC_CNT = :preUseVacCnt,
                    USE_VAC_CNT = :useVacCnt,
                    STR_DT = :startDate,
                    STR_TM = :startTime,
                    END_DT = :endDate,
                    END_TM = :endTime,
                    APRV_TOT_LVL_NO = :aprvTotLvlNo,
                    APRV_CURR_LVL_NO = :aprvCurrLvlNo,
                    APRV_STS_CD = :aprvStsCd,
                    REQ_RSN_CNTS = :requestReason,
                    REJ_RSN_CNTS = '',
                    ATCH_FILE_NM = :attachmentName,
                    STS_CD = 'ACTIVE',
                    UPD_BY = :actor,
                    UPD_DTM = CURRENT_TIMESTAMP
                WHERE ID = :leaveId
                """;
        MapSqlParameterSource params = writeParams(values).addValue("leaveId", leaveId);
        jdbcTemplate.update(sql, params);
    }

    public void updateLeaveApprovalStatus(Long leaveId, Integer currentLevel, String status, String rejectReason, String actor) {
        String sql = """
                UPDATE TB_LEAVE_REQ
                SET APRV_CURR_LVL_NO = :currentLevel,
                    APRV_STS_CD = :status,
                    REJ_RSN_CNTS = COALESCE(:rejectReason, REJ_RSN_CNTS),
                    UPD_BY = :actor,
                    UPD_DTM = CURRENT_TIMESTAMP
                WHERE ID = :leaveId
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("leaveId", leaveId)
                .addValue("currentLevel", currentLevel)
                .addValue("status", status)
                .addValue("rejectReason", rejectReason)
                .addValue("actor", actor));
    }

    public void cancelLeave(Long leaveId, String actor) {
        String sql = """
                UPDATE TB_LEAVE_REQ
                SET STS_CD = 'CANCELLED',
                    UPD_BY = :actor,
                    UPD_DTM = CURRENT_TIMESTAMP
                WHERE ID = :leaveId
                """;
        jdbcTemplate.update(sql, Map.of("leaveId", leaveId, "actor", actor));
    }

    public void deleteApprovals(Long leaveId) {
        jdbcTemplate.update("DELETE FROM TB_LEAVE_APRV WHERE LEAVE_REQ_ID = :leaveId", Map.of("leaveId", leaveId));
    }

    public void insertApproval(ApprovalWriteValues values) {
        String sql = """
                INSERT INTO TB_LEAVE_APRV (
                    LEAVE_REQ_ID, TENANT_ID, CO_ID, DEPT_ID, EMP_ID, APRV_EMP_ID,
                    LEGACY_EMP_ID, LEGACY_APRV_EMP_ID,
                    APRV_TP_CD, APRV_LVL_NO, APRV_STS_CD, APRV_YN, FINAL_YN,
                    STS_CD, CRT_BY, UPD_BY
                ) VALUES (
                    :leaveReqId, :tenantId, :coId, :deptId, :empId, :aprvEmpId,
                    :legacyEmpId, :legacyAprvEmpId,
                    :aprvTpCd, :aprvLvlNo, :aprvStsCd, :aprvYn, :finalYn,
                    'ACTIVE', :actor, :actor
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("leaveReqId", values.leaveReqId())
                .addValue("tenantId", values.tenantId())
                .addValue("coId", values.coId())
                .addValue("deptId", values.deptId())
                .addValue("empId", values.empId())
                .addValue("aprvEmpId", values.aprvEmpId())
                .addValue("legacyEmpId", values.legacyEmpId())
                .addValue("legacyAprvEmpId", values.legacyAprvEmpId())
                .addValue("aprvTpCd", values.aprvTpCd())
                .addValue("aprvLvlNo", values.aprvLvlNo())
                .addValue("aprvStsCd", values.aprvStsCd())
                .addValue("aprvYn", values.aprvYn())
                .addValue("finalYn", values.finalYn())
                .addValue("actor", values.actor()));
    }

    public List<ApprovalRow> findApprovals(Collection<Long> leaveIds) {
        if (leaveIds == null || leaveIds.isEmpty()) {
            return List.of();
        }
        String sql = """
                SELECT
                    a.*,
                    e.EMP_NM AS APRV_EMP_NM,
                    e.EMP_NO AS APRV_EMP_NO,
                    e.JOB_TTL_NM AS APRV_JOB_TTL_NM,
                    d.DEPT_NM AS APRV_DEPT_NM
                FROM TB_LEAVE_APRV a
                LEFT JOIN TB_EMP e ON e.ID = a.APRV_EMP_ID
                LEFT JOIN TB_DEPT d ON d.ID = e.DEPT_ID
                WHERE a.LEAVE_REQ_ID IN (:leaveIds)
                  AND a.STS_CD = 'ACTIVE'
                ORDER BY a.APRV_LVL_NO ASC, a.ID ASC
                """;
        return jdbcTemplate.query(sql, Map.of("leaveIds", leaveIds), approvalRowMapper());
    }

    public Optional<ApprovalRow> findCurrentApproval(Long leaveId, Long approverEmpId, Integer level) {
        String sql = """
                SELECT
                    a.*,
                    e.EMP_NM AS APRV_EMP_NM,
                    e.EMP_NO AS APRV_EMP_NO,
                    e.JOB_TTL_NM AS APRV_JOB_TTL_NM,
                    d.DEPT_NM AS APRV_DEPT_NM
                FROM TB_LEAVE_APRV a
                LEFT JOIN TB_EMP e ON e.ID = a.APRV_EMP_ID
                LEFT JOIN TB_DEPT d ON d.ID = e.DEPT_ID
                WHERE a.LEAVE_REQ_ID = :leaveId
                  AND a.APRV_EMP_ID = :approverEmpId
                  AND a.APRV_LVL_NO = :level
                  AND a.APRV_TP_CD = 'APP'
                  AND a.STS_CD = 'ACTIVE'
                LIMIT 1
                """;
        List<ApprovalRow> rows = jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("leaveId", leaveId)
                .addValue("approverEmpId", approverEmpId)
                .addValue("level", level), approvalRowMapper());
        return rows.stream().findFirst();
    }

    public void updateApprovalStatus(Long leaveId, Integer level, String status, String approvedYn, String actor) {
        String sql = """
                UPDATE TB_LEAVE_APRV
                SET APRV_STS_CD = :status,
                    APRV_YN = :approvedYn,
                    UPD_BY = :actor,
                    UPD_DTM = CURRENT_TIMESTAMP
                WHERE LEAVE_REQ_ID = :leaveId
                  AND APRV_LVL_NO = :level
                  AND APRV_TP_CD = 'APP'
                  AND STS_CD = 'ACTIVE'
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("leaveId", leaveId)
                .addValue("level", level)
                .addValue("status", status)
                .addValue("approvedYn", approvedYn)
                .addValue("actor", actor));
    }

    public void rejectApprovals(Long leaveId, String actor) {
        String sql = """
                UPDATE TB_LEAVE_APRV
                SET APRV_STS_CD = 'RJ',
                    APRV_YN = 'N',
                    UPD_BY = :actor,
                    UPD_DTM = CURRENT_TIMESTAMP
                WHERE LEAVE_REQ_ID = :leaveId
                  AND APRV_TP_CD = 'APP'
                  AND STS_CD = 'ACTIVE'
                  AND APRV_STS_CD <> 'RF'
                """;
        jdbcTemplate.update(sql, Map.of("leaveId", leaveId, "actor", actor));
    }

    public List<DuplicateRow> findDuplicates(Long tenantId, Long coId, Long empId, Long excludeLeaveId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT 'BT' AS DUP_TP, bt.ID AS DUP_ID, bt.STR_DT, bt.END_DT
                FROM TB_BIZ_TRIP bt
                WHERE bt.TENANT_ID = :tenantId
                  AND bt.CO_ID = :coId
                  AND bt.EMP_ID = :empId
                  AND bt.STS_CD = 'ACTIVE'
                  AND bt.APRV_STS_CD <> 'RJ'
                  AND :startDate <= bt.END_DT
                  AND :endDate >= bt.STR_DT
                UNION ALL
                SELECT 'VAC' AS DUP_TP, r.ID AS DUP_ID, r.STR_DT, r.END_DT
                FROM TB_LEAVE_REQ r
                WHERE r.TENANT_ID = :tenantId
                  AND r.CO_ID = :coId
                  AND r.EMP_ID = :empId
                  AND r.STS_CD = 'ACTIVE'
                  AND r.APRV_STS_CD <> 'RJ'
                  AND (:excludeLeaveId IS NULL OR r.ID <> :excludeLeaveId)
                  AND :startDate <= r.END_DT
                  AND :endDate >= r.STR_DT
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("coId", coId)
                .addValue("empId", empId)
                .addValue("excludeLeaveId", excludeLeaveId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate), (rs, rowNum) ->
                new DuplicateRow(
                        rs.getString("DUP_TP"),
                        rs.getLong("DUP_ID"),
                        localDate(rs, "STR_DT"),
                        localDate(rs, "END_DT")
                ));
    }

    public List<LocalDate> findRegionHolidays(String countryCd, String regionCd, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT HOLI_DT
                FROM TB_HOLI
                WHERE CTRY_CD = :countryCd
                  AND REGION_CD IN (:regionCds)
                  AND HOLI_DT BETWEEN :startDate AND :endDate
                """;
        List<String> regionCds = List.of("ALL", regionCd == null || regionCd.isBlank() ? "ALL" : regionCd);
        return jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("countryCd", countryCd)
                .addValue("regionCds", regionCds)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate), (rs, rowNum) -> localDate(rs, "HOLI_DT"));
    }

    public List<CoHoliRow> findCoHolidays(Long tenantId, Long coId, LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT HOLI_DT, ANNUAL_YN
                FROM TB_CO_HOLI
                WHERE TENANT_ID = :tenantId
                  AND CO_ID = :coId
                  AND USE_YN = 'Y'
                  AND (
                      ANNUAL_YN = 'Y'
                      OR HOLI_DT BETWEEN :startDate AND :endDate
                  )
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("tenantId", tenantId)
                .addValue("coId", coId)
                .addValue("startDate", startDate)
                .addValue("endDate", endDate), (rs, rowNum) ->
                new CoHoliRow(localDate(rs, "HOLI_DT"), rs.getString("ANNUAL_YN")));
    }

    private MapSqlParameterSource writeParams(LeaveWriteValues values) {
        return new MapSqlParameterSource()
                .addValue("tenantId", values.tenantId())
                .addValue("coId", values.coId())
                .addValue("deptId", values.deptId())
                .addValue("empId", values.empId())
                .addValue("legacyDeptId", values.legacyDeptId())
                .addValue("legacyEmpId", values.legacyEmpId())
                .addValue("vacTpCd", values.vacTpCd())
                .addValue("dayTpCd", values.dayTpCd())
                .addValue("deductYn", values.deductYn())
                .addValue("currUseVacCnt", values.currUseVacCnt())
                .addValue("preUseVacCnt", values.preUseVacCnt())
                .addValue("useVacCnt", values.useVacCnt())
                .addValue("startDate", values.startDate())
                .addValue("startTime", values.startTime())
                .addValue("endDate", values.endDate())
                .addValue("endTime", values.endTime())
                .addValue("aprvTotLvlNo", values.aprvTotLvlNo())
                .addValue("aprvCurrLvlNo", values.aprvCurrLvlNo())
                .addValue("aprvStsCd", values.aprvStsCd())
                .addValue("requestReason", values.requestReason())
                .addValue("attachmentName", values.attachmentName())
                .addValue("actor", values.actor());
    }

    private RowMapper<EmpCtx> empContextMapper() {
        return (rs, rowNum) -> new EmpCtx(
                rs.getLong("EMP_ID"),
                rs.getLong("TENANT_ID"),
                rs.getLong("CO_ID"),
                nullableLong(rs, "USR_ID"),
                nullableLong(rs, "DEPT_ID"),
                rs.getString("EMP_NO"),
                rs.getString("EMP_NM"),
                rs.getString("JOB_TTL_NM"),
                rs.getString("DEPT_NM"),
                rs.getString("HQ_CTRY_CD"),
                rs.getString("HQ_REGION_CD"),
                nullableLong(rs, "PROFILE_ID"),
                bigDecimal(rs, "STD_VAC_CNT"),
                bigDecimal(rs, "INC_VAC_CNT"),
                bigDecimal(rs, "CURR_VAC_CNT"),
                bigDecimal(rs, "PRE_VAC_CNT")
        );
    }

    private RowMapper<LeaveRow> leaveRowMapper() {
        return (rs, rowNum) -> new LeaveRow(
                rs.getLong("ID"),
                rs.getLong("TENANT_ID"),
                rs.getLong("CO_ID"),
                nullableLong(rs, "DEPT_ID"),
                nullableLong(rs, "EMP_ID"),
                rs.getString("LEGACY_DEPT_ID"),
                rs.getString("LEGACY_EMP_ID"),
                rs.getString("VAC_TP_CD"),
                rs.getString("DAY_TP_CD"),
                rs.getString("DEDUCT_YN"),
                bigDecimal(rs, "CURR_USE_VAC_CNT"),
                bigDecimal(rs, "PRE_USE_VAC_CNT"),
                bigDecimal(rs, "USE_VAC_CNT"),
                localDate(rs, "STR_DT"),
                localTime(rs, "STR_TM"),
                localDate(rs, "END_DT"),
                localTime(rs, "END_TM"),
                rs.getInt("APRV_TOT_LVL_NO"),
                rs.getInt("APRV_CURR_LVL_NO"),
                rs.getString("APRV_STS_CD"),
                rs.getString("REQ_RSN_CNTS"),
                rs.getString("REJ_RSN_CNTS"),
                rs.getString("ATCH_FILE_NM"),
                rs.getString("STS_CD"),
                rs.getString("CRT_BY"),
                localDateTime(rs, "CRT_DTM"),
                rs.getString("EMP_NM"),
                rs.getString("EMP_NO"),
                rs.getString("DEPT_NM"),
                rs.getString("MY_ROLE_CD")
        );
    }

    private RowMapper<ApprovalRow> approvalRowMapper() {
        return (rs, rowNum) -> new ApprovalRow(
                rs.getLong("ID"),
                rs.getLong("LEAVE_REQ_ID"),
                rs.getLong("TENANT_ID"),
                rs.getLong("CO_ID"),
                nullableLong(rs, "DEPT_ID"),
                nullableLong(rs, "EMP_ID"),
                nullableLong(rs, "APRV_EMP_ID"),
                rs.getString("LEGACY_EMP_ID"),
                rs.getString("LEGACY_APRV_EMP_ID"),
                rs.getString("APRV_TP_CD"),
                rs.getInt("APRV_LVL_NO"),
                rs.getString("APRV_STS_CD"),
                rs.getString("APRV_YN"),
                rs.getString("FINAL_YN"),
                rs.getString("APRV_EMP_NM"),
                rs.getString("APRV_EMP_NO"),
                rs.getString("APRV_JOB_TTL_NM"),
                rs.getString("APRV_DEPT_NM")
        );
    }

    private static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private static BigDecimal bigDecimal(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column);
        return value == null ? BigDecimal.ZERO : value;
    }

    private static LocalDate localDate(ResultSet rs, String column) throws SQLException {
        Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private static LocalTime localTime(ResultSet rs, String column) throws SQLException {
        Time value = rs.getTime(column);
        return value == null ? null : value.toLocalTime();
    }

    private static LocalDateTime localDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    public record EmpCtx(
            Long empId,
            Long tenantId,
            Long coId,
            Long usrId,
            Long deptId,
            String empNo,
            String empNm,
            String jobTitleNm,
            String deptNm,
            String countryCd,
            String regionCd,
            Long profileId,
            BigDecimal stdVacCnt,
            BigDecimal incVacCnt,
            BigDecimal currVacCnt,
            BigDecimal preVacCnt
    ) {
    }

    public record DeptRow(
            Long deptId,
            String deptNm,
            Long parentDeptId
    ) {
    }

    public record LeaveRow(
            Long id,
            Long tenantId,
            Long coId,
            Long deptId,
            Long empId,
            String legacyDeptId,
            String legacyEmpId,
            String vacTpCd,
            String dayTpCd,
            String deductYn,
            BigDecimal currUseVacCnt,
            BigDecimal preUseVacCnt,
            BigDecimal useVacCnt,
            LocalDate startDate,
            LocalTime startTime,
            LocalDate endDate,
            LocalTime endTime,
            Integer aprvTotLvlNo,
            Integer aprvCurrLvlNo,
            String aprvStsCd,
            String requestReason,
            String rejectReason,
            String attachmentName,
            String stsCd,
            String createdBy,
            LocalDateTime createdAt,
            String empNm,
            String empNo,
            String deptNm,
            String myRoleCd
    ) {
    }

    public record ApprovalRow(
            Long id,
            Long leaveReqId,
            Long tenantId,
            Long coId,
            Long deptId,
            Long empId,
            Long aprvEmpId,
            String legacyEmpId,
            String legacyAprvEmpId,
            String aprvTpCd,
            Integer aprvLvlNo,
            String aprvStsCd,
            String aprvYn,
            String finalYn,
            String approverName,
            String approverNo,
            String approverJobTitle,
            String approverDeptName
    ) {
    }

    public record LeaveWriteValues(
            Long tenantId,
            Long coId,
            Long deptId,
            Long empId,
            String legacyDeptId,
            String legacyEmpId,
            String vacTpCd,
            String dayTpCd,
            String deductYn,
            BigDecimal currUseVacCnt,
            BigDecimal preUseVacCnt,
            BigDecimal useVacCnt,
            LocalDate startDate,
            LocalTime startTime,
            LocalDate endDate,
            LocalTime endTime,
            Integer aprvTotLvlNo,
            Integer aprvCurrLvlNo,
            String aprvStsCd,
            String requestReason,
            String attachmentName,
            String actor
    ) {
    }

    public record ApprovalWriteValues(
            Long leaveReqId,
            Long tenantId,
            Long coId,
            Long deptId,
            Long empId,
            Long aprvEmpId,
            String legacyEmpId,
            String legacyAprvEmpId,
            String aprvTpCd,
            Integer aprvLvlNo,
            String aprvStsCd,
            String aprvYn,
            String finalYn,
            String actor
    ) {
    }

    public record DuplicateRow(
            String type,
            Long id,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    public record CoHoliRow(
            LocalDate holiDt,
            String annualYn
    ) {
    }
}
