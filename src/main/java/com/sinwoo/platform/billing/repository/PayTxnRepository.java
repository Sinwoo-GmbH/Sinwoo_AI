package com.sinwoo.platform.billing.repository;

import com.sinwoo.platform.billing.domain.PayTxn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayTxnRepository extends JpaRepository<PayTxn, Long> {

    List<PayTxn> findAllByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);
}
