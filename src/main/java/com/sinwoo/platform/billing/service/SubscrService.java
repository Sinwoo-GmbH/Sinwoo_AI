package com.sinwoo.platform.billing.service;

import com.sinwoo.platform.billing.dto.CreateSubscrRequest;
import com.sinwoo.platform.billing.dto.SubscrListResponse;
import com.sinwoo.platform.billing.dto.SubscrResponse;

public interface SubscrService {

    SubscrResponse createSubscr(CreateSubscrRequest request);

    SubscrListResponse getSubscrs(Long tenantId);
}
