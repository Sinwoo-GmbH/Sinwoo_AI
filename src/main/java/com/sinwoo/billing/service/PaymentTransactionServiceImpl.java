package com.sinwoo.billing.service;

import com.sinwoo.billing.domain.PaymentTransaction;
import com.sinwoo.billing.domain.Subscription;
import com.sinwoo.billing.dto.CreatePaymentTransactionRequest;
import com.sinwoo.billing.dto.PaymentTransactionListResponse;
import com.sinwoo.billing.dto.PaymentTransactionResponse;
import com.sinwoo.billing.repository.PaymentTransactionRepository;
import com.sinwoo.billing.repository.SubscriptionRepository;
import com.sinwoo.tenant.domain.Tenant;
import com.sinwoo.tenant.repository.TenantRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public PaymentTransactionResponse createPaymentTransaction(CreatePaymentTransactionRequest request) {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));

        Subscription subscription = subscriptionRepository.findByIdAndTenantId(request.subsId(), request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription not found in tenant"));

        if ("Y".equals(tenant.getBillFreeYn()) && request.payAmt() != null && request.payAmt().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Billing-free tenant cannot create paid transactions");
        }

        PaymentTransaction transaction = PaymentTransaction.create(
                tenant.getId(),
                subscription.getId(),
                request.payTpCd().trim().toUpperCase(),
                normalizeStatus(request.payStsCd()),
                request.payAmt() == null ? BigDecimal.ZERO : request.payAmt(),
                request.currCd().trim().toUpperCase(),
                blankToNullUpper(request.pgCd()),
                blankToNull(request.pgTxnNo()),
                request.aprvDtm(),
                blankToNull(request.failMsg())
        );

        return PaymentTransactionResponse.from(paymentTransactionRepository.save(transaction));
    }

    @Override
    public PaymentTransactionListResponse getPaymentTransactions(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }

        List<PaymentTransactionResponse> items = paymentTransactionRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId).stream()
                .map(PaymentTransactionResponse::from)
                .toList();

        return new PaymentTransactionListResponse(items.size(), items);
    }

    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return "READY";
        }
        return value.trim().toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToNullUpper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }
}
