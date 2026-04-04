package com.sinwoo.company.service;

import com.sinwoo.company.dto.CompanyListResponse;
import com.sinwoo.company.dto.CompanyResponse;
import com.sinwoo.company.dto.CreateCompanyRequest;

public interface CompanyService {

    CompanyResponse createCompany(CreateCompanyRequest request);

    CompanyListResponse getCompanies(Long tenantId);
}
