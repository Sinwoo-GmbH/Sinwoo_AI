package com.sinwoo.auth.domain;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "TB_USR_OAUTH",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_USR_OAUTH_PROV_SUB", columnNames = {"OAUTH_PROV_CD", "OAUTH_SUB"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOauthIdentity extends BaseEntity {

    @Column(name = "USR_ID", nullable = false)
    private Long usrId;

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "OAUTH_PROV_CD", nullable = false, length = 30)
    private String oauthProvCd;

    @Column(name = "OAUTH_SUB", nullable = false, length = 255)
    private String oauthSub;

    @Column(name = "EML", length = 255)
    private String eml;

    @Column(name = "EML_VRFY_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String emlVrfyYn;

    @Column(name = "LAST_LGN_DTM")
    private OffsetDateTime lastLgnDtm;

    private UserOauthIdentity(
            Long usrId,
            Long tenantId,
            String oauthProvCd,
            String oauthSub,
            String eml,
            String emlVrfyYn,
            OffsetDateTime lastLgnDtm
    ) {
        this.usrId = usrId;
        this.tenantId = tenantId;
        this.oauthProvCd = oauthProvCd;
        this.oauthSub = oauthSub;
        this.eml = eml;
        this.emlVrfyYn = emlVrfyYn;
        this.lastLgnDtm = lastLgnDtm;
    }

    public static UserOauthIdentity create(
            Long usrId,
            Long tenantId,
            String oauthProvCd,
            String oauthSub,
            String eml,
            String emlVrfyYn,
            OffsetDateTime lastLgnDtm
    ) {
        return new UserOauthIdentity(usrId, tenantId, oauthProvCd, oauthSub, eml, emlVrfyYn, lastLgnDtm);
    }

    public void markLogin(String eml, String emlVrfyYn, OffsetDateTime loginDtm) {
        this.eml = eml;
        this.emlVrfyYn = emlVrfyYn;
        this.lastLgnDtm = loginDtm;
    }
}
