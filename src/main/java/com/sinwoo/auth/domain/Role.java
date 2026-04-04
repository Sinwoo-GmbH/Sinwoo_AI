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

    @Column(name = "ROLE_GRP_CD", length = 20)
    private String roleGrpCd;

    @Column(name = "ROLE_LVL_CD", length = 20)
    private String roleLvlCd;

    private Role(String roleCd, String roleNm, String roleGrpCd, String roleLvlCd) {
        this.roleCd = roleCd;
        this.roleNm = roleNm;
        this.roleGrpCd = roleGrpCd;
        this.roleLvlCd = roleLvlCd;
    }

    public static Role create(String roleCd, String roleNm, String roleGrpCd, String roleLvlCd) {
        return new Role(roleCd, roleNm, roleGrpCd, roleLvlCd);
    }
}
