package com.sinwoo.platform.mnu.domain;

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
        name = "TB_MNU",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_MNU_MNU_CD", columnNames = "MNU_CD")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mnu extends BaseEntity {

    @Column(name = "MNU_CD", nullable = false, length = 100)
    private String mnuCd;

    @Column(name = "MNU_NM_CD", length = 100)
    private String mnuNmCd;

    @Column(name = "MNU_NM", nullable = false, length = 255)
    private String mnuNm;

    @Column(name = "MNU_SCOPE_CD", nullable = false, length = 20)
    private String mnuScopeCd;

    @Column(name = "UP_MNU_ID")
    private Long upMnuId;

    @Column(name = "PATH_URI", length = 500)
    private String pathUri;

    @Column(name = "ICON_NM", length = 100)
    private String iconNm;

    @Column(name = "DSP_ORD", nullable = false)
    private Integer dspOrd;

    @Column(name = "USE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String useYn;

    @Column(name = "BILL_GATE_CD", length = 30)
    private String billGateCd;

    private Mnu(
            String mnuCd,
            String mnuNmCd,
            String mnuNm,
            String mnuScopeCd,
            Long upMnuId,
            String pathUri,
            String iconNm,
            Integer dspOrd,
            String useYn,
            String billGateCd
    ) {
        this.mnuCd = mnuCd;
        this.mnuNmCd = mnuNmCd;
        this.mnuNm = mnuNm;
        this.mnuScopeCd = mnuScopeCd;
        this.upMnuId = upMnuId;
        this.pathUri = pathUri;
        this.iconNm = iconNm;
        this.dspOrd = dspOrd;
        this.useYn = useYn;
        this.billGateCd = billGateCd;
    }

    public static Mnu create(
            String mnuCd,
            String mnuNmCd,
            String mnuNm,
            String mnuScopeCd,
            Long upMnuId,
            String pathUri,
            String iconNm,
            Integer dspOrd,
            String useYn,
            String billGateCd
    ) {
        return new Mnu(mnuCd, mnuNmCd, mnuNm, mnuScopeCd, upMnuId, pathUri, iconNm, dspOrd, useYn, billGateCd);
    }
}
