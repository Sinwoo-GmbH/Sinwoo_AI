package com.sinwoo.billing.repository;

import com.sinwoo.billing.domain.PaymentTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findAllByTenantIdOrderByCreatedAtDescIdDesc(Long tenantId);
}
