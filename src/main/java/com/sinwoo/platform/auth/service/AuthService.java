package com.sinwoo.platform.auth.service;

import com.sinwoo.platform.auth.dto.AuthProviderListResponse;
import com.sinwoo.platform.auth.dto.AuthTokenResponse;
import com.sinwoo.platform.auth.dto.CredKeyResponse;
import com.sinwoo.platform.auth.dto.CredLoginRequest;
import com.sinwoo.platform.auth.dto.CurrentUsrResponse;
import com.sinwoo.common.security.AuthenticatedUsr;
import java.util.Map;

public interface AuthService {

    AuthProviderListResponse getOauthProviders();

    CredKeyResponse getCredKey();

    AuthTokenResponse loginWithCreds(CredLoginRequest request);

    AuthTokenResponse completeOauthLogin(String registrationId, String tenantCd, Map<String, Object> attributes);

    CurrentUsrResponse getCurrentUsr(AuthenticatedUsr authenticatedUsr);
}
