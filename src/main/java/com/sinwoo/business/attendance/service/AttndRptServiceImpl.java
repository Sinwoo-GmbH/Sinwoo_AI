package com.sinwoo.business.attendance.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sinwoo.business.attendance.domain.AttndRec;
import com.sinwoo.business.attendance.domain.HoliCache;
import com.sinwoo.business.attendance.dto.AttndRptExportFile;
import com.sinwoo.business.attendance.dto.AttndWorkTimeFiltOptResponse;
import com.sinwoo.business.attendance.dto.AttndWorkTimeFiltOptsResponse;
import com.sinwoo.business.attendance.dto.AttndWorkTimeHistListResponse;
import com.sinwoo.business.attendance.dto.AttndWorkTimeHistQuery;
import com.sinwoo.business.attendance.dto.AttndWorkTimeHistRowResponse;
import com.sinwoo.business.attendance.repository.AttndRecRepository;
import com.sinwoo.business.attendance.repository.HoliCacheRepository;
import com.sinwoo.business.attendance.support.AttndProperties;
import com.sinwoo.business.attendance.support.AttndStatusCd;
import com.sinwoo.platform.code.service.CommonCdService;
import com.sinwoo.platform.code.support.CommonCdGroupCd;
import com.sinwoo.common.security.AuthenticatedUsr;
import com.sinwoo.platform.company.domain.Co;
import com.sinwoo.platform.company.repository.CoRepository;
import com.sinwoo.platform.dept.domain.Dept;
import com.sinwoo.platform.dept.repository.DeptRepository;
import com.sinwoo.platform.emp.domain.Emp;
import com.sinwoo.platform.emp.repository.EmpRepository;
import com.sinwoo.platform.worklocation.domain.WorkLoc;
import com.sinwoo.platform.worklocation.repository.WorkLocRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttndRptServiceImpl implements AttndRptService {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter SAMPLE_PDF_MONTH_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int DEFAULT_BREAK_MINUTES = 60;
    private static final List<String> WINDOWS_KOREAN_FONT_PATHS = List.of(
            "C:/Windows/Fonts/malgun.ttf",
            "C:/Windows/Fonts/NanumGothic.ttf"
    );
    private static final Set<String> CUSTOMER_ADMIN_ROLE_CDS = Set.of(
            "ROLE_CUSTOMER_ADMIN_MEMBER",
            "ROLE_CUSTOMER_ADMIN_LEADER"
    );

    private final AttndRecRepository attndRecRepository;
    private final HoliCacheRepository holiCacheRepository;
    private final EmpRepository empRepository;
    private final DeptRepository deptRepository;
    private final CoRepository coRepository;
    private final WorkLocRepository workLocRepository;
    private final CommonCdService commonCdService;
    private final AttndProperties attndProperties;

    @Override
    public AttndWorkTimeHistListResponse getWorkTimeHist(
            AuthenticatedUsr authenticatedUsr,
            AttndWorkTimeHistQuery query,
            Locale locale
    ) {
        RptCtx rptCtx = buildRptCtx(authenticatedUsr);
        YearMonth targetMonth = resolveTargetMonth(query.yearMonth());
        List<AttndHistRow> rows = findHistRows(rptCtx, targetMonth, locale);
        List<AttndHistRow> filteredRows = filterRows(rows, query);

        return new AttndWorkTimeHistListResponse(
                targetMonth.format(MONTH_FORMAT),
                rptCtx.ownOnlyYn(),
                filteredRows.size(),
                filteredRows.stream()
                        .map(this::toHistRowResponse)
                        .toList()
        );
    }

    @Override
    public AttndWorkTimeFiltOptsResponse getWorkTimeFiltOpts(AuthenticatedUsr authenticatedUsr) {
        RptCtx rptCtx = buildRptCtx(authenticatedUsr);
        List<AttndWorkTimeFiltOptResponse> empOpts = rptCtx.empList().stream()
                .map(emp -> {
                    Dept dept = emp.getDeptId() == null ? null : rptCtx.deptById().get(emp.getDeptId());
                    return new AttndWorkTimeFiltOptResponse(
                            emp.getId(),
                            emp.getEmpNo(),
                            emp.getEmpNm(),
                            dept == null ? null : dept.getDeptNm()
                    );
                })
                .toList();
        List<AttndWorkTimeFiltOptResponse> deptOpts = rptCtx.deptList().stream()
                .map(dept -> new AttndWorkTimeFiltOptResponse(
                        dept.getId(),
                        dept.getDeptCd(),
                        dept.getDeptNm(),
                        null
                ))
                .toList();

        return new AttndWorkTimeFiltOptsResponse(
                rptCtx.ownOnlyYn(),
                empOpts.size(),
                empOpts,
                deptOpts.size(),
                deptOpts
        );
    }

    @Override
    public AttndRptExportFile exportWorkTimeHistExcel(
            AuthenticatedUsr authenticatedUsr,
            AttndWorkTimeHistQuery query,
            Locale locale
    ) {
        MonthlyExportData exportData = buildMonthlyExportData(authenticatedUsr, query, locale);
        byte[] cnt = writeExcel(exportData);
        return new AttndRptExportFile(
                "Work Time List(" + exportData.yearMonth().format(MONTH_FORMAT) + ").xlsx",
                EXCEL_CONTENT_TYPE,
                cnt
        );
    }

    @Override
    public AttndRptExportFile exportWorkTimeHistPdf(
            AuthenticatedUsr authenticatedUsr,
            AttndWorkTimeHistQuery query,
            Locale locale
    ) {
        MonthlyExportData exportData = buildMonthlyExportData(authenticatedUsr, query, locale);
        byte[] cnt = writePdf(exportData, locale);
        return new AttndRptExportFile(
                buildPdfExportFileName(exportData),
                "application/pdf",
                cnt
        );
    }

    private MonthlyExportData buildMonthlyExportData(
            AuthenticatedUsr authenticatedUsr,
            AttndWorkTimeHistQuery query,
            Locale locale
    ) {
        RptCtx rptCtx = buildRptCtx(authenticatedUsr);
        YearMonth targetMonth = resolveTargetMonth(query.yearMonth());
        List<AttndHistRow> rows = findHistRows(rptCtx, targetMonth, locale);
        List<AttndHistRow> filteredRows = filterRows(rows, query);
        Map<LocalDate, HoliCache> holidayByDate = loadHolidayByDate(rptCtx.user(), targetMonth);
        List<CalDayMeta> calDays = buildCalDays(targetMonth, holidayByDate);
        List<EmpScopeRow> empRows = resolveEmpScopeRows(rptCtx, filteredRows, query);

        Map<String, Map<LocalDate, AttndHistRow>> historyByEmp = new LinkedHashMap<>();
        for (AttndHistRow row : filteredRows) {
            historyByEmp.computeIfAbsent(resolveEmpKey(row), ignored -> new LinkedHashMap<>())
                    .put(row.attndDt(), row);
        }

        List<EmpMonthSheet> empSheets = new ArrayList<>();
        int ordinal = 1;
        for (EmpScopeRow empRow : empRows) {
            empSheets.add(buildEmpMonthSheet(
                    ordinal++,
                    empRow,
                    calDays,
                    historyByEmp.getOrDefault(empRow.empKey(), Map.of())
            ));
        }

        return new MonthlyExportData(targetMonth, rptCtx.ownOnlyYn(), calDays, empSheets);
    }

    private RptCtx buildRptCtx(AuthenticatedUsr authenticatedUsr) {
        AuthenticatedUsr user = requireUsr(authenticatedUsr);
        boolean elevated = hasElevatedRptAccess(user);
        Optional<Emp> emp = empRepository.findByUsrId(user.usrId());
        boolean ownOnlyYn = !elevated;
        Scope scope = resolveScope(user, emp, elevated);

        List<Emp> empList = loadEmps(user.tenantId(), scope);
        List<Dept> deptList = loadDepts(user.tenantId(), scope);
        Map<Long, Emp> empById = empList.stream()
                .collect(Collectors.toMap(Emp::getId, value -> value, (left, right) -> left, LinkedHashMap::new));
        Map<Long, Dept> deptById = deptList.stream()
                .collect(Collectors.toMap(Dept::getId, value -> value, (left, right) -> left, LinkedHashMap::new));

        return new RptCtx(user, ownOnlyYn, scope, emp.orElse(null), empList, deptList, empById, deptById);
    }

    private Scope resolveScope(AuthenticatedUsr user, Optional<Emp> emp, boolean elevated) {
        if (!elevated) {
            Long companyId = emp.map(Emp::getCoId).orElse(user.coId());
            return new Scope(companyId == null ? Set.of() : Set.of(companyId), false);
        }

        if (hasPlatformRole(user)) {
            return new Scope(Set.of(), true);
        }

        Long companyId = user.coId();
        if (companyId == null && emp.isPresent()) {
            companyId = emp.get().getCoId();
        }
        if (companyId == null) {
            Set<Long> tenantCoIds = coRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(user.tenantId()).stream()
                    .map(Co::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return new Scope(tenantCoIds, tenantCoIds.isEmpty());
        }
        return new Scope(Set.of(companyId), false);
    }

    private List<Emp> loadEmps(Long tenantId, Scope scope) {
        if (scope.tenantWideYn()) {
            return empRepository.findAllByTenantIdOrderByEmpNmAscIdAsc(tenantId);
        }
        if (scope.coIdSet().isEmpty()) {
            return List.of();
        }
        return empRepository.findAllByTenantIdAndCoIdInOrderByEmpNmAscIdAsc(tenantId, scope.coIdSet());
    }

    private List<Dept> loadDepts(Long tenantId, Scope scope) {
        if (scope.tenantWideYn()) {
            return deptRepository.findAllByTenantIdOrderByDeptLvlNoAscDeptNmAscIdAsc(tenantId);
        }
        if (scope.coIdSet().isEmpty()) {
            return List.of();
        }
        return deptRepository.findAllByTenantIdAndCoIdInOrderByDeptLvlNoAscDeptNmAscIdAsc(tenantId, scope.coIdSet());
    }

    private List<AttndHistRow> findHistRows(RptCtx rptCtx, YearMonth targetMonth, Locale locale) {
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();
        List<AttndRec> records = loadAttndRecs(rptCtx, startDate, endDate);

        return records.stream()
                .map(record -> toHistRow(rptCtx, record, locale))
                .sorted(Comparator
                        .comparing(AttndHistRow::attndDt).reversed()
                        .thenComparing(AttndHistRow::empNm, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(AttndHistRow::attndId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private List<AttndRec> loadAttndRecs(RptCtx rptCtx, LocalDate startDate, LocalDate endDate) {
        if (rptCtx.ownOnlyYn()) {
            return attndRecRepository.findAllByTenantIdAndUsrIdAndAttndDtBetweenOrderByAttndDtDescIdDesc(
                    rptCtx.user().tenantId(),
                    rptCtx.user().usrId(),
                    startDate,
                    endDate
            );
        }

        if (rptCtx.scope().tenantWideYn() || rptCtx.scope().coIdSet().isEmpty()) {
            return attndRecRepository.findAllByTenantIdAndAttndDtBetweenOrderByAttndDtDescUsrIdAscIdDesc(
                    rptCtx.user().tenantId(),
                    startDate,
                    endDate
            );
        }

        return attndRecRepository.findAllByTenantIdAndCoIdInAndAttndDtBetweenOrderByAttndDtDescUsrIdAscIdDesc(
                rptCtx.user().tenantId(),
                rptCtx.scope().coIdSet(),
                startDate,
                endDate
        );
    }

    private AttndHistRow toHistRow(RptCtx rptCtx, AttndRec record, Locale locale) {
        Emp emp = record.getEmpId() == null ? null : rptCtx.empById().get(record.getEmpId());
        Dept dept = emp == null || emp.getDeptId() == null ? null : rptCtx.deptById().get(emp.getDeptId());
        String empNm = emp == null
                ? fallbackEmpName(rptCtx.user(), rptCtx.emp(), record)
                : emp.getEmpNm();
        String deptNm = dept == null ? null : dept.getDeptNm();
        String deptCd = dept == null ? null : dept.getDeptCd();
        String statusNm = resolveStatusName(record.getAttndStsCd(), locale);
        String checkInTm = formatTime(record.getChkinDtm());
        String checkOutTm = formatTime(record.getChkoutDtm());

        return new AttndHistRow(
                record.getId(),
                record.getAttndDt(),
                record.getUsrId(),
                emp == null ? record.getEmpId() : emp.getId(),
                emp == null ? null : emp.getEmpNo(),
                empNm,
                dept == null ? null : dept.getId(),
                deptCd,
                deptNm,
                record.getAttndStsCd(),
                statusNm,
                checkInTm,
                checkOutTm,
                calculateWorkMinuteCnt(record),
                buildKeywordText(record, emp, dept, statusNm, checkInTm, checkOutTm)
        );
    }

    private List<AttndHistRow> filterRows(List<AttndHistRow> rows, AttndWorkTimeHistQuery query) {
        String empNm = normalizeText(query.empNm());
        String deptNm = normalizeText(query.deptNm());
        String keyword = normalizeText(query.keyword());

        return rows.stream()
                .filter(row -> empNm == null || containsIgnoreCase(row.empNm(), empNm))
                .filter(row -> deptNm == null || containsIgnoreCase(row.deptNm(), deptNm))
                .filter(row -> keyword == null || containsIgnoreCase(row.keywordText(), keyword))
                .toList();
    }

    private AttndWorkTimeHistRowResponse toHistRowResponse(AttndHistRow row) {
        return new AttndWorkTimeHistRowResponse(
                row.attndId(),
                row.attndDt().format(DATE_FORMAT),
                row.usrId(),
                row.empId(),
                row.empNo(),
                row.empNm(),
                row.deptId(),
                row.deptNm(),
                row.attndStsCd(),
                row.attndStsNm(),
                row.chkinTm(),
                row.chkoutTm(),
                row.workMinuteCnt()
        );
    }

    private byte[] writeExcel(MonthlyExportData exportData) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Sheet1");
            int lastColIndex = 4 + exportData.calDays().size();

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            applyExcelBorder(headerStyle);

            CellStyle bodyStyle = workbook.createCellStyle();
            bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            applyExcelBorder(bodyStyle);

            CellStyle centeredBodyStyle = workbook.createCellStyle();
            centeredBodyStyle.cloneStyleFrom(bodyStyle);
            centeredBodyStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle saturdayStyle = workbook.createCellStyle();
            saturdayStyle.cloneStyleFrom(centeredBodyStyle);
            saturdayStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            saturdayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle sundayStyle = workbook.createCellStyle();
            sundayStyle.cloneStyleFrom(centeredBodyStyle);
            sundayStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            sundayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle holidayStyle = workbook.createCellStyle();
            holidayStyle.cloneStyleFrom(centeredBodyStyle);
            holidayStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            holidayStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastColIndex));
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Work Time List(" + exportData.yearMonth().format(MONTH_FORMAT) + ")");
            titleCell.setCellStyle(titleStyle);

            Row headerRow = sheet.createRow(1);
            String[] fixedHeaders = {"No", "ID", "Name", "Dept ID", "Dept Name"};
            for (int index = 0; index < fixedHeaders.length; index++) {
                Cell cell = headerRow.createCell(index);
                cell.setCellValue(fixedHeaders[index]);
                cell.setCellStyle(headerStyle);
            }
            for (int index = 0; index < exportData.calDays().size(); index++) {
                Cell cell = headerRow.createCell(index + 5);
                cell.setCellValue(exportData.calDays().get(index).dayLabel());
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 2;
            for (EmpMonthSheet rowData : exportData.empSheets()) {
                Row row = sheet.createRow(rowIndex++);
                writeExcelCell(row, 0, rowData.no(), centeredBodyStyle);
                writeExcelCell(row, 1, rowData.empNo(), bodyStyle);
                writeExcelCell(row, 2, rowData.empNm(), bodyStyle);
                writeExcelCell(row, 3, rowData.deptCd(), bodyStyle);
                writeExcelCell(row, 4, rowData.deptNm(), bodyStyle);

                for (int index = 0; index < rowData.dayCells().size(); index++) {
                    EmpDayCell dayCell = rowData.dayCells().get(index);
                    Cell cell = row.createCell(index + 5);
                    cell.setCellValue(nullToBlank(dayCell.matrixValue()));
                    if (dayCell.holiday()) {
                        cell.setCellStyle(holidayStyle);
                    } else if (dayCell.sunday()) {
                        cell.setCellStyle(sundayStyle);
                    } else if (dayCell.saturday()) {
                        cell.setCellStyle(saturdayStyle);
                    } else {
                        cell.setCellStyle(centeredBodyStyle);
                    }
                }
            }

            sheet.createFreezePane(5, 2);
            sheet.setColumnWidth(0, 2100);
            sheet.setColumnWidth(1, 3200);
            sheet.setColumnWidth(2, 6200);
            sheet.setColumnWidth(3, 3200);
            sheet.setColumnWidth(4, 7600);
            for (int index = 5; index <= lastColIndex; index++) {
                sheet.setColumnWidth(index, 1500);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export work time history to Excel", exception);
        }
    }

    private byte[] writePdf(MonthlyExportData exportData, Locale locale) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36f, 36f, 42f, 36f);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = resolvePdfFont(locale, 15f, Font.BOLD);
            Font subtitleFont = resolvePdfFont(locale, 12f, Font.BOLD);
            Font headerFont = resolvePdfFont(locale, 9f, Font.BOLD);
            Font bodyFont = resolvePdfFont(locale, 9f, Font.NORMAL);
            Font footnoteFont = resolvePdfFont(locale, 8f, Font.ITALIC);

            for (int index = 0; index < exportData.empSheets().size(); index++) {
                EmpMonthSheet empSheet = exportData.empSheets().get(index);
                if (index > 0) {
                    document.newPage();
                }

                Paragraph title = new Paragraph("Arbeitszeit - Stundenaufstellung für Arbeitnehmer/in", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(6f);
                document.add(title);

                Paragraph subtitle = new Paragraph("(Emp Time Sheet)", subtitleFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(14f);
                document.add(subtitle);

                PdfPTable infoLayout = new PdfPTable(new float[]{2.1f, 1.2f});
                infoLayout.setWidthPercentage(100f);
                infoLayout.setSpacingAfter(12f);

                PdfPCell leftInfo = new PdfPCell();
                leftInfo.setBorder(Rectangle.NO_BORDER);
                leftInfo.setPadding(0f);
                leftInfo.addElement(new Paragraph(
                        "Arbeitnehmer/in(Applicant) : " + nullToBlank(empSheet.empNm()),
                        bodyFont
                ));
                leftInfo.addElement(new Paragraph(
                        "Monat/Jahr(Month/Year) : " + exportData.yearMonth().format(SAMPLE_PDF_MONTH_FORMAT),
                        bodyFont
                ));
                infoLayout.addCell(leftInfo);

                PdfPCell rightInfo = new PdfPCell(buildPdfSignatureTable(headerFont, bodyFont));
                rightInfo.setBorder(Rectangle.NO_BORDER);
                rightInfo.setPadding(0f);
                infoLayout.addCell(rightInfo);
                document.add(infoLayout);

                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100f);
                table.setWidths(new float[]{0.8f, 0.7f, 1.7f, 1.7f, 1.7f, 1.7f, 1.8f});
                table.setSpacingAfter(12f);

                String[] headers = {
                        "Datum\n(Date)",
                        "Tag\n(day)",
                        "Abwesenheit*\n(Leave)",
                        "Beginn Uhrzeit\n(Start at)",
                        "Ende Uhrzeit\n(End at)",
                        "Pause\n(Hours-break)",
                        "Arbeitsstunden\n(Hours-work)"
                };
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(6f);
                    table.addCell(cell);
                }

                for (EmpDayCell dayCell : empSheet.dayCells()) {
                    java.awt.Color background = dayCell.sunday()
                            ? new java.awt.Color(250, 236, 231)
                            : dayCell.saturday()
                                    ? new java.awt.Color(232, 238, 250)
                                    : null;
                    table.addCell(buildPdfBodyCell(String.valueOf(dayCell.date().getDayOfMonth()), bodyFont, Element.ALIGN_CENTER, background));
                    table.addCell(buildPdfBodyCell(dayCell.dayLabel(), bodyFont, Element.ALIGN_CENTER, background));
                    table.addCell(buildPdfBodyCell(dayCell.absenceCd(), bodyFont, Element.ALIGN_CENTER, background));
                    table.addCell(buildPdfBodyCell(dayCell.startAt(), bodyFont, Element.ALIGN_RIGHT, background));
                    table.addCell(buildPdfBodyCell(dayCell.endAt(), bodyFont, Element.ALIGN_RIGHT, background));
                    table.addCell(buildPdfBodyCell(dayCell.breakDisplay(), bodyFont, Element.ALIGN_RIGHT, background));
                    table.addCell(buildPdfBodyCell(dayCell.workDisplay(), bodyFont, Element.ALIGN_RIGHT, background));
                }

                PdfPCell totalLabelCell = new PdfPCell(new Phrase("Summe(Total)", headerFont));
                totalLabelCell.setColspan(6);
                totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalLabelCell.setPadding(6f);
                table.addCell(totalLabelCell);

                PdfPCell totalValueCell = new PdfPCell(new Phrase(formatWorkTime(empSheet.totalWorkMinutes()), headerFont));
                totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalValueCell.setPadding(6f);
                table.addCell(totalValueCell);
                document.add(table);

                Paragraph footnoteTitle = new Paragraph("* Abwesenheit(Leave)", footnoteFont);
                footnoteTitle.setSpacingAfter(2f);
                document.add(footnoteTitle);
                document.add(new Paragraph("  U = Urlaub(Annual)", footnoteFont));
                document.add(new Paragraph("  B = Business Trip", footnoteFont));
                document.add(new Paragraph("  F = Feiertag(Holiday)", footnoteFont));
            }

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export work time history to PDF", exception);
        }
    }

    private PdfPTable buildPdfSignatureTable(Font headerFont, Font bodyFont) {
        PdfPTable table = new PdfPTable(new float[]{1.1f, 1.2f});
        table.setWidthPercentage(100f);

        PdfPCell titleCell = new PdfPCell(new Phrase("Datum/Unterschrift(Date/Signature)", headerFont));
        titleCell.setColspan(2);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPadding(7f);
        table.addCell(titleCell);

        String[] labels = {"Arbeitnehmer/in", "Manager/in", "HR Manager/in", "Präsident/in"};
        for (String label : labels) {
            PdfPCell labelCell = new PdfPCell(new Phrase(label, bodyFont));
            labelCell.setPadding(8f);
            table.addCell(labelCell);

            PdfPCell signCell = new PdfPCell(new Phrase("", bodyFont));
            signCell.setPadding(8f);
            table.addCell(signCell);
        }

        return table;
    }

    private PdfPCell buildPdfBodyCell(String value, Font font, int alignment, java.awt.Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(nullToBlank(value), font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        if (background != null) {
            cell.setBackgroundColor(background);
        }
        return cell;
    }

    private void applyExcelBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private void writeExcelCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value == null ? "" : value.toString());
        }
        cell.setCellStyle(style);
    }

    private List<CalDayMeta> buildCalDays(YearMonth targetMonth, Map<LocalDate, HoliCache> holidayByDate) {
        List<CalDayMeta> days = new ArrayList<>();
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            days.add(new CalDayMeta(
                    date,
                    String.format(Locale.ROOT, "%02d", day),
                    date.getDayOfWeek() == DayOfWeek.SATURDAY,
                    date.getDayOfWeek() == DayOfWeek.SUNDAY,
                    holidayByDate.containsKey(date)
            ));
        }
        return days;
    }

    private List<EmpScopeRow> resolveEmpScopeRows(
            RptCtx rptCtx,
            List<AttndHistRow> filteredRows,
            AttndWorkTimeHistQuery query
    ) {
        String normalizedEmp = normalizeText(query.empNm());
        String normalizedDept = normalizeText(query.deptNm());
        String normalizedKeyword = normalizeText(query.keyword());
        LinkedHashMap<String, EmpScopeRow> orderedRows = new LinkedHashMap<>();

        if (normalizedKeyword == null) {
            for (Emp emp : rptCtx.empList()) {
                Dept dept = emp.getDeptId() == null ? null : rptCtx.deptById().get(emp.getDeptId());
                EmpScopeRow row = new EmpScopeRow(
                        resolveEmpKey(emp),
                        emp.getId(),
                        emp.getUsrId(),
                        emp.getEmpNo(),
                        emp.getEmpNm(),
                        dept == null ? null : dept.getDeptCd(),
                        dept == null ? null : dept.getDeptNm()
                );
                if (matchesEmpFilts(row, normalizedEmp, normalizedDept)) {
                    orderedRows.put(row.empKey(), row);
                }
            }
        }

        for (AttndHistRow historyRow : filteredRows) {
            EmpScopeRow row = new EmpScopeRow(
                    resolveEmpKey(historyRow),
                    historyRow.empId(),
                    historyRow.usrId(),
                    historyRow.empNo(),
                    historyRow.empNm(),
                    historyRow.deptCd(),
                    historyRow.deptNm()
            );
            orderedRows.putIfAbsent(row.empKey(), row);
        }

        if (orderedRows.isEmpty()) {
            EmpScopeRow currentUsrRow = buildCurrentUsrScopeRow(rptCtx);
            if (currentUsrRow != null && matchesEmpFilts(currentUsrRow, normalizedEmp, normalizedDept)) {
                orderedRows.put(currentUsrRow.empKey(), currentUsrRow);
            }
        }

        return orderedRows.values().stream()
                .sorted(Comparator.comparing(EmpScopeRow::empNm, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private boolean matchesEmpFilts(EmpScopeRow row, String normalizedEmp, String normalizedDept) {
        return (normalizedEmp == null || containsIgnoreCase(row.empNm(), normalizedEmp))
                && (normalizedDept == null || containsIgnoreCase(row.deptNm(), normalizedDept));
    }

    private EmpScopeRow buildCurrentUsrScopeRow(RptCtx rptCtx) {
        if (!rptCtx.ownOnlyYn()) {
            return null;
        }

        Dept dept = rptCtx.emp() == null || rptCtx.emp().getDeptId() == null
                ? null
                : rptCtx.deptById().get(rptCtx.emp().getDeptId());
        return new EmpScopeRow(
                resolveEmpKey(rptCtx.user()),
                rptCtx.emp() == null ? null : rptCtx.emp().getId(),
                rptCtx.user().usrId(),
                rptCtx.emp() == null ? null : rptCtx.emp().getEmpNo(),
                rptCtx.emp() == null ? fallbackCurrentUsrName(rptCtx.user()) : rptCtx.emp().getEmpNm(),
                dept == null ? null : dept.getDeptCd(),
                dept == null ? null : dept.getDeptNm()
        );
    }

    private EmpMonthSheet buildEmpMonthSheet(
            int ordinal,
            EmpScopeRow emp,
            List<CalDayMeta> calDays,
            Map<LocalDate, AttndHistRow> historyByDate
    ) {
        List<EmpDayCell> dayCells = new ArrayList<>();
        int totalMinutes = 0;

        for (CalDayMeta day : calDays) {
            AttndHistRow row = historyByDate.get(day.date());
            int breakMinutes = calculateBreakMinutes(row);
            int netMinutes = calculateNetWorkMinutes(row);
            totalMinutes += netMinutes;
            dayCells.add(new EmpDayCell(
                    day.date(),
                    resolveGermanDayLabel(day.date()),
                    resolveAbsenceCd(row, day.holiday()),
                    row == null ? null : row.chkinTm(),
                    row == null ? null : row.chkoutTm(),
                    breakMinutes == 0 ? "" : formatWorkTime(breakMinutes),
                    netMinutes == 0 ? "" : formatWorkTime(netMinutes),
                    day.saturday(),
                    day.sunday(),
                    day.holiday(),
                    resolveMatrixValue(row, day.holiday())
            ));
        }

        return new EmpMonthSheet(
                emp.empKey(),
                ordinal,
                emp.empNo(),
                emp.empNm(),
                emp.deptCd(),
                emp.deptNm(),
                dayCells,
                totalMinutes
        );
    }

    private String buildPdfExportFileName(MonthlyExportData exportData) {
        if (exportData.empSheets().size() == 1) {
            EmpMonthSheet sheet = exportData.empSheets().get(0);
            String empName = nullToBlank(sheet.empNm()).replaceAll("[\\\\/:*?\"<>|]+", " ").trim();
            return (empName.isBlank() ? "Emp" : empName) + "_"
                    + exportData.yearMonth().getMonthValue() + "_"
                    + exportData.yearMonth().getYear() + ".pdf";
        }
        return "Emp Time Sheet(" + exportData.yearMonth().format(MONTH_FORMAT) + ").pdf";
    }

    private Font resolvePdfFont(Locale locale, float size, int style) throws DocumentException, IOException {
        for (String path : WINDOWS_KOREAN_FONT_PATHS) {
            java.io.File fontFile = new java.io.File(path);
            if (fontFile.exists()) {
                BaseFont baseFont = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return new Font(baseFont, size, style);
            }
        }
        return FontFactory.getFont(FontFactory.HELVETICA, size, style);
    }

    private Map<LocalDate, HoliCache> loadHolidayByDate(AuthenticatedUsr authenticatedUsr, YearMonth targetMonth) {
        HolidayContext holidayContext = resolveHolidayContext(authenticatedUsr);
        List<HoliCache> holidays = holiCacheRepository.findAllByCtryCdAndRegionCdInAndHoliDtBetweenOrderByHoliDtAsc(
                holidayContext.ctryCd(),
                resolveRegionScope(holidayContext.regionCd()),
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth()
        );
        return buildHolidayMap(holidays);
    }

    private HolidayContext resolveHolidayContext(AuthenticatedUsr authenticatedUsr) {
        Optional<Emp> emp = empRepository.findByUsrId(authenticatedUsr.usrId());
        if (emp.isPresent() && emp.get().getWorkLocId() != null) {
            Optional<WorkLoc> workLoc = workLocRepository.findByIdAndTenantIdAndCoId(
                    emp.get().getWorkLocId(),
                    authenticatedUsr.tenantId(),
                    emp.get().getCoId()
            );
            if (workLoc.isPresent()) {
                return new HolidayContext(
                        normalizeCountryCd(workLoc.get().getCtryCd()),
                        normalizeRegionCd(workLoc.get().getRegionCd())
                );
            }
        }

        Long companyId = authenticatedUsr.coId();
        if (companyId == null && emp.isPresent()) {
            companyId = emp.get().getCoId();
        }
        if (companyId != null) {
            return coRepository.findByIdAndTenantId(companyId, authenticatedUsr.tenantId())
                    .map(company -> new HolidayContext(
                            normalizeCountryCd(company.getHqCtryCd()),
                            normalizeRegionCd(company.getHqRegionCd())
                    ))
                    .orElse(new HolidayContext(attndProperties.dfltCtryCd(), attndProperties.dfltRegionCd()));
        }

        return new HolidayContext(attndProperties.dfltCtryCd(), attndProperties.dfltRegionCd());
    }

    private List<String> resolveRegionScope(String regionCd) {
        if (regionCd == null || regionCd.isBlank() || attndProperties.dfltRegionCd().equalsIgnoreCase(regionCd)) {
            return List.of(attndProperties.dfltRegionCd());
        }
        return List.of(attndProperties.dfltRegionCd(), regionCd.toUpperCase(Locale.ROOT));
    }

    private Map<LocalDate, HoliCache> buildHolidayMap(List<HoliCache> holidays) {
        Map<LocalDate, HoliCache> holidayByDate = new LinkedHashMap<>();
        for (HoliCache holiday : holidays) {
            HoliCache current = holidayByDate.get(holiday.getHoliDt());
            if (current == null || (attndProperties.dfltRegionCd().equals(current.getRegionCd())
                    && !attndProperties.dfltRegionCd().equals(holiday.getRegionCd()))) {
                holidayByDate.put(holiday.getHoliDt(), holiday);
            }
        }
        return holidayByDate;
    }

    private String resolveEmpKey(AttndHistRow row) {
        if (row.empId() != null) {
            return "emp:" + row.empId();
        }
        if (row.usrId() != null) {
            return "usr:" + row.usrId();
        }
        return "name:" + nullToBlank(row.empNm());
    }

    private String resolveEmpKey(Emp emp) {
        if (emp.getId() != null) {
            return "emp:" + emp.getId();
        }
        if (emp.getUsrId() != null) {
            return "usr:" + emp.getUsrId();
        }
        return "empno:" + nullToBlank(emp.getEmpNo());
    }

    private String resolveEmpKey(AuthenticatedUsr authenticatedUsr) {
        return authenticatedUsr.usrId() == null ? "user:session" : "usr:" + authenticatedUsr.usrId();
    }

    private String resolveGermanDayLabel(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "Mo.";
            case TUESDAY -> "Di.";
            case WEDNESDAY -> "Mi.";
            case THURSDAY -> "Do.";
            case FRIDAY -> "Fr.";
            case SATURDAY -> "Sa.";
            case SUNDAY -> "So.";
        };
    }

    private String resolveAbsenceCd(AttndHistRow row, boolean holiday) {
        if (holiday) {
            return "F";
        }
        if (row == null || row.attndStsCd() == null) {
            return "";
        }
        if (AttndStatusCd.LEAVE.equalsIgnoreCase(row.attndStsCd())) {
            return "U";
        }
        if (AttndStatusCd.BUSINESS_TRIP.equalsIgnoreCase(row.attndStsCd())) {
            return "B";
        }
        return "";
    }

    private String resolveMatrixValue(AttndHistRow row, boolean holiday) {
        if (holiday) {
            return "";
        }
        return resolveAbsenceCd(row, false);
    }

    private int calculateBreakMinutes(AttndHistRow row) {
        if (row == null || row.chkinTm() == null || row.chkoutTm() == null) {
            return 0;
        }
        if (AttndStatusCd.isNoTimeStatus(row.attndStsCd())) {
            return 0;
        }
        int grossMinutes = calculateGrossWorkMinutes(row);
        return grossMinutes <= 0 ? 0 : Math.min(DEFAULT_BREAK_MINUTES, grossMinutes);
    }

    private int calculateNetWorkMinutes(AttndHistRow row) {
        int grossMinutes = calculateGrossWorkMinutes(row);
        int breakMinutes = calculateBreakMinutes(row);
        return Math.max(grossMinutes - breakMinutes, 0);
    }

    private int calculateGrossWorkMinutes(AttndHistRow row) {
        if (row == null || row.chkinTm() == null || row.chkoutTm() == null) {
            return 0;
        }
        try {
            LocalTime checkIn = LocalTime.parse(row.chkinTm(), TIME_FORMAT);
            LocalTime checkOut = LocalTime.parse(row.chkoutTm(), TIME_FORMAT);
            long minutes = Duration.between(checkIn, checkOut).toMinutes();
            return minutes < 0 ? 0 : Math.toIntExact(minutes);
        } catch (DateTimeParseException exception) {
            return 0;
        }
    }

    private AuthenticatedUsr requireUsr(AuthenticatedUsr authenticatedUsr) {
        if (authenticatedUsr == null || authenticatedUsr.usrId() == null || authenticatedUsr.tenantId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return authenticatedUsr;
    }

    private boolean hasElevatedRptAccess(AuthenticatedUsr user) {
        if (hasPlatformRole(user)) {
            return true;
        }
        return user.roleCds() != null && user.roleCds().stream()
                .filter(Objects::nonNull)
                .anyMatch(CUSTOMER_ADMIN_ROLE_CDS::contains);
    }

    private boolean hasPlatformRole(AuthenticatedUsr user) {
        return user.roleCds() != null && user.roleCds().stream().anyMatch(role -> role != null && role.startsWith("ROLE_PLATFORM_"));
    }

    private YearMonth resolveTargetMonth(String value) {
        if (value == null || value.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(value, MONTH_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year month must follow yyyy-MM");
        }
    }

    private String resolveStatusName(String statusCd, Locale locale) {
        return commonCdService.resolveDspNm(
                CommonCdGroupCd.ATTND_FLAG,
                statusCd,
                AttndStatusCd.fallbackDisplayValue(statusCd)
        );
    }

    private Integer calculateWorkMinuteCnt(AttndRec record) {
        if (record.getChkinDtm() == null || record.getChkoutDtm() == null) {
            return 0;
        }
        long minutes = Duration.between(record.getChkinDtm(), record.getChkoutDtm()).toMinutes();
        return minutes < 0 ? 0 : Math.toIntExact(minutes);
    }

    private String formatTime(OffsetDateTime value) {
        return value == null
                ? null
                : value.atZoneSameInstant(attndProperties.bizZoneId()).toLocalTime().format(TIME_FORMAT);
    }

    private String buildKeywordText(
            AttndRec record,
            Emp emp,
            Dept dept,
            String statusNm,
            String checkInTm,
            String checkOutTm
    ) {
        return String.join(" ",
                nullToBlank(record.getAttndDt() == null ? null : record.getAttndDt().format(DATE_FORMAT)),
                nullToBlank(emp == null ? null : emp.getEmpNo()),
                nullToBlank(emp == null ? null : emp.getEmpNm()),
                nullToBlank(dept == null ? null : dept.getDeptNm()),
                nullToBlank(statusNm),
                nullToBlank(checkInTm),
                nullToBlank(checkOutTm)
        );
    }

    private String fallbackEmpName(AuthenticatedUsr authenticatedUsr, Emp currentEmp, AttndRec record) {
        if (currentEmp != null && Objects.equals(record.getUsrId(), authenticatedUsr.usrId()) && currentEmp.getEmpNm() != null) {
            return currentEmp.getEmpNm();
        }
        if (Objects.equals(record.getUsrId(), authenticatedUsr.usrId()) && authenticatedUsr.dspNm() != null && !authenticatedUsr.dspNm().isBlank()) {
            return authenticatedUsr.dspNm();
        }
        return record.getUsrId() == null ? "-" : "USER-" + record.getUsrId();
    }

    private String fallbackCurrentUsrName(AuthenticatedUsr authenticatedUsr) {
        if (authenticatedUsr.dspNm() != null && !authenticatedUsr.dspNm().isBlank()) {
            return authenticatedUsr.dspNm();
        }
        if (authenticatedUsr.lgnId() != null && !authenticatedUsr.lgnId().isBlank()) {
            return authenticatedUsr.lgnId();
        }
        return "Current Usr";
    }

    private String formatWorkTime(Integer workMinuteCnt) {
        int safeMinutes = workMinuteCnt == null ? 0 : workMinuteCnt;
        int hours = safeMinutes / 60;
        int minutes = safeMinutes % 60;
        return String.format(Locale.ROOT, "%02d:%02d", hours, minutes);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(String source, String normalizedToken) {
        if (source == null || normalizedToken == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(normalizedToken);
    }

    private String normalizeCountryCd(String value) {
        if (value == null || value.isBlank()) {
            return attndProperties.dfltCtryCd();
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRegionCd(String value) {
        if (value == null || value.isBlank()) {
            return attndProperties.dfltRegionCd();
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private record Scope(Set<Long> coIdSet, boolean tenantWideYn) {
    }

    private record RptCtx(
            AuthenticatedUsr user,
            boolean ownOnlyYn,
            Scope scope,
            Emp emp,
            List<Emp> empList,
            List<Dept> deptList,
            Map<Long, Emp> empById,
            Map<Long, Dept> deptById
    ) {
    }

    private record AttndHistRow(
            Long attndId,
            LocalDate attndDt,
            Long usrId,
            Long empId,
            String empNo,
            String empNm,
            Long deptId,
            String deptCd,
            String deptNm,
            String attndStsCd,
            String attndStsNm,
            String chkinTm,
            String chkoutTm,
            Integer workMinuteCnt,
            String keywordText
    ) {
    }

    private record HolidayContext(String ctryCd, String regionCd) {
    }

    private record CalDayMeta(
            LocalDate date,
            String dayLabel,
            boolean saturday,
            boolean sunday,
            boolean holiday
    ) {
    }

    private record EmpScopeRow(
            String empKey,
            Long empId,
            Long usrId,
            String empNo,
            String empNm,
            String deptCd,
            String deptNm
    ) {
    }

    private record EmpDayCell(
            LocalDate date,
            String dayLabel,
            String absenceCd,
            String startAt,
            String endAt,
            String breakDisplay,
            String workDisplay,
            boolean saturday,
            boolean sunday,
            boolean holiday,
            String matrixValue
    ) {
    }

    private record EmpMonthSheet(
            String empKey,
            int no,
            String empNo,
            String empNm,
            String deptCd,
            String deptNm,
            List<EmpDayCell> dayCells,
            int totalWorkMinutes
    ) {
    }

    private record MonthlyExportData(
            YearMonth yearMonth,
            boolean ownOnlyYn,
            List<CalDayMeta> calDays,
            List<EmpMonthSheet> empSheets
    ) {
    }
}
