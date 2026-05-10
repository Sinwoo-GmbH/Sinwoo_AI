package com.sinwoo.attendance.service;

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
import com.sinwoo.attendance.domain.AttendanceRecord;
import com.sinwoo.attendance.domain.HolidayCache;
import com.sinwoo.attendance.dto.AttendanceReportExportFile;
import com.sinwoo.attendance.dto.AttendanceWorkTimeFilterOptionResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeFilterOptionsResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryListResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryQuery;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryRowResponse;
import com.sinwoo.attendance.repository.AttendanceRecordRepository;
import com.sinwoo.attendance.repository.HolidayCacheRepository;
import com.sinwoo.attendance.support.AttendanceProperties;
import com.sinwoo.attendance.support.AttendanceStatusCd;
import com.sinwoo.code.service.CommonCodeService;
import com.sinwoo.code.support.CommonCodeGroupCd;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.company.domain.Company;
import com.sinwoo.company.repository.CompanyRepository;
import com.sinwoo.department.domain.Department;
import com.sinwoo.department.repository.DepartmentRepository;
import com.sinwoo.employee.domain.Employee;
import com.sinwoo.employee.repository.EmployeeRepository;
import com.sinwoo.worklocation.domain.WorkLocation;
import com.sinwoo.worklocation.repository.WorkLocationRepository;
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
public class AttendanceReportServiceImpl implements AttendanceReportService {

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

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final HolidayCacheRepository holidayCacheRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final WorkLocationRepository workLocationRepository;
    private final CommonCodeService commonCodeService;
    private final AttendanceProperties attendanceProperties;

    @Override
    public AttendanceWorkTimeHistoryListResponse getWorkTimeHistory(
            AuthenticatedUser authenticatedUser,
            AttendanceWorkTimeHistoryQuery query,
            Locale locale
    ) {
        ReportContext reportContext = buildReportContext(authenticatedUser);
        YearMonth targetMonth = resolveTargetMonth(query.yearMonth());
        List<AttendanceHistoryRow> rows = findHistoryRows(reportContext, targetMonth, locale);
        List<AttendanceHistoryRow> filteredRows = filterRows(rows, query);

        return new AttendanceWorkTimeHistoryListResponse(
                targetMonth.format(MONTH_FORMAT),
                reportContext.ownOnlyYn(),
                filteredRows.size(),
                filteredRows.stream()
                        .map(this::toHistoryRowResponse)
                        .toList()
        );
    }

    @Override
    public AttendanceWorkTimeFilterOptionsResponse getWorkTimeFilterOptions(AuthenticatedUser authenticatedUser) {
        ReportContext reportContext = buildReportContext(authenticatedUser);
        List<AttendanceWorkTimeFilterOptionResponse> employeeOptions = reportContext.employeeList().stream()
                .map(employee -> {
                    Department department = employee.getDeptId() == null ? null : reportContext.departmentById().get(employee.getDeptId());
                    return new AttendanceWorkTimeFilterOptionResponse(
                            employee.getId(),
                            employee.getEmpNo(),
                            employee.getEmpNm(),
                            department == null ? null : department.getDeptNm()
                    );
                })
                .toList();
        List<AttendanceWorkTimeFilterOptionResponse> departmentOptions = reportContext.departmentList().stream()
                .map(department -> new AttendanceWorkTimeFilterOptionResponse(
                        department.getId(),
                        department.getDeptCd(),
                        department.getDeptNm(),
                        null
                ))
                .toList();

        return new AttendanceWorkTimeFilterOptionsResponse(
                reportContext.ownOnlyYn(),
                employeeOptions.size(),
                employeeOptions,
                departmentOptions.size(),
                departmentOptions
        );
    }

    @Override
    public AttendanceReportExportFile exportWorkTimeHistoryExcel(
            AuthenticatedUser authenticatedUser,
            AttendanceWorkTimeHistoryQuery query,
            Locale locale
    ) {
        MonthlyExportData exportData = buildMonthlyExportData(authenticatedUser, query, locale);
        byte[] content = writeExcel(exportData);
        return new AttendanceReportExportFile(
                "Work Time List(" + exportData.yearMonth().format(MONTH_FORMAT) + ").xlsx",
                EXCEL_CONTENT_TYPE,
                content
        );
    }

    @Override
    public AttendanceReportExportFile exportWorkTimeHistoryPdf(
            AuthenticatedUser authenticatedUser,
            AttendanceWorkTimeHistoryQuery query,
            Locale locale
    ) {
        MonthlyExportData exportData = buildMonthlyExportData(authenticatedUser, query, locale);
        byte[] content = writePdf(exportData, locale);
        return new AttendanceReportExportFile(
                buildPdfExportFileName(exportData),
                "application/pdf",
                content
        );
    }

    private MonthlyExportData buildMonthlyExportData(
            AuthenticatedUser authenticatedUser,
            AttendanceWorkTimeHistoryQuery query,
            Locale locale
    ) {
        ReportContext reportContext = buildReportContext(authenticatedUser);
        YearMonth targetMonth = resolveTargetMonth(query.yearMonth());
        List<AttendanceHistoryRow> rows = findHistoryRows(reportContext, targetMonth, locale);
        List<AttendanceHistoryRow> filteredRows = filterRows(rows, query);
        Map<LocalDate, HolidayCache> holidayByDate = loadHolidayByDate(reportContext.user(), targetMonth);
        List<CalendarDayMeta> calendarDays = buildCalendarDays(targetMonth, holidayByDate);
        List<EmployeeScopeRow> employeeRows = resolveEmployeeScopeRows(reportContext, filteredRows, query);

        Map<String, Map<LocalDate, AttendanceHistoryRow>> historyByEmployee = new LinkedHashMap<>();
        for (AttendanceHistoryRow row : filteredRows) {
            historyByEmployee.computeIfAbsent(resolveEmployeeKey(row), ignored -> new LinkedHashMap<>())
                    .put(row.attndDt(), row);
        }

        List<EmployeeMonthSheet> employeeSheets = new ArrayList<>();
        int ordinal = 1;
        for (EmployeeScopeRow employeeRow : employeeRows) {
            employeeSheets.add(buildEmployeeMonthSheet(
                    ordinal++,
                    employeeRow,
                    calendarDays,
                    historyByEmployee.getOrDefault(employeeRow.employeeKey(), Map.of())
            ));
        }

        return new MonthlyExportData(targetMonth, reportContext.ownOnlyYn(), calendarDays, employeeSheets);
    }

    private ReportContext buildReportContext(AuthenticatedUser authenticatedUser) {
        AuthenticatedUser user = requireUser(authenticatedUser);
        boolean elevated = hasElevatedReportAccess(user);
        Optional<Employee> employee = employeeRepository.findByUsrId(user.usrId());
        boolean ownOnlyYn = !elevated;
        Scope scope = resolveScope(user, employee, elevated);

        List<Employee> employeeList = loadEmployees(user.tenantId(), scope);
        List<Department> departmentList = loadDepartments(user.tenantId(), scope);
        Map<Long, Employee> employeeById = employeeList.stream()
                .collect(Collectors.toMap(Employee::getId, value -> value, (left, right) -> left, LinkedHashMap::new));
        Map<Long, Department> departmentById = departmentList.stream()
                .collect(Collectors.toMap(Department::getId, value -> value, (left, right) -> left, LinkedHashMap::new));

        return new ReportContext(user, ownOnlyYn, scope, employee.orElse(null), employeeList, departmentList, employeeById, departmentById);
    }

    private Scope resolveScope(AuthenticatedUser user, Optional<Employee> employee, boolean elevated) {
        if (!elevated) {
            Long companyId = employee.map(Employee::getCoId).orElse(user.coId());
            return new Scope(companyId == null ? Set.of() : Set.of(companyId), false);
        }

        if (hasPlatformRole(user)) {
            return new Scope(Set.of(), true);
        }

        Long companyId = user.coId();
        if (companyId == null && employee.isPresent()) {
            companyId = employee.get().getCoId();
        }
        if (companyId == null) {
            Set<Long> tenantCompanyIds = companyRepository.findAllByTenantIdOrderByCreatedAtDescIdDesc(user.tenantId()).stream()
                    .map(Company::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return new Scope(tenantCompanyIds, tenantCompanyIds.isEmpty());
        }
        return new Scope(Set.of(companyId), false);
    }

    private List<Employee> loadEmployees(Long tenantId, Scope scope) {
        if (scope.tenantWideYn()) {
            return employeeRepository.findAllByTenantIdOrderByEmpNmAscIdAsc(tenantId);
        }
        if (scope.coIdSet().isEmpty()) {
            return List.of();
        }
        return employeeRepository.findAllByTenantIdAndCoIdInOrderByEmpNmAscIdAsc(tenantId, scope.coIdSet());
    }

    private List<Department> loadDepartments(Long tenantId, Scope scope) {
        if (scope.tenantWideYn()) {
            return departmentRepository.findAllByTenantIdOrderByDeptLvlNoAscDeptNmAscIdAsc(tenantId);
        }
        if (scope.coIdSet().isEmpty()) {
            return List.of();
        }
        return departmentRepository.findAllByTenantIdAndCoIdInOrderByDeptLvlNoAscDeptNmAscIdAsc(tenantId, scope.coIdSet());
    }

    private List<AttendanceHistoryRow> findHistoryRows(ReportContext reportContext, YearMonth targetMonth, Locale locale) {
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();
        List<AttendanceRecord> records = loadAttendanceRecords(reportContext, startDate, endDate);

        return records.stream()
                .map(record -> toHistoryRow(reportContext, record, locale))
                .sorted(Comparator
                        .comparing(AttendanceHistoryRow::attndDt).reversed()
                        .thenComparing(AttendanceHistoryRow::empNm, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(AttendanceHistoryRow::attndId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private List<AttendanceRecord> loadAttendanceRecords(ReportContext reportContext, LocalDate startDate, LocalDate endDate) {
        if (reportContext.ownOnlyYn()) {
            return attendanceRecordRepository.findAllByTenantIdAndUsrIdAndAttndDtBetweenOrderByAttndDtDescIdDesc(
                    reportContext.user().tenantId(),
                    reportContext.user().usrId(),
                    startDate,
                    endDate
            );
        }

        if (reportContext.scope().tenantWideYn() || reportContext.scope().coIdSet().isEmpty()) {
            return attendanceRecordRepository.findAllByTenantIdAndAttndDtBetweenOrderByAttndDtDescUsrIdAscIdDesc(
                    reportContext.user().tenantId(),
                    startDate,
                    endDate
            );
        }

        return attendanceRecordRepository.findAllByTenantIdAndCoIdInAndAttndDtBetweenOrderByAttndDtDescUsrIdAscIdDesc(
                reportContext.user().tenantId(),
                reportContext.scope().coIdSet(),
                startDate,
                endDate
        );
    }

    private AttendanceHistoryRow toHistoryRow(ReportContext reportContext, AttendanceRecord record, Locale locale) {
        Employee employee = record.getEmpId() == null ? null : reportContext.employeeById().get(record.getEmpId());
        Department department = employee == null || employee.getDeptId() == null ? null : reportContext.departmentById().get(employee.getDeptId());
        String empNm = employee == null
                ? fallbackEmployeeName(reportContext.user(), reportContext.employee(), record)
                : employee.getEmpNm();
        String deptNm = department == null ? null : department.getDeptNm();
        String deptCd = department == null ? null : department.getDeptCd();
        String statusNm = resolveStatusName(record.getAttndStsCd(), locale);
        String checkInTm = formatTime(record.getChkinDtm());
        String checkOutTm = formatTime(record.getChkoutDtm());

        return new AttendanceHistoryRow(
                record.getId(),
                record.getAttndDt(),
                record.getUsrId(),
                employee == null ? record.getEmpId() : employee.getId(),
                employee == null ? null : employee.getEmpNo(),
                empNm,
                department == null ? null : department.getId(),
                deptCd,
                deptNm,
                record.getAttndStsCd(),
                statusNm,
                checkInTm,
                checkOutTm,
                calculateWorkMinuteCnt(record),
                buildKeywordText(record, employee, department, statusNm, checkInTm, checkOutTm)
        );
    }

    private List<AttendanceHistoryRow> filterRows(List<AttendanceHistoryRow> rows, AttendanceWorkTimeHistoryQuery query) {
        String empNm = normalizeText(query.empNm());
        String deptNm = normalizeText(query.deptNm());
        String keyword = normalizeText(query.keyword());

        return rows.stream()
                .filter(row -> empNm == null || containsIgnoreCase(row.empNm(), empNm))
                .filter(row -> deptNm == null || containsIgnoreCase(row.deptNm(), deptNm))
                .filter(row -> keyword == null || containsIgnoreCase(row.keywordText(), keyword))
                .toList();
    }

    private AttendanceWorkTimeHistoryRowResponse toHistoryRowResponse(AttendanceHistoryRow row) {
        return new AttendanceWorkTimeHistoryRowResponse(
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
            int lastColumnIndex = 4 + exportData.calendarDays().size();

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

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastColumnIndex));
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
            for (int index = 0; index < exportData.calendarDays().size(); index++) {
                Cell cell = headerRow.createCell(index + 5);
                cell.setCellValue(exportData.calendarDays().get(index).dayLabel());
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 2;
            for (EmployeeMonthSheet rowData : exportData.employeeSheets()) {
                Row row = sheet.createRow(rowIndex++);
                writeExcelCell(row, 0, rowData.no(), centeredBodyStyle);
                writeExcelCell(row, 1, rowData.empNo(), bodyStyle);
                writeExcelCell(row, 2, rowData.empNm(), bodyStyle);
                writeExcelCell(row, 3, rowData.deptCd(), bodyStyle);
                writeExcelCell(row, 4, rowData.deptNm(), bodyStyle);

                for (int index = 0; index < rowData.dayCells().size(); index++) {
                    EmployeeDayCell dayCell = rowData.dayCells().get(index);
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
            for (int index = 5; index <= lastColumnIndex; index++) {
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

            for (int index = 0; index < exportData.employeeSheets().size(); index++) {
                EmployeeMonthSheet employeeSheet = exportData.employeeSheets().get(index);
                if (index > 0) {
                    document.newPage();
                }

                Paragraph title = new Paragraph("Arbeitszeit - Stundenaufstellung für Arbeitnehmer/in", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(6f);
                document.add(title);

                Paragraph subtitle = new Paragraph("(Employee Time Sheet)", subtitleFont);
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
                        "Arbeitnehmer/in(Applicant) : " + nullToBlank(employeeSheet.empNm()),
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

                for (EmployeeDayCell dayCell : employeeSheet.dayCells()) {
                    java.awt.Color background = dayCell.sunday()
                            ? new java.awt.Color(250, 236, 231)
                            : dayCell.saturday()
                                    ? new java.awt.Color(232, 238, 250)
                                    : null;
                    table.addCell(buildPdfBodyCell(String.valueOf(dayCell.date().getDayOfMonth()), bodyFont, Element.ALIGN_CENTER, background));
                    table.addCell(buildPdfBodyCell(dayCell.dayLabel(), bodyFont, Element.ALIGN_CENTER, background));
                    table.addCell(buildPdfBodyCell(dayCell.absenceCode(), bodyFont, Element.ALIGN_CENTER, background));
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

                PdfPCell totalValueCell = new PdfPCell(new Phrase(formatWorkTime(employeeSheet.totalWorkMinutes()), headerFont));
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

    private List<CalendarDayMeta> buildCalendarDays(YearMonth targetMonth, Map<LocalDate, HolidayCache> holidayByDate) {
        List<CalendarDayMeta> days = new ArrayList<>();
        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            days.add(new CalendarDayMeta(
                    date,
                    String.format(Locale.ROOT, "%02d", day),
                    date.getDayOfWeek() == DayOfWeek.SATURDAY,
                    date.getDayOfWeek() == DayOfWeek.SUNDAY,
                    holidayByDate.containsKey(date)
            ));
        }
        return days;
    }

    private List<EmployeeScopeRow> resolveEmployeeScopeRows(
            ReportContext reportContext,
            List<AttendanceHistoryRow> filteredRows,
            AttendanceWorkTimeHistoryQuery query
    ) {
        String normalizedEmp = normalizeText(query.empNm());
        String normalizedDept = normalizeText(query.deptNm());
        String normalizedKeyword = normalizeText(query.keyword());
        LinkedHashMap<String, EmployeeScopeRow> orderedRows = new LinkedHashMap<>();

        if (normalizedKeyword == null) {
            for (Employee employee : reportContext.employeeList()) {
                Department department = employee.getDeptId() == null ? null : reportContext.departmentById().get(employee.getDeptId());
                EmployeeScopeRow row = new EmployeeScopeRow(
                        resolveEmployeeKey(employee),
                        employee.getId(),
                        employee.getUsrId(),
                        employee.getEmpNo(),
                        employee.getEmpNm(),
                        department == null ? null : department.getDeptCd(),
                        department == null ? null : department.getDeptNm()
                );
                if (matchesEmployeeFilters(row, normalizedEmp, normalizedDept)) {
                    orderedRows.put(row.employeeKey(), row);
                }
            }
        }

        for (AttendanceHistoryRow historyRow : filteredRows) {
            EmployeeScopeRow row = new EmployeeScopeRow(
                    resolveEmployeeKey(historyRow),
                    historyRow.empId(),
                    historyRow.usrId(),
                    historyRow.empNo(),
                    historyRow.empNm(),
                    historyRow.deptCd(),
                    historyRow.deptNm()
            );
            orderedRows.putIfAbsent(row.employeeKey(), row);
        }

        if (orderedRows.isEmpty()) {
            EmployeeScopeRow currentUserRow = buildCurrentUserScopeRow(reportContext);
            if (currentUserRow != null && matchesEmployeeFilters(currentUserRow, normalizedEmp, normalizedDept)) {
                orderedRows.put(currentUserRow.employeeKey(), currentUserRow);
            }
        }

        return orderedRows.values().stream()
                .sorted(Comparator.comparing(EmployeeScopeRow::empNm, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    private boolean matchesEmployeeFilters(EmployeeScopeRow row, String normalizedEmp, String normalizedDept) {
        return (normalizedEmp == null || containsIgnoreCase(row.empNm(), normalizedEmp))
                && (normalizedDept == null || containsIgnoreCase(row.deptNm(), normalizedDept));
    }

    private EmployeeScopeRow buildCurrentUserScopeRow(ReportContext reportContext) {
        if (!reportContext.ownOnlyYn()) {
            return null;
        }

        Department department = reportContext.employee() == null || reportContext.employee().getDeptId() == null
                ? null
                : reportContext.departmentById().get(reportContext.employee().getDeptId());
        return new EmployeeScopeRow(
                resolveEmployeeKey(reportContext.user()),
                reportContext.employee() == null ? null : reportContext.employee().getId(),
                reportContext.user().usrId(),
                reportContext.employee() == null ? null : reportContext.employee().getEmpNo(),
                reportContext.employee() == null ? fallbackCurrentUserName(reportContext.user()) : reportContext.employee().getEmpNm(),
                department == null ? null : department.getDeptCd(),
                department == null ? null : department.getDeptNm()
        );
    }

    private EmployeeMonthSheet buildEmployeeMonthSheet(
            int ordinal,
            EmployeeScopeRow employee,
            List<CalendarDayMeta> calendarDays,
            Map<LocalDate, AttendanceHistoryRow> historyByDate
    ) {
        List<EmployeeDayCell> dayCells = new ArrayList<>();
        int totalMinutes = 0;

        for (CalendarDayMeta day : calendarDays) {
            AttendanceHistoryRow row = historyByDate.get(day.date());
            int breakMinutes = calculateBreakMinutes(row);
            int netMinutes = calculateNetWorkMinutes(row);
            totalMinutes += netMinutes;
            dayCells.add(new EmployeeDayCell(
                    day.date(),
                    resolveGermanDayLabel(day.date()),
                    resolveAbsenceCode(row, day.holiday()),
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

        return new EmployeeMonthSheet(
                employee.employeeKey(),
                ordinal,
                employee.empNo(),
                employee.empNm(),
                employee.deptCd(),
                employee.deptNm(),
                dayCells,
                totalMinutes
        );
    }

    private String buildPdfExportFileName(MonthlyExportData exportData) {
        if (exportData.employeeSheets().size() == 1) {
            EmployeeMonthSheet sheet = exportData.employeeSheets().get(0);
            String employeeName = nullToBlank(sheet.empNm()).replaceAll("[\\\\/:*?\"<>|]+", " ").trim();
            return (employeeName.isBlank() ? "Employee" : employeeName) + "_"
                    + exportData.yearMonth().getMonthValue() + "_"
                    + exportData.yearMonth().getYear() + ".pdf";
        }
        return "Employee Time Sheet(" + exportData.yearMonth().format(MONTH_FORMAT) + ").pdf";
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

    private Map<LocalDate, HolidayCache> loadHolidayByDate(AuthenticatedUser authenticatedUser, YearMonth targetMonth) {
        HolidayContext holidayContext = resolveHolidayContext(authenticatedUser);
        List<HolidayCache> holidays = holidayCacheRepository.findAllByCtryCdAndRegionCdInAndHoliDtBetweenOrderByHoliDtAsc(
                holidayContext.ctryCd(),
                resolveRegionScope(holidayContext.regionCd()),
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth()
        );
        return buildHolidayMap(holidays);
    }

    private HolidayContext resolveHolidayContext(AuthenticatedUser authenticatedUser) {
        Optional<Employee> employee = employeeRepository.findByUsrId(authenticatedUser.usrId());
        if (employee.isPresent() && employee.get().getWorkLocId() != null) {
            Optional<WorkLocation> workLocation = workLocationRepository.findByIdAndTenantIdAndCoId(
                    employee.get().getWorkLocId(),
                    authenticatedUser.tenantId(),
                    employee.get().getCoId()
            );
            if (workLocation.isPresent()) {
                return new HolidayContext(
                        normalizeCountryCode(workLocation.get().getCtryCd()),
                        normalizeRegionCode(workLocation.get().getRegionCd())
                );
            }
        }

        Long companyId = authenticatedUser.coId();
        if (companyId == null && employee.isPresent()) {
            companyId = employee.get().getCoId();
        }
        if (companyId != null) {
            return companyRepository.findByIdAndTenantId(companyId, authenticatedUser.tenantId())
                    .map(company -> new HolidayContext(
                            normalizeCountryCode(company.getHqCtryCd()),
                            normalizeRegionCode(company.getHqRegionCd())
                    ))
                    .orElse(new HolidayContext(attendanceProperties.dfltCtryCd(), attendanceProperties.dfltRegionCd()));
        }

        return new HolidayContext(attendanceProperties.dfltCtryCd(), attendanceProperties.dfltRegionCd());
    }

    private List<String> resolveRegionScope(String regionCd) {
        if (regionCd == null || regionCd.isBlank() || attendanceProperties.dfltRegionCd().equalsIgnoreCase(regionCd)) {
            return List.of(attendanceProperties.dfltRegionCd());
        }
        return List.of(attendanceProperties.dfltRegionCd(), regionCd.toUpperCase(Locale.ROOT));
    }

    private Map<LocalDate, HolidayCache> buildHolidayMap(List<HolidayCache> holidays) {
        Map<LocalDate, HolidayCache> holidayByDate = new LinkedHashMap<>();
        for (HolidayCache holiday : holidays) {
            HolidayCache current = holidayByDate.get(holiday.getHoliDt());
            if (current == null || (attendanceProperties.dfltRegionCd().equals(current.getRegionCd())
                    && !attendanceProperties.dfltRegionCd().equals(holiday.getRegionCd()))) {
                holidayByDate.put(holiday.getHoliDt(), holiday);
            }
        }
        return holidayByDate;
    }

    private String resolveEmployeeKey(AttendanceHistoryRow row) {
        if (row.empId() != null) {
            return "emp:" + row.empId();
        }
        if (row.usrId() != null) {
            return "usr:" + row.usrId();
        }
        return "name:" + nullToBlank(row.empNm());
    }

    private String resolveEmployeeKey(Employee employee) {
        if (employee.getId() != null) {
            return "emp:" + employee.getId();
        }
        if (employee.getUsrId() != null) {
            return "usr:" + employee.getUsrId();
        }
        return "empno:" + nullToBlank(employee.getEmpNo());
    }

    private String resolveEmployeeKey(AuthenticatedUser authenticatedUser) {
        return authenticatedUser.usrId() == null ? "user:session" : "usr:" + authenticatedUser.usrId();
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

    private String resolveAbsenceCode(AttendanceHistoryRow row, boolean holiday) {
        if (holiday) {
            return "F";
        }
        if (row == null || row.attndStsCd() == null) {
            return "";
        }
        if (AttendanceStatusCd.LEAVE.equalsIgnoreCase(row.attndStsCd())) {
            return "U";
        }
        if (AttendanceStatusCd.BUSINESS_TRIP.equalsIgnoreCase(row.attndStsCd())) {
            return "B";
        }
        return "";
    }

    private String resolveMatrixValue(AttendanceHistoryRow row, boolean holiday) {
        if (holiday) {
            return "";
        }
        return resolveAbsenceCode(row, false);
    }

    private int calculateBreakMinutes(AttendanceHistoryRow row) {
        if (row == null || row.chkinTm() == null || row.chkoutTm() == null) {
            return 0;
        }
        if (AttendanceStatusCd.isNoTimeStatus(row.attndStsCd())) {
            return 0;
        }
        int grossMinutes = calculateGrossWorkMinutes(row);
        return grossMinutes <= 0 ? 0 : Math.min(DEFAULT_BREAK_MINUTES, grossMinutes);
    }

    private int calculateNetWorkMinutes(AttendanceHistoryRow row) {
        int grossMinutes = calculateGrossWorkMinutes(row);
        int breakMinutes = calculateBreakMinutes(row);
        return Math.max(grossMinutes - breakMinutes, 0);
    }

    private int calculateGrossWorkMinutes(AttendanceHistoryRow row) {
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

    private AuthenticatedUser requireUser(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.usrId() == null || authenticatedUser.tenantId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return authenticatedUser;
    }

    private boolean hasElevatedReportAccess(AuthenticatedUser user) {
        if (hasPlatformRole(user)) {
            return true;
        }
        return user.roleCds() != null && user.roleCds().stream()
                .filter(Objects::nonNull)
                .anyMatch(CUSTOMER_ADMIN_ROLE_CDS::contains);
    }

    private boolean hasPlatformRole(AuthenticatedUser user) {
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
        return commonCodeService.resolveDisplayName(
                CommonCodeGroupCd.ATTND_FLAG,
                statusCd,
                AttendanceStatusCd.fallbackDisplayValue(statusCd)
        );
    }

    private Integer calculateWorkMinuteCnt(AttendanceRecord record) {
        if (record.getChkinDtm() == null || record.getChkoutDtm() == null) {
            return 0;
        }
        long minutes = Duration.between(record.getChkinDtm(), record.getChkoutDtm()).toMinutes();
        return minutes < 0 ? 0 : Math.toIntExact(minutes);
    }

    private String formatTime(OffsetDateTime value) {
        return value == null
                ? null
                : value.atZoneSameInstant(attendanceProperties.bizZoneId()).toLocalTime().format(TIME_FORMAT);
    }

    private String buildKeywordText(
            AttendanceRecord record,
            Employee employee,
            Department department,
            String statusNm,
            String checkInTm,
            String checkOutTm
    ) {
        return String.join(" ",
                nullToBlank(record.getAttndDt() == null ? null : record.getAttndDt().format(DATE_FORMAT)),
                nullToBlank(employee == null ? null : employee.getEmpNo()),
                nullToBlank(employee == null ? null : employee.getEmpNm()),
                nullToBlank(department == null ? null : department.getDeptNm()),
                nullToBlank(statusNm),
                nullToBlank(checkInTm),
                nullToBlank(checkOutTm)
        );
    }

    private String fallbackEmployeeName(AuthenticatedUser authenticatedUser, Employee currentEmployee, AttendanceRecord record) {
        if (currentEmployee != null && Objects.equals(record.getUsrId(), authenticatedUser.usrId()) && currentEmployee.getEmpNm() != null) {
            return currentEmployee.getEmpNm();
        }
        if (Objects.equals(record.getUsrId(), authenticatedUser.usrId()) && authenticatedUser.dspNm() != null && !authenticatedUser.dspNm().isBlank()) {
            return authenticatedUser.dspNm();
        }
        return record.getUsrId() == null ? "-" : "USER-" + record.getUsrId();
    }

    private String fallbackCurrentUserName(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser.dspNm() != null && !authenticatedUser.dspNm().isBlank()) {
            return authenticatedUser.dspNm();
        }
        if (authenticatedUser.lgnId() != null && !authenticatedUser.lgnId().isBlank()) {
            return authenticatedUser.lgnId();
        }
        return "Current User";
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

    private String normalizeCountryCode(String value) {
        if (value == null || value.isBlank()) {
            return attendanceProperties.dfltCtryCd();
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRegionCode(String value) {
        if (value == null || value.isBlank()) {
            return attendanceProperties.dfltRegionCd();
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private record Scope(Set<Long> coIdSet, boolean tenantWideYn) {
    }

    private record ReportContext(
            AuthenticatedUser user,
            boolean ownOnlyYn,
            Scope scope,
            Employee employee,
            List<Employee> employeeList,
            List<Department> departmentList,
            Map<Long, Employee> employeeById,
            Map<Long, Department> departmentById
    ) {
    }

    private record AttendanceHistoryRow(
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

    private record CalendarDayMeta(
            LocalDate date,
            String dayLabel,
            boolean saturday,
            boolean sunday,
            boolean holiday
    ) {
    }

    private record EmployeeScopeRow(
            String employeeKey,
            Long empId,
            Long usrId,
            String empNo,
            String empNm,
            String deptCd,
            String deptNm
    ) {
    }

    private record EmployeeDayCell(
            LocalDate date,
            String dayLabel,
            String absenceCode,
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

    private record EmployeeMonthSheet(
            String employeeKey,
            int no,
            String empNo,
            String empNm,
            String deptCd,
            String deptNm,
            List<EmployeeDayCell> dayCells,
            int totalWorkMinutes
    ) {
    }

    private record MonthlyExportData(
            YearMonth yearMonth,
            boolean ownOnlyYn,
            List<CalendarDayMeta> calendarDays,
            List<EmployeeMonthSheet> employeeSheets
    ) {
    }
}
