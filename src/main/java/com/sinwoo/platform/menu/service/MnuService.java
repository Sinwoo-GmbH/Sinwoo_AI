package com.sinwoo.platform.mnu.service;

import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.mnu.dto.CreateMnuRequest;
import com.sinwoo.platform.mnu.dto.MnuListResponse;
import com.sinwoo.platform.mnu.dto.MnuResponse;
import com.sinwoo.platform.mnu.dto.MnuTreeResponse;
import java.util.List;

public interface MnuService {

    MnuResponse createMnu(CreateMnuRequest request);

    MnuListResponse getMnus(String mnuScopeCd);

    MnuTreeResponse getVisibleMnus(List<String> roleCds, String mnuScopeCd);

    MnuTreeResponse getVisibleMnusByUsr(Long usrId, String mnuScopeCd);

    MnuTreeResponse getVisibleMnusByLgnId(String tenantCd, String lgnId, String mnuScopeCd);

    MnuTreeResponse getVisibleMnusForCurrentUsr(AuthenticatedUsr authenticatedUsr, String mnuScopeCd);
}
