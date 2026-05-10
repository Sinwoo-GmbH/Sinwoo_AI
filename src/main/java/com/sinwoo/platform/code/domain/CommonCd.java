package com.sinwoo.platform.code.domain;

import com.sinwoo.common.util.BaseEntity;
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
        name = "TB_CD",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_CD_GRP_ID_CD", columnNames = {"GRP_ID", "CD"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCd extends BaseEntity {

    @Column(name = "GRP_ID", nullable = false)
    private Long grpId;

    @Column(name = "CD", nullable = false, length = 100)
    private String cd;

    @Column(name = "CD_NM_KO", nullable = false, length = 255)
    private String cdNmKo;

    @Column(name = "CD_NM_EN", nullable = false, length = 255)
    private String cdNmEn;

    @Column(name = "CD_NM_DE", nullable = false, length = 255)
    private String cdNmDe;

    @Column(name = "CD_DESC_KO", columnDefinition = "TEXT")
    private String cdDescKo;

    @Column(name = "CD_DESC_EN", columnDefinition = "TEXT")
    private String cdDescEn;

    @Column(name = "CD_DESC_DE", columnDefinition = "TEXT")
    private String cdDescDe;

    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String useYn;

    @Column(name = "DSP_ORD", nullable = false)
    private Integer dspOrd;

    private CommonCd(
            Long grpId,
            String cd,
            String cdNmKo,
            String cdNmEn,
            String cdNmDe,
            String cdDescKo,
            String cdDescEn,
            String cdDescDe,
            String useYn,
            Integer dspOrd
    ) {
        this.grpId = grpId;
        this.cd = cd;
        this.cdNmKo = cdNmKo;
        this.cdNmEn = cdNmEn;
        this.cdNmDe = cdNmDe;
        this.cdDescKo = cdDescKo;
        this.cdDescEn = cdDescEn;
        this.cdDescDe = cdDescDe;
        this.useYn = useYn;
        this.dspOrd = dspOrd;
    }

    public static CommonCd create(
            Long grpId,
            String cd,
            String cdNmKo,
            String cdNmEn,
            String cdNmDe,
            String cdDescKo,
            String cdDescEn,
            String cdDescDe,
            String useYn,
            Integer dspOrd
    ) {
        return new CommonCd(grpId, cd, cdNmKo, cdNmEn, cdNmDe, cdDescKo, cdDescEn, cdDescDe, useYn, dspOrd);
    }

    public void update(
            String cdNmKo,
            String cdNmEn,
            String cdNmDe,
            String cdDescKo,
            String cdDescEn,
            String cdDescDe,
            String useYn,
            Integer dspOrd
    ) {
        this.cdNmKo = cdNmKo;
        this.cdNmEn = cdNmEn;
        this.cdNmDe = cdNmDe;
        this.cdDescKo = cdDescKo;
        this.cdDescEn = cdDescEn;
        this.cdDescDe = cdDescDe;
        this.useYn = useYn;
        this.dspOrd = dspOrd;
    }
}
