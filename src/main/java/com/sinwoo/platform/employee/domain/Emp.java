package com.sinwoo.platform.employee.domain;

import com.sinwoo.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "TB_EMP",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_EMP_TENANT_ID_CO_ID_EMP_NO", columnNames = {"TENANT_ID", "CO_ID", "EMP_NO"}),
                @UniqueConstraint(name = "UK_TB_EMP_USR_ID", columnNames = {"USR_ID"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emp extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID", nullable = false)
    private Long coId;

    @Column(name = "USR_ID")
    private Long usrId;

    @Column(name = "DEPT_ID")
    private Long deptId;

    @Column(name = "MGR_EMP_ID")
    private Long mgrEmpId;

    @Column(name = "EMP_NO", nullable = false, length = 100)
    private String empNo;

    @Column(name = "EMP_NM", nullable = false, length = 255)
    private String empNm;

    @Column(name = "TEAM_ROLE_CD", nullable = false, length = 20)
    private String teamRoleCd;

    @Column(name = "JOB_TTL_CD", length = 20)
    private String jobTtlCd;

    @Column(name = "EML", length = 255)
    private String eml;

    @Column(name = "EXP_ACC_CD")
    private Integer expAccCd;

    @Column(name = "SEX_CD", columnDefinition = "CHAR(1)")
    private String sexCd;

    @Column(name = "TEL_NO", length = 20)
    private String telNo;

    @Column(name = "BIRTH_DT")
    private LocalDate birthDt;

    @Column(name = "HIRE_DT")
    private LocalDate hireDt;

    @Column(name = "RETR_DT")
    private LocalDate retrDt;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    private Emp(
            Long tenantId,
            Long coId,
            Long usrId,
            Long deptId,
            Long mgrEmpId,
            String empNo,
            String empNm,
            String teamRoleCd,
            String jobTtlCd,
            LocalDate hireDt,
            LocalDate retrDt,
            String stsCd
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.usrId = usrId;
        this.deptId = deptId;
        this.mgrEmpId = mgrEmpId;
        this.empNo = empNo;
        this.empNm = empNm;
        this.teamRoleCd = teamRoleCd;
        this.jobTtlCd = jobTtlCd;
        this.hireDt = hireDt;
        this.retrDt = retrDt;
        this.stsCd = stsCd;
    }

    public static Emp create(
            Long tenantId,
            Long coId,
            Long usrId,
            Long deptId,
            Long mgrEmpId,
            String empNo,
            String empNm,
            String teamRoleCd,
            String jobTtlCd,
            LocalDate hireDt,
            LocalDate retrDt,
            String stsCd
    ) {
        return new Emp(tenantId, coId, usrId, deptId, mgrEmpId, empNo, empNm, teamRoleCd, jobTtlCd, hireDt, retrDt, stsCd);
    }
}
