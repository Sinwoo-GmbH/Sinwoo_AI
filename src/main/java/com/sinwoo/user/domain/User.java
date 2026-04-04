package com.sinwoo.user.domain;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "TB_USR")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_ID")
    private Long coId;

    @Column(name = "EML", nullable = false, unique = true, length = 255)
    private String eml;

    @Column(name = "PWD_HASH", nullable = false, length = 255)
    private String pwdHash;

    @Column(name = "DSP_NM", nullable = false, length = 255)
    private String dspNm;

    @Column(name = "LOCL_CD", nullable = false, length = 10)
    private String loclCd;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;
}
