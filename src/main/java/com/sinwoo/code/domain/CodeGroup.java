package com.sinwoo.code.domain;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "TB_CD_GRP",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_CD_GRP_GRP_CD", columnNames = "GRP_CD")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeGroup extends BaseEntity {

    @Column(name = "GRP_CD", nullable = false, length = 100)
    private String grpCd;

    @Column(name = "GRP_NM_KO", nullable = false, length = 255)
    private String grpNmKo;

    @Column(name = "GRP_NM_EN", nullable = false, length = 255)
    private String grpNmEn;

    @Column(name = "GRP_NM_DE", nullable = false, length = 255)
    private String grpNmDe;

    @Column(name = "SYS_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String sysYn;

    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String useYn;

    @Column(name = "DSP_ORD", nullable = false)
    private Integer dspOrd;

    private CodeGroup(
            String grpCd,
            String grpNmKo,
            String grpNmEn,
            String grpNmDe,
            String sysYn,
            String useYn,
            Integer dspOrd
    ) {
        this.grpCd = grpCd;
        this.grpNmKo = grpNmKo;
        this.grpNmEn = grpNmEn;
        this.grpNmDe = grpNmDe;
        this.sysYn = sysYn;
        this.useYn = useYn;
        this.dspOrd = dspOrd;
    }

    public static CodeGroup create(
            String grpCd,
            String grpNmKo,
            String grpNmEn,
            String grpNmDe,
            String sysYn,
            String useYn,
            Integer dspOrd
    ) {
        return new CodeGroup(grpCd, grpNmKo, grpNmEn, grpNmDe, sysYn, useYn, dspOrd);
    }

    public void update(
            String grpNmKo,
            String grpNmEn,
            String grpNmDe,
            String useYn,
            Integer dspOrd
    ) {
        this.grpNmKo = grpNmKo;
        this.grpNmEn = grpNmEn;
        this.grpNmDe = grpNmDe;
        this.useYn = useYn;
        this.dspOrd = dspOrd;
    }
}
