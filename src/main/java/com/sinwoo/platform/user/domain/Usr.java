package com.sinwoo.platform.user.domain;

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
        name = "TB_USR",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_USR_TENANT_ID_LGN_ID", columnNames = {"TENANT_ID", "LGN_ID"}),
                @UniqueConstraint(name = "UK_TB_USR_TENANT_ID_EML", columnNames = {"TENANT_ID", "EML"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usr extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID")
    private Long coId;

    @Column(name = "LGN_ID", nullable = false, length = 100)
    private String lgnId;

    @Column(name = "EML", nullable = false, length = 255)
    private String eml;

    @Column(name = "PWD_HASH", nullable = false, length = 255)
    private String pwdHash;

    @Column(name = "DSP_NM", nullable = false, length = 255)
    private String dspNm;

    @Column(name = "LOCL_CD", nullable = false, length = 10)
    private String loclCd;

    @Column(name = "TEL_NO", length = 30)
    private String telNo;

    @Column(name = "AUTH_GRP_CD", length = 20)
    private String authGrpCd;

    @Column(name = "AUTH_LVL_CD", length = 20)
    private String authLvlCd;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    private Usr(
            Long tenantId,
            Long coId,
            String lgnId,
            String eml,
            String pwdHash,
            String dspNm,
            String loclCd,
            String telNo,
            String authGrpCd,
            String authLvlCd,
            String stsCd
    ) {
        this.tenantId = tenantId;
        this.coId = coId;
        this.lgnId = lgnId;
        this.eml = eml;
        this.pwdHash = pwdHash;
        this.dspNm = dspNm;
        this.loclCd = loclCd;
        this.telNo = telNo;
        this.authGrpCd = authGrpCd;
        this.authLvlCd = authLvlCd;
        this.stsCd = stsCd;
    }

    public static Usr create(
            Long tenantId,
            Long coId,
            String lgnId,
            String eml,
            String pwdHash,
            String dspNm,
            String loclCd,
            String telNo,
            String authGrpCd,
            String authLvlCd,
            String stsCd
    ) {
        return new Usr(tenantId, coId, lgnId, eml, pwdHash, dspNm, loclCd, telNo, authGrpCd, authLvlCd, stsCd);
    }
}
