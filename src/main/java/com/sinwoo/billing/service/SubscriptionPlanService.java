package com.sinwoo.billing.service;

import com.sinwoo.billing.dto.CreateSubscriptionPlanRequest;
import com.sinwoo.billing.dto.SubscriptionPlanListResponse;
import com.sinwoo.billing.dto.SubscriptionPlanResponse;

public interface SubscriptionPlanService {

    SubscriptionPlanResponse createSubscriptionPlan(CreateSubscriptionPlanRequest request);

    SubscriptionPlanListResponse getSubscriptionPlans();
}
