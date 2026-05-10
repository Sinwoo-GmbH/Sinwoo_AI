package com.sinwoo.business.attendance.service;

import com.sinwoo.business.attendance.dto.AttndRptExportFile;
import com.sinwoo.business.attendance.dto.AttndWorkTimeFiltOptsResponse;
import com.sinwoo.business.attendance.dto.AttndWorkTimeHistListResponse;
import com.sinwoo.business.attendance.dto.AttndWorkTimeHistQuery;
import com.sinwoo.common.security.AuthenticatedUsr;
import java.util.Locale;

public interface AttndRptService {

    AttndWorkTimeHistListResponse getWorkTimeHist(
            AuthenticatedUsr authenticatedUsr,
            AttndWorkTimeHistQuery query,
            Locale locale
    );

    AttndWorkTimeFiltOptsResponse getWorkTimeFiltOpts(AuthenticatedUsr authenticatedUsr);

    AttndRptExportFile exportWorkTimeHistExcel(
            AuthenticatedUsr authenticatedUsr,
            AttndWorkTimeHistQuery query,
            Locale locale
    );

    AttndRptExportFile exportWorkTimeHistPdf(
            AuthenticatedUsr authenticatedUsr,
            AttndWorkTimeHistQuery query,
            Locale locale
    );
}
