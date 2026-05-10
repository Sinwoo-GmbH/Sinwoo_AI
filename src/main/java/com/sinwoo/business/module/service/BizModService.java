package com.sinwoo.business.module.service;

import com.sinwoo.business.module.dto.BizModListResponse;
import com.sinwoo.business.module.dto.BizModResponse;
import com.sinwoo.common.security.AuthenticatedUsr;
import java.util.Locale;

public interface BizModService {

    BizModListResponse getBizMods(AuthenticatedUsr authenticatedUsr, Locale locale);

    BizModResponse getBizMod(AuthenticatedUsr authenticatedUsr, String modCd, Locale locale);
}
