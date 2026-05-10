package com.sinwoo.common.audit.accesslog;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JdbcAccessLogService implements AccessLogService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(AccessLogEntry entry) {
        jdbcTemplate.update("""
                INSERT INTO TB_ACCESS_LOG (
                    REQ_ID,
                    TENANT_KEY,
                    USR_KEY,
                    HTTP_MTHD_CD,
                    REQ_URI,
                    REQ_QS_CNTS,
                    REQ_BODY_CNTS,
                    REQ_HDR_CNTS,
                    CLNT_IP,
                    RESP_STS_CD,
                    SUCC_YN,
                    ERR_MSG,
                    CRT_BY,
                    CRT_DTM,
                    UPD_BY,
                    UPD_DTM
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                entry.reqId(),
                entry.tenantKey(),
                entry.usrKey(),
                entry.httpMthdCd(),
                entry.reqUri(),
                entry.reqQsCnts(),
                entry.reqBodyCnts(),
                entry.reqHdrCnts(),
                entry.clntIp(),
                entry.respStsCd(),
                entry.succYn(),
                entry.errMsg(),
                entry.crtBy(),
                entry.crtDtm(),
                entry.updBy(),
                entry.updDtm()
        );
    }
}
