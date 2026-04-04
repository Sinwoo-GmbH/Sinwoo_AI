package com.sinwoo.company.domain;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "TB_CO")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "CO_NM", nullable = false, length = 255)
    private String coNm;

    @Column(name = "REG_NO", length = 100)
    private String regNo;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;
}
