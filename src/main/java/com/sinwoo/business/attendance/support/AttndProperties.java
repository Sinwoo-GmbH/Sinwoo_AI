package com.sinwoo.business.attendance.support;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.attendance")
public record AttndProperties(
        String bizTmznId,
        String dfltChkinTm,
        String dfltChkoutTm,
        String dfltCtryCd,
        String dfltRegionCd,
        String holiSource,
        String holiApiUrl
) {

    public ZoneId bizZoneId() {
        return ZoneId.of(bizTmznId);
    }
}
