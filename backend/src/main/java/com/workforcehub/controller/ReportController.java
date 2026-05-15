package com.workforcehub.controller;

import com.workforcehub.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Export and download reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/employees/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Export employee directory as Excel")
    public ResponseEntity<byte[]> exportEmployeesExcel() {
        byte[] data = reportService.exportEmployeesToExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/employees/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Export employee directory as PDF")
    public ResponseEntity<byte[]> exportEmployeesPdf() {
        byte[] data = reportService.exportEmployeesToPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
