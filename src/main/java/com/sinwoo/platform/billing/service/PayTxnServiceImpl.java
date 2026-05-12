package com.sinwoo.platform.billing.service;

import static com.sinwoo.common.util.StringNormalizer.blankToNull;
import static com.sinwoo.common.util.StringNormalizer.blankToNullUpper;
import static com.sinwoo.common.util.StringNormalizer.defaultIfBlankUpper;

import com.sinwoo.platform.billing.domain.PayTxn;
import com.sinwoo.platform.billing.domain.Subscr;
import com.sinwoo.platform.billing.dto.CreatePayTxnRequest;
import com.sinwoo.platform.billing.dto.PayTxnListResponse;
import com.sinwoo.platform.billing.dto.PayTxnResponse;
import com.sinwoo.platform.billing.repository.PayTxnRepository;
import com.sinwoo.platform.billing.repository.SubscrRepository;
import com.sinwoo.platform.tenant.domain.Tenant;
import com.sinwoo.platform.tenant.repository.TenantRepository;
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
public class PayTxnServiceImpl implements PayTxnService {

    private final PayTxnRepository payTxnRepository;
    private final SubscrRepository subscrRepository;
    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public PayTxnResponse createPayTxn(CreatePayTxnRequest request) {
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found"));

        Subscr subscr = subscrRepository.findByIdAndTenantId(request.subsId(), request.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription not found in tenant"));

        if ("Y".equals(tenant.getBillFreeYn()) && request.payAmt() != null && request.payAmt().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Billing-free tenant cannot create paid transactions");
        }

        PayTxn payTxn = PayTxn.create(
                tenant.getId(),
                subscr.getId(),
                request.payTpCd().trim().toUpperCase(),
                defaultIfBlankUpper(request.payStsCd(), "READY"),
                request.payAmt() == null ? BigDecimal.ZERO : request.payAmt(),
                request.currCd().trim().toUpperCase(),
                blankToNullUpper(request.pgCd()),
                blankToNull(request.pgTxnNo()),
                request.aprvDtm(),
                blankToNull(request.failMsg())
        );

        return PayTxnResponse.from(payTxnRepository.save(payTxn));
    }

    @Override
    public PayTxnListResponse getPayTxns(Long tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant not found");
        }

        List<PayTxnResponse> items = payTxnRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(tenantId).stream()
                .map(PayTxnResponse::from)
                .toList();

        return new PayTxnListResponse(items.size(), items);
    }

}
