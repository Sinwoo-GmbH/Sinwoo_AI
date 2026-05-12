package com.sinwoo.platform.menu.domain;

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
        name = "TB_ROLE_MNU_AUTH",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TB_ROLE_MNU_AUTH_ROLE_ID_MNU_ID", columnNames = {"ROLE_ID", "MNU_ID"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleMnuAuth extends BaseEntity {

    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;

    @Column(name = "MNU_ID", nullable = false)
    private Long mnuId;

    @Column(name = "VIEW_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String viewYn;

    @Column(name = "CRT_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String crtYn;

    @Column(name = "UPD_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String updYn;

    @Column(name = "DEL_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String delYn;

    @Column(name = "APRV_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String aprvYn;

    @Column(name = "EXPRT_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String exprtYn;

    private RoleMnuAuth(
            Long roleId,
            Long mnuId,
            String viewYn,
            String crtYn,
            String updYn,
            String delYn,
            String aprvYn,
            String exprtYn
    ) {
        this.roleId = roleId;
        this.mnuId = mnuId;
        this.viewYn = viewYn;
        this.crtYn = crtYn;
        this.updYn = updYn;
        this.delYn = delYn;
        this.aprvYn = aprvYn;
        this.exprtYn = exprtYn;
    }

    public static RoleMnuAuth create(
            Long roleId,
            Long mnuId,
            String viewYn,
            String crtYn,
            String updYn,
            String delYn,
            String aprvYn,
            String exprtYn
    ) {
        return new RoleMnuAuth(roleId, mnuId, viewYn, crtYn, updYn, delYn, aprvYn, exprtYn);
    }
}
