package com.sinwoo.business.module.service;

import com.sinwoo.business.module.dto.BizRecListResponse;
import com.sinwoo.business.module.dto.BizRecQuery;
import com.sinwoo.business.module.dto.BizRecResponse;
import com.sinwoo.business.module.dto.BizRecSaveRequest;
import com.sinwoo.business.module.dto.BizRecStatusRequest;
import com.sinwoo.business.module.dto.BizRelListResponse;
import com.sinwoo.common.security.AuthenticatedUsr;
import java.util.Locale;

public interface BizRecService {

    BizRecListResponse getRecs(
            AuthenticatedUsr authenticatedUsr,
            String modCd,
            BizRecQuery query,
            Locale locale
    );

    BizRecResponse createRec(
            AuthenticatedUsr authenticatedUsr,
            String modCd,
            BizRecSaveRequest request
    );

    BizRecResponse updateRec(
            AuthenticatedUsr authenticatedUsr,
            String modCd,
            Long recId,
            BizRecSaveRequest request
    );

    BizRecResponse updateRecStatus(
            AuthenticatedUsr authenticatedUsr,
            String modCd,
            Long recId,
            BizRecStatusRequest request
    );

    void deleteRec(AuthenticatedUsr authenticatedUsr, String modCd, Long recId);

    BizRelListResponse getRelRecs(AuthenticatedUsr authenticatedUsr, String modCd, Long recId);
}
