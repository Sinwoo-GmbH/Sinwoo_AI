package com.sinwoo.business.service;

import com.sinwoo.business.dto.BusinessRecordListResponse;
import com.sinwoo.business.dto.BusinessRecordQuery;
import com.sinwoo.business.dto.BusinessRecordResponse;
import com.sinwoo.business.dto.BusinessRecordSaveRequest;
import com.sinwoo.business.dto.BusinessRecordStatusRequest;
import com.sinwoo.business.dto.BusinessRelatedListResponse;
import com.sinwoo.common.security.AuthenticatedUser;
import java.util.Locale;

public interface BusinessRecordService {

    BusinessRecordListResponse getRecords(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            BusinessRecordQuery query,
            Locale locale
    );

    BusinessRecordResponse createRecord(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            BusinessRecordSaveRequest request
    );

    BusinessRecordResponse updateRecord(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            Long recordId,
            BusinessRecordSaveRequest request
    );

    BusinessRecordResponse updateRecordStatus(
            AuthenticatedUser authenticatedUser,
            String moduleCd,
            Long recordId,
            BusinessRecordStatusRequest request
    );

    void deleteRecord(AuthenticatedUser authenticatedUser, String moduleCd, Long recordId);

    BusinessRelatedListResponse getRelatedRecords(AuthenticatedUser authenticatedUser, String moduleCd, Long recordId);
}
