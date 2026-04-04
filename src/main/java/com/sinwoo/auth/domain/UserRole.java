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
@Table(name = "TB_USR_ROLE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole extends BaseEntity {

    @Column(name = "USR_ID", nullable = false)
    private Long usrId;

    @Column(name = "ROLE_ID", nullable = false)
    private Long roleId;

    private UserRole(Long usrId, Long roleId) {
        this.usrId = usrId;
        this.roleId = roleId;
    }

    public static UserRole create(Long usrId, Long roleId) {
        return new UserRole(usrId, roleId);
    }
}
