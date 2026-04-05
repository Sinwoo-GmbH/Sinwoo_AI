package com.sinwoo.code.service;

import com.sinwoo.code.dto.CodeGroupListResponse;
import com.sinwoo.code.dto.CodeGroupResponse;
import com.sinwoo.code.dto.CommonCodeListResponse;
import com.sinwoo.code.dto.CommonCodeResponse;
import com.sinwoo.code.dto.CreateCodeGroupRequest;
import com.sinwoo.code.dto.CreateCommonCodeRequest;
import com.sinwoo.code.dto.UpdateCodeGroupRequest;
import com.sinwoo.code.dto.UpdateCommonCodeRequest;

public interface CommonCodeService {

    CodeGroupResponse createCodeGroup(CreateCodeGroupRequest request);

    CodeGroupResponse updateCodeGroup(Long grpId, UpdateCodeGroupRequest request);

    CodeGroupListResponse getCodeGroups();

    CommonCodeResponse createCode(CreateCommonCodeRequest request);

    CommonCodeResponse updateCode(Long cdId, UpdateCommonCodeRequest request);

    CommonCodeListResponse getCodes(String grpCd);

    String resolveDisplayName(String grpCd, String cd, String fallbackName);

    void ensureCode(String grpCd, String cd, String fallbackName);
}
