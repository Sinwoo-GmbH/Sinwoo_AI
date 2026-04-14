package com.sinwoo.attendance.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sinwoo.attendance.domain.AttendanceRecord;
import com.sinwoo.attendance.dto.AttendanceReportExportFile;
import com.sinwoo.attendance.dto.AttendanceWorkTimeFilterOptionResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeFilterOptionsResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryListResponse;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryQuery;
import com.sinwoo.attendance.dto.AttendanceWorkTimeHistoryRowResponse;
import com.sinwoo.attendance.repository.AttendanceRecordRepository;
import com.sinwoo.code.service.CommonCodeService;
import com.sinwoo.common.security.AuthenticatedUser;
import com.sinwoo.company.repository.CompanyRepository;
import com.sinwoo.department.domain.Department;
import com.sinwoo.department.repository.DepartmentRepository;
import com.sinwoo.employee.domain.Employee;
import com.sinwoo.employee.repository.EmployeeRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
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
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<String> WINDOWS_KOREAN_FONT_PATHS = List.of(
            "C:/Windows/Fonts/malgun.ttf",
            "C:/Windows/Fonts/NanumGothic.ttf"
    );

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final CommonCodeService commonCodeService;

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
                .map(employee -> new AttendanceWorkTimeFilterOptionResponse(employee.getId(), employee.getEmpNo(), employee.getEmpNm()))
                .toList();
        List<AttendanceWorkTimeFilterOptionResponse> departmentOptions = reportContext.departmentList().stream()
                .map(department -> new AttendanceWorkTimeFilterOptionResponse(department.getId(), department.getDeptCd(), department.getDeptNm()))
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
        AttendanceWorkTimeHistoryListResponse history = getWorkTimeHistory(authenticatedUser, query, locale);
        byte[] content = writeExcel(history, locale);
        return new AttendanceReportExportFile(
                buildExportFileName("work-time-history", history.yearMonth(), "xlsx"),
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
        AttendanceWorkTimeHistoryListResponse history = getWorkTimeHistory(authenticatedUser, query, locale);
        byte[] content = writePdf(history, locale);
        return new AttendanceReportExportFile(
                buildExportFileName("work-time-history", history.yearMonth(), "pdf"),
                "application/pdf",
                content
        );
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
                    .map(com.sinwoo.company.domain.Company::getId)
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

    private byte[] writeExcel(AttendanceWorkTimeHistoryListResponse history, Locale locale) {
        ReportLabels labels = resolveLabels(locale);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(labels.sheetNm());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            String[] headerLabels = labels.tableHeaders();
            for (int index = 0; index < headerLabels.length; index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(headerLabels[index]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (AttendanceWorkTimeHistoryRowResponse item : history.itemList()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(item.attndDt());
                row.createCell(1).setCellValue(nullToBlank(item.empNm()));
                row.createCell(2).setCellValue(nullToBlank(item.deptNm()));
                row.createCell(3).setCellValue(nullToBlank(item.attndStsNm()));
                row.createCell(4).setCellValue(nullToBlank(item.chkinTm()));
                row.createCell(5).setCellValue(nullToBlank(item.chkoutTm()));
                row.createCell(6).setCellValue(formatWorkTime(item.workMinuteCnt()));
            }

            for (int index = 0; index < headerLabels.length; index++) {
                sheet.autoSizeColumn(index);
                sheet.setColumnWidth(index, Math.max(sheet.getColumnWidth(index), 3600));
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export work time history to Excel", exception);
        }
    }

    private byte[] writePdf(AttendanceWorkTimeHistoryListResponse history, Locale locale) {
        ReportLabels labels = resolveLabels(locale);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = resolvePdfFont(locale, 15f, Font.BOLD);
            Font bodyFont = resolvePdfFont(locale, 9f, Font.NORMAL);

            Paragraph title = new Paragraph(labels.pdfTitle(history.yearMonth()), titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(title);
            document.add(new Paragraph(" ", bodyFont));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100f);
            table.setWidths(new float[]{1.3f, 1.7f, 1.7f, 1.4f, 1.1f, 1.1f, 1.1f});
            for (String header : labels.tableHeaders()) {
                PdfPCell cell = new PdfPCell(new Phrase(header, bodyFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6f);
                table.addCell(cell);
            }

            for (AttendanceWorkTimeHistoryRowResponse item : history.itemList()) {
                table.addCell(new Phrase(item.attndDt(), bodyFont));
                table.addCell(new Phrase(nullToBlank(item.empNm()), bodyFont));
                table.addCell(new Phrase(nullToBlank(item.deptNm()), bodyFont));
                table.addCell(new Phrase(nullToBlank(item.attndStsNm()), bodyFont));
                table.addCell(new Phrase(nullToBlank(item.chkinTm()), bodyFont));
                table.addCell(new Phrase(nullToBlank(item.chkoutTm()), bodyFont));
                table.addCell(new Phrase(formatWorkTime(item.workMinuteCnt()), bodyFont));
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export work time history to PDF", exception);
        }
    }

    private Font resolvePdfFont(Locale locale, float size, int style) throws DocumentException, IOException {
        if ("ko".equalsIgnoreCase(locale.getLanguage())) {
            for (String path : WINDOWS_KOREAN_FONT_PATHS) {
                java.io.File fontFile = new java.io.File(path);
                if (fontFile.exists()) {
                    BaseFont baseFont = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    return new Font(baseFont, size, style);
                }
            }
        }
        return FontFactory.getFont(FontFactory.HELVETICA, size, style);
    }

    private ReportLabels resolveLabels(Locale locale) {
        return switch (locale.getLanguage().toLowerCase(Locale.ROOT)) {
            case "de" -> new ReportLabels(
                    "Arbeitszeitverlauf",
                    "Arbeitszeitverlauf %s",
                    new String[]{"Datum", "Mitarbeiter", "Abteilung", "Status", "Check-in", "Check-out", "Arbeitszeit"}
            );
            case "ko" -> new ReportLabels(
                    "근태 이력",
                    "%s 근태 이력",
                    new String[]{"일자", "직원", "부서", "상태", "출근", "퇴근", "근무시간"}
            );
            default -> new ReportLabels(
                    "Work Time History",
                    "Work Time History %s",
                    new String[]{"Date", "Employee", "Department", "Status", "Check-in", "Check-out", "Work time"}
            );
        };
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
        return user.roleCds() != null && user.roleCds().stream().anyMatch(role ->
                role != null && (role.contains("_ADMIN_") || role.endsWith("_LEADER"))
        );
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
        return commonCodeService.resolveDisplayName("ATTND_FLAG", statusCd, fallbackStatusName(statusCd, locale));
    }

    private String fallbackStatusName(String statusCd, Locale locale) {
        return switch (locale.getLanguage().toLowerCase(Locale.ROOT)) {
            case "de" -> switch (statusCd) {
                case "CHECKED_IN" -> "Eingecheckt";
                case "CHECKED_OUT" -> "Ausgecheckt";
                case "LEAVE" -> "Urlaub";
                case "BUSINESS_TRIP" -> "Dienstreise";
                default -> "Bereit";
            };
            case "ko" -> switch (statusCd) {
                case "CHECKED_IN" -> "출근";
                case "CHECKED_OUT" -> "퇴근";
                case "LEAVE" -> "휴가";
                case "BUSINESS_TRIP" -> "출장";
                default -> "대기";
            };
            default -> switch (statusCd) {
                case "CHECKED_IN" -> "Checked in";
                case "CHECKED_OUT" -> "Checked out";
                case "LEAVE" -> "Leave";
                case "BUSINESS_TRIP" -> "Business trip";
                default -> "Ready";
            };
        };
    }

    private Integer calculateWorkMinuteCnt(AttendanceRecord record) {
        if (record.getChkinDtm() == null || record.getChkoutDtm() == null) {
            return 0;
        }
        long minutes = Duration.between(record.getChkinDtm(), record.getChkoutDtm()).toMinutes();
        return minutes < 0 ? 0 : Math.toIntExact(minutes);
    }

    private String formatTime(java.time.OffsetDateTime value) {
        return value == null ? null : value.toLocalTime().format(TIME_FORMAT);
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

    private String formatWorkTime(Integer workMinuteCnt) {
        int safeMinutes = workMinuteCnt == null ? 0 : workMinuteCnt;
        int hours = safeMinutes / 60;
        int minutes = safeMinutes % 60;
        return String.format(Locale.ROOT, "%02d:%02d", hours, minutes);
    }

    private String buildExportFileName(String prefix, String yearMonth, String extension) {
        return prefix + "-" + yearMonth + "." + extension;
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
            String deptNm,
            String attndStsCd,
            String attndStsNm,
            String chkinTm,
            String chkoutTm,
            Integer workMinuteCnt,
            String keywordText
    ) {
    }

    private record ReportLabels(
            String sheetNm,
            String pdfTitleTemplate,
            String[] tableHeaders
    ) {
        String pdfTitle(String yearMonth) {
            return pdfTitleTemplate.formatted(yearMonth);
        }
    }
}
