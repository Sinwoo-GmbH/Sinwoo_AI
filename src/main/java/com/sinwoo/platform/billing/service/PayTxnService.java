package com.sinwoo.platform.billing.service;

import com.sinwoo.platform.billing.dto.CreatePayTxnRequest;
import com.sinwoo.platform.billing.dto.PayTxnListResponse;
import com.sinwoo.platform.billing.dto.PayTxnResponse;

public interface PayTxnService {

    PayTxnResponse createPayTxn(CreatePayTxnRequest request);

    PayTxnListResponse getPayTxns(Long tenantId);
}
