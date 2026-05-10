package com.sinwoo.platform.code.service;

import com.sinwoo.platform.code.dto.CdGroupListResponse;
import com.sinwoo.platform.code.dto.CdGroupResponse;
import com.sinwoo.platform.code.dto.CommonCdListResponse;
import com.sinwoo.platform.code.dto.CommonCdResponse;
import com.sinwoo.platform.code.dto.CreateCdGroupRequest;
import com.sinwoo.platform.code.dto.CreateCommonCdRequest;
import com.sinwoo.platform.code.dto.UpdateCdGroupRequest;
import com.sinwoo.platform.code.dto.UpdateCommonCdRequest;

public interface CommonCdService {

    CdGroupResponse createCdGroup(CreateCdGroupRequest request);

    CdGroupResponse updateCdGroup(Long grpId, UpdateCdGroupRequest request);

    CdGroupListResponse getCdGroups();

    CommonCdResponse createCd(CreateCommonCdRequest request);

    CommonCdResponse updateCd(Long cdId, UpdateCommonCdRequest request);

    CommonCdListResponse getCds(String grpCd);

    String resolveDspNm(String grpCd, String cd, String fallbackName);

    void ensureCd(String grpCd, String cd, String fallbackName);
}
