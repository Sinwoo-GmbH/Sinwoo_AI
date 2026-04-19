package com.sinwoo.attendance.domain;

import com.sinwoo.attendance.support.AttendanceStatusCd;
import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "TB_ATTND",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_ATTND_TENANT_ID_USR_ID_ATTND_DT", columnNames = {"TENANT_ID", "USR_ID", "ATTND_DT"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceRecord extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID", nullable = false)
    private Long coId;

    @Column(name = "USR_ID", nullable = false)
    private Long usrId;

    @Column(name = "EMP_ID")
    private Long empId;

    @Column(name = "ATTND_DT", nullable = false)
    private LocalDate attndDt;

    @Column(name = "CHKIN_DTM")
    private OffsetDateTime chkinDtm;

    @Column(name = "CHKOUT_DTM")
    private OffsetDateTime chkoutDtm;

    @Column(name = "ATTND_STS_CD", nullable = false, length = 20)
    private String attndStsCd;

    private AttendanceRecord(
            Long tenantId,
            Long coId,
            Long usrId,
            Long empId,
            LocalDate attndDt,
            OffsetDateTime chkinDtm,
            OffsetDateTime chkoutDtm,
            String attndStsCd
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.usrId = usrId;
        this.empId = empId;
        this.attndDt = attndDt;
        this.chkinDtm = chkinDtm;
        this.chkoutDtm = chkoutDtm;
        this.attndStsCd = attndStsCd;
    }

    public static AttendanceRecord createCheckedIn(
            Long tenantId,
            Long coId,
            Long usrId,
            Long empId,
            LocalDate attndDt,
            OffsetDateTime chkinDtm
    ) {
        return new AttendanceRecord(tenantId, coId, usrId, empId, attndDt, chkinDtm, null, AttendanceStatusCd.CHECKED_IN);
    }

    public static AttendanceRecord createManual(
            Long tenantId,
            Long coId,
            Long usrId,
            Long empId,
            LocalDate attndDt,
            OffsetDateTime chkinDtm,
            OffsetDateTime chkoutDtm,
            String attndStsCd
    ) {
        return new AttendanceRecord(
                tenantId,
                coId,
                usrId,
                empId,
                attndDt,
                chkinDtm,
                chkoutDtm,
                resolveManualStatus(chkinDtm, chkoutDtm, attndStsCd)
        );
    }

    public void applyCheckIn(OffsetDateTime value) {
        if (chkinDtm == null) {
            chkinDtm = value;
        }
        if (chkoutDtm == null) {
            attndStsCd = AttendanceStatusCd.CHECKED_IN;
        }
    }

    public void applyCheckOut(OffsetDateTime value) {
        if (chkinDtm == null || chkoutDtm != null) {
            return;
        }
        chkoutDtm = value;
        attndStsCd = AttendanceStatusCd.CHECKED_OUT;
    }

    public void applyManualEntry(OffsetDateTime checkInValue, OffsetDateTime checkOutValue, String attndStsCd) {
        chkinDtm = checkInValue;
        chkoutDtm = checkOutValue;
        this.attndStsCd = resolveManualStatus(checkInValue, checkOutValue, attndStsCd);
    }

    private static String resolveManualStatus(OffsetDateTime checkInValue, OffsetDateTime checkOutValue, String attndStsCd) {
        return AttendanceStatusCd.resolveManualStatus(checkInValue, checkOutValue, attndStsCd);
    }
}
