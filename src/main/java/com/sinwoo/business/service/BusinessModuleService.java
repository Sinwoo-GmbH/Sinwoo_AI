package com.sinwoo.business.service;

import com.sinwoo.business.dto.BusinessModuleListResponse;
import com.sinwoo.business.dto.BusinessModuleResponse;
import com.sinwoo.common.security.AuthenticatedUser;
import java.util.Locale;

public interface BusinessModuleService {

    BusinessModuleListResponse getBusinessModules(AuthenticatedUser authenticatedUser, Locale locale);

    BusinessModuleResponse getBusinessModule(AuthenticatedUser authenticatedUser, String moduleCd, Locale locale);
}
