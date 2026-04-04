package com.sinwoo.billing.service;

import com.sinwoo.billing.dto.CreateSubscriptionRequest;
import com.sinwoo.billing.dto.SubscriptionListResponse;
import com.sinwoo.billing.dto.SubscriptionResponse;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);

    SubscriptionListResponse getSubscriptions(Long tenantId);
}
