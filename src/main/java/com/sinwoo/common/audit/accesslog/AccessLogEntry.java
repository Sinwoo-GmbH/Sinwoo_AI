package com.sinwoo.common.audit.accesslog;

import java.time.OffsetDateTime;

public record AccessLogEntry(
        String reqId,
        String tenantKey,
        String usrKey,
        String httpMthdCd,
        String reqUri,
        String reqQsCnts,
        String reqBodyCnts,
        String reqHdrCnts,
        String clntIp,
        String respStsCd,
        String succYn,
        String errMsg,
        String crtBy,
        OffsetDateTime crtDtm,
        String updBy,
        OffsetDateTime updDtm
) {
}
