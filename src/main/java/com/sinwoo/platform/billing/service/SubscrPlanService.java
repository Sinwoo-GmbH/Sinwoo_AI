package com.sinwoo.platform.billing.service;

import com.sinwoo.platform.billing.dto.CreateSubscrPlanRequest;
import com.sinwoo.platform.billing.dto.SubscrPlanListResponse;
import com.sinwoo.platform.billing.dto.SubscrPlanResponse;

public interface SubscrPlanService {

    SubscrPlanResponse createSubscrPlan(CreateSubscrPlanRequest request);

    SubscrPlanListResponse getSubscrPlans();
}
