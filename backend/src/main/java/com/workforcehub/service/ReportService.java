package com.workforcehub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    public byte[] exportEmployeesToExcel() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Employees");
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Department");
            headerRow.createCell(3).setCellValue("Designation");
            
            // Mock data population
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("EMP-001");
            row.createCell(1).setCellValue("John Doe");
            row.createCell(2).setCellValue("Engineering");
            row.createCell(3).setCellValue("Developer");

            workbook.write(out);
            log.info("Excel report generated successfully.");
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Excel report", e);
            throw new RuntimeException("Excel generation failed");
        }
    }

    public byte[] exportEmployeesToPdf() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Employee Directory Report"));
            document.add(new Paragraph("--------------------------------------------------"));
            document.add(new Paragraph("ID: EMP-001 | Name: John Doe | Dept: Engineering"));
            document.close();
            log.info("PDF report generated successfully.");
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("PDF generation failed");
        }
    }
}
