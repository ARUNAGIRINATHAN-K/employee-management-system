package com.ems.controller;

import com.ems.entity.AuditLog;
import com.ems.entity.Employee;
import com.ems.entity.User;
import com.ems.service.AuditLogService;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/export")
public class AdminExportController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportAuditLogs() throws Exception {
        List<AuditLog> logs = auditLogService.getAllLogs();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Audit Logs");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Timestamp");
            header.createCell(1).setCellValue("Action");
            header.createCell(2).setCellValue("User");
            header.createCell(3).setCellValue("Details");

            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            for (AuditLog log : logs) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(log.getTimestamp().format(fmt));
                r.createCell(1).setCellValue(log.getAction());
                r.createCell(2).setCellValue(log.getUsername());
                r.createCell(3).setCellValue(log.getDetails());
            }
            workbook.write(out);
            byte[] data = out.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audits.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        }
    }

    @GetMapping("/company-directory")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportCompanyDirectory() throws Exception {
        List<Employee> employees = employeeRepository.findAll();
        List<User> users = userRepository.findAll();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Employees");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Employee ID");
            header.createCell(1).setCellValue("First Name");
            header.createCell(2).setCellValue("Last Name");
            header.createCell(3).setCellValue("Email");
            header.createCell(4).setCellValue("Department");
            header.createCell(5).setCellValue("Manager ID");

            for (Employee e : employees) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(e.getId() != null ? e.getId() : 0);
                r.createCell(1).setCellValue(e.getFirstName() != null ? e.getFirstName() : "");
                r.createCell(2).setCellValue(e.getLastName() != null ? e.getLastName() : "");
                r.createCell(3).setCellValue(e.getEmail() != null ? e.getEmail() : "");
                r.createCell(4).setCellValue(e.getDepartment() != null && e.getDepartment().getName() != null ? e.getDepartment().getName() : "");
                r.createCell(5).setCellValue(e.getManager() != null && e.getManager().getId() != null ? e.getManager().getId() : 0);
            }

            XSSFSheet uSheet = workbook.createSheet("Users");
            int uRow = 0;
            Row uh = uSheet.createRow(uRow++);
            uh.createCell(0).setCellValue("User ID");
            uh.createCell(1).setCellValue("Username");
            uh.createCell(2).setCellValue("Role");
            uh.createCell(3).setCellValue("Employee ID");
            uh.createCell(4).setCellValue("Active");
            for (User u : users) {
                Row r = uSheet.createRow(uRow++);
                r.createCell(0).setCellValue(u.getId() != null ? u.getId() : 0);
                r.createCell(1).setCellValue(u.getUsername() != null ? u.getUsername() : "");
                r.createCell(2).setCellValue(u.getRole() != null ? u.getRole() : "");
                r.createCell(3).setCellValue(u.getEmployee() != null && u.getEmployee().getId() != null ? u.getEmployee().getId() : 0);
                r.createCell(4).setCellValue(u.getActive() != null ? u.getActive() : true);
            }

            workbook.write(out);
            byte[] data = out.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=company_directory.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        }
    }
}
