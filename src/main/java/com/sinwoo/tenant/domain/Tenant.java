package com.sinwoo.tenant.domain;

import com.sinwoo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "TB_TENANT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tenant extends BaseEntity {

    @Column(name = "TENANT_CD", nullable = false, unique = true, length = 100)
    private String tenantCd;

    @Column(name = "TENANT_NM", nullable = false, length = 255)
    private String tenantNm;

    @Column(name = "TENANT_TP_CD", nullable = false, length = 20)
    private String tenantTpCd;

    @Column(name = "BILL_FREE_YN", nullable = false, length = 1, columnDefinition = "CHAR(1)")
    private String billFreeYn;

    @Column(name = "STS_CD", nullable = false, length = 20)
    private String stsCd;

    private Tenant(String tenantCd, String tenantNm, String tenantTpCd, String billFreeYn, TenantStatus tenantStatus) {
        this.tenantCd = tenantCd;
        this.tenantNm = tenantNm;
        this.tenantTpCd = tenantTpCd;
        this.billFreeYn = billFreeYn;
        this.stsCd = tenantStatus.name();
    }

    public static Tenant create(String tenantCd, String tenantNm, TenantStatus tenantStatus) {
        return new Tenant(tenantCd, tenantNm, "CUSTOMER", "N", tenantStatus);
    }

    public static Tenant create(String tenantCd, String tenantNm, String tenantTpCd, String billFreeYn, TenantStatus tenantStatus) {
        return new Tenant(tenantCd, tenantNm, tenantTpCd, billFreeYn, tenantStatus);
    }

    public TenantStatus getTenantStatus() {
        return TenantStatus.valueOf(stsCd);
    }
}
