package com.sinwoo.auth.domain;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "TB_ROLE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseEntity {

    @Column(name = "ROLE_CD", nullable = false, unique = true, length = 100)
    private String roleCd;

    @Column(name = "ROLE_NM", nullable = false, length = 255)
    private String roleNm;

    @Column(name = "ROLE_SCOPE_CD", length = 20)
    private String roleScopeCd;

    @Column(name = "ROLE_D1_CD", length = 30)
    private String roleD1Cd;

    @Column(name = "ROLE_D2_CD", length = 30)
    private String roleD2Cd;

    @Column(name = "ROLE_D3_CD", length = 30)
    private String roleD3Cd;

    @Column(name = "ROLE_GRP_CD", length = 20)
    private String roleGrpCd;

    @Column(name = "ROLE_LVL_CD", length = 20)
    private String roleLvlCd;

    private Role(
            String roleCd,
            String roleNm,
            String roleScopeCd,
            String roleD1Cd,
            String roleD2Cd,
            String roleD3Cd,
            String roleGrpCd,
            String roleLvlCd
    ) {
        this.roleCd = roleCd;
        this.roleNm = roleNm;
        this.roleScopeCd = roleScopeCd;
        this.roleD1Cd = roleD1Cd;
        this.roleD2Cd = roleD2Cd;
        this.roleD3Cd = roleD3Cd;
        this.roleGrpCd = roleGrpCd;
        this.roleLvlCd = roleLvlCd;
    }

    public static Role create(String roleCd, String roleNm, String roleGrpCd, String roleLvlCd) {
        return new Role(roleCd, roleNm, null, null, null, null, roleGrpCd, roleLvlCd);
    }

    public static Role create(
            String roleCd,
            String roleNm,
            String roleScopeCd,
            String roleD1Cd,
            String roleD2Cd,
            String roleD3Cd,
            String roleGrpCd,
            String roleLvlCd
    ) {
        return new Role(roleCd, roleNm, roleScopeCd, roleD1Cd, roleD2Cd, roleD3Cd, roleGrpCd, roleLvlCd);
    }
}
