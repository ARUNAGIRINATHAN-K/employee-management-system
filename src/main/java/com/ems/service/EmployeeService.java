package com.ems.service;

import com.ems.entity.Employee;
import com.ems.entity.User;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.UserRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(String search, int page, int size, String sortBy, String sortDir, Long departmentId) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        if (search != null && !search.trim().isEmpty()) {
            return employeeRepository.searchEmployees(search, departmentId, pageable);
        }
        if (departmentId != null) {
            return employeeRepository.findByDepartmentIdAndStatusNot(departmentId, "DELETED", pageable);
        }
        return employeeRepository.findByStatusNot("DELETED", pageable);
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeByIdScoped(Long id, Long departmentId) {
        Employee employee = getEmployeeById(id);
        if (departmentId != null) {
            Long employeeDepartmentId = employee.getDepartment() != null ? employee.getDepartment().getId() : null;
            if (employeeDepartmentId == null || !departmentId.equals(employeeDepartmentId)) {
                throw new AccessDeniedException("You may only view employees in your own department");
            }
        }
        return employee;
    }

    @Transactional
    public Employee createEmployee(Employee employee, String adminUsername) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isHr = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_HR".equals(a.getAuthority()));
        if (!isHr) {
            throw new AccessDeniedException("Only HR Admins may create employees");
        }
        if (employee.getEmail() != null && employeeRepository.existsByEmailIgnoreCaseAndStatusNot(employee.getEmail(), "DELETED")) {
            throw new RuntimeException("An active employee with email " + employee.getEmail() + " already exists.");
        }
        
        Employee saved = employeeRepository.save(employee);
        auditLogService.log("CREATE_EMPLOYEE", adminUsername, 
                "Created employee: " + saved.getFirstName() + " " + saved.getLastName() + " (ID: " + saved.getId() + ")");
        return saved;
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee employeeDetails, String adminUsername) {
        Employee employee = getEmployeeById(id);
        
        employee.setFirstName(employeeDetails.getFirstName());
        employee.setLastName(employeeDetails.getLastName());
        employee.setPhone(employeeDetails.getPhone());
        employee.setJobTitle(employeeDetails.getJobTitle());
        employee.setSalary(employeeDetails.getSalary());
        employee.setHireDate(employeeDetails.getHireDate());
        if (employeeDetails.getDepartment() != null) {
            employee.setDepartment(employeeDetails.getDepartment());
        }
        if (employeeDetails.getManager() != null) {
            employee.setManager(employeeDetails.getManager());
        }
        employee.setShift(employeeDetails.getShift());
        employee.setAllowanceRate(employeeDetails.getAllowanceRate());
        employee.setDeductionRate(employeeDetails.getDeductionRate());
        
        Employee updated = employeeRepository.save(employee);
        auditLogService.log("UPDATE_EMPLOYEE", adminUsername, 
                "Updated employee profile for ID: " + id);
        return updated;
    }

    @Transactional
    public void deleteEmployee(Long id, String adminUsername) {
        Employee employee = getEmployeeById(id);
        employee.setStatus("DELETED");
        employeeRepository.save(employee);
        
        // Also deactivate/delete associated User if any
        Optional<User> userOpt = userRepository.findByUsername(employee.getEmail());
        userOpt.ifPresent(user -> {
            userRepository.delete(user);
        });

        auditLogService.log("DELETE_EMPLOYEE", adminUsername, 
                "Soft deleted employee: " + employee.getFirstName() + " " + employee.getLastName() + " (ID: " + id + ")");
    }

    @Transactional
    public Employee uploadPhoto(Long id, MultipartFile file, String adminUsername) throws IOException {
        Employee employee = getEmployeeById(id);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate a unique file name
        String fileName = id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        // Save relative path
        employee.setPhotoPath("/" + uploadDir + "/" + fileName);
        Employee updated = employeeRepository.save(employee);
        
        auditLogService.log("UPLOAD_PHOTO", adminUsername, "Uploaded profile photo for Employee ID: " + id);
        return updated;
    }

    public byte[] exportToExcel(Long departmentId) throws IOException {
        List<Employee> employees = departmentId != null
                ? employeeRepository.findByDepartmentIdAndStatusNot(departmentId, "DELETED")
                : employeeRepository.findByStatusNot("DELETED");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Employees");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "First Name", "Last Name", "Email", "Phone", "Job Title", "Department", "Salary", "Status", "Hire Date"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (Employee e : employees) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(e.getFirstName());
                row.createCell(2).setCellValue(e.getLastName());
                row.createCell(3).setCellValue(e.getEmail());
                row.createCell(4).setCellValue(e.getPhone() != null ? e.getPhone() : "");
                row.createCell(5).setCellValue(e.getJobTitle() != null ? e.getJobTitle() : "");
                row.createCell(6).setCellValue(e.getDepartment() != null ? e.getDepartment().getName() : "N/A");
                row.createCell(7).setCellValue(e.getSalary() != null ? e.getSalary() : 0.0);
                row.createCell(8).setCellValue(e.getStatus());
                row.createCell(9).setCellValue(e.getHireDate() != null ? e.getHireDate().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportToPdf(Long departmentId) {
        List<Employee> employees = departmentId != null
                ? employeeRepository.findByDepartmentIdAndStatusNot(departmentId, "DELETED")
                : employeeRepository.findByStatusNot("DELETED");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate()); // Landscape for wide tables
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("Employee Management System - Employee Directory", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Report Generated on: " + LocalDate.now()));
            document.add(Chunk.NEWLINE);

            // Table Headers
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100f);
            table.setWidths(new float[] {1f, 2.5f, 3.5f, 2.5f, 2.5f, 2f, 2f, 1.5f});

            String[] headers = {"ID", "Name", "Email", "Phone", "Job Title", "Department", "Salary", "Status"};
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(110, 89, 222)); // Deep Purple Theme Color
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Content
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (Employee e : employees) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(e.getId()), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(e.getFirstName() + " " + e.getLastName(), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(e.getEmail(), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(e.getPhone() != null ? e.getPhone() : "", bodyFont)));
                table.addCell(new PdfPCell(new Phrase(e.getJobTitle() != null ? e.getJobTitle() : "", bodyFont)));
                table.addCell(new PdfPCell(new Phrase(e.getDepartment() != null ? e.getDepartment().getName() : "N/A", bodyFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(e.getSalary() != null ? e.getSalary() : 0.0), bodyFont)));
                table.addCell(new PdfPCell(new Phrase(e.getStatus(), bodyFont)));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while generating PDF report", e);
        }
    }
}
