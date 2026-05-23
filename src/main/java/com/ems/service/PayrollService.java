package com.ems.service;

import com.ems.entity.Employee;
import com.ems.entity.Payroll;
import com.ems.entity.ExpenseClaim;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.PayrollRepository;
import com.ems.repository.ExpenseClaimRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ExpenseClaimRepository expenseClaimRepository;

    @Transactional(readOnly = true)
    public List<Payroll> getPayrollHistory(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Payroll> getPayrollByPeriod(String payPeriod) {
        return payrollRepository.findByPayPeriod(payPeriod);
    }

    @Transactional
    public Payroll generatePayroll(Long employeeId, String payPeriod, String adminUsername) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Optional<Payroll> existing = payrollRepository.findByEmployeeIdAndPayPeriod(employeeId, payPeriod);
        if (existing.isPresent()) {
            throw new RuntimeException("Payroll already generated for Employee ID: " + employeeId + " and Period: " + payPeriod);
        }

        double basic = employee.getSalary() != null ? employee.getSalary() : 0.0;
        double allowanceRate = employee.getAllowanceRate() != null ? employee.getAllowanceRate() : 0.12;
        double deductionRate = employee.getDeductionRate() != null ? employee.getDeductionRate() : 0.08;

        // Parse payPeriod to find start and end of calendar month
        LocalDate startDate;
        LocalDate endDate;
        try {
            String[] parts = payPeriod.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } catch (Exception e) {
            throw new RuntimeException("Invalid pay period format. Expected YYYY-MM.");
        }

        // Query approved expense claims
        List<ExpenseClaim> approvedClaims = expenseClaimRepository.findForPayroll(employeeId, "APPROVED", startDate, endDate);
        double expenseSum = 0.0;
        for (ExpenseClaim claim : approvedClaims) {
            expenseSum += claim.getAmount();
        }

        double standardAllowances = Math.round(basic * allowanceRate * 100.0) / 100.0;
        double allowances = standardAllowances + expenseSum;
        double deductions = Math.round(basic * deductionRate * 100.0) / 100.0;
        double net = basic + allowances - deductions;

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .payPeriod(payPeriod)
                .basicSalary(basic)
                .allowances(allowances)
                .deductions(deductions)
                .netSalary(net)
                .status("PENDING")
                .build();

        Payroll saved = payrollRepository.save(payroll);

        // Mark claims as PAID
        for (ExpenseClaim claim : approvedClaims) {
            claim.setStatus("PAID");
            expenseClaimRepository.save(claim);
        }

        auditLogService.log("PAYROLL_GENERATE", adminUsername, 
                "Generated payroll for " + employee.getFirstName() + " " + employee.getLastName() + " (Period: " + payPeriod + ") with " + approvedClaims.size() + " claims worth $" + expenseSum);
        return saved;
    }

    @Transactional
    public Payroll markAsPaid(Long payrollId, String adminUsername) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));

        if ("PAID".equals(payroll.getStatus())) {
            throw new RuntimeException("Payroll is already paid.");
        }

        payroll.setStatus("PAID");
        payroll.setProcessedDate(LocalDate.now());
        Payroll saved = payrollRepository.save(payroll);

        auditLogService.log("PAYROLL_PAY", adminUsername, 
                "Marked payroll ID: " + payrollId + " as PAID for " + payroll.getEmployee().getFirstName());
        return saved;
    }

    public byte[] generatePayslipPdf(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll record not found"));

        Employee e = payroll.getEmployee();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Styling colors
            Color themeColor = new Color(110, 89, 222); // Deep Purple
            Color lightGray = new Color(245, 245, 245);

            // Invoice / Payslip Title
            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, themeColor);
            Paragraph companyTitle = new Paragraph("EMS CORPORATION", companyFont);
            companyTitle.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(companyTitle);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Paragraph companySubtitle = new Paragraph("123 Business Center, Tech Park, City, Country\nEmail: contact@emscorp.com", subtitleFont);
            companySubtitle.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(companySubtitle);

            document.add(new Paragraph("\n"));
            
            // Draw a separator line
            LineSeparator line = new LineSeparator();
            line.setLineColor(themeColor);
            line.setPercentage(100);
            document.add(new Chunk(line));

            document.add(new Paragraph("\n"));

            // Summary Details Table (2 columns)
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingAfter(15);

            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            detailsTable.addCell(createBorderLessCell("Employee Name: " + e.getFirstName() + " " + e.getLastName(), normalFont));
            detailsTable.addCell(createBorderLessCell("Pay Period: " + payroll.getPayPeriod(), boldFont));

            detailsTable.addCell(createBorderLessCell("Employee Email: " + e.getEmail(), normalFont));
            detailsTable.addCell(createBorderLessCell("Status: " + payroll.getStatus(), boldFont));

            detailsTable.addCell(createBorderLessCell("Job Title: " + (e.getJobTitle() != null ? e.getJobTitle() : "N/A"), normalFont));
            detailsTable.addCell(createBorderLessCell("Processed Date: " + (payroll.getProcessedDate() != null ? payroll.getProcessedDate().toString() : "N/A"), normalFont));

            detailsTable.addCell(createBorderLessCell("Department: " + (e.getDepartment() != null ? e.getDepartment().getName() : "N/A"), normalFont));
            detailsTable.addCell(createBorderLessCell("Payslip ID: #" + payroll.getId(), normalFont));

            document.add(detailsTable);

            // Financial Breakdown Table
            PdfPTable breakdownTable = new PdfPTable(2);
            breakdownTable.setWidthPercentage(100);
            breakdownTable.setSpacingAfter(20);

            // Table Headers
            PdfPCell h1 = new PdfPCell(new Phrase("Description", boldFont));
            h1.setBackgroundColor(lightGray);
            h1.setPadding(8);
            breakdownTable.addCell(h1);

            PdfPCell h2 = new PdfPCell(new Phrase("Amount ($)", boldFont));
            h2.setBackgroundColor(lightGray);
            h2.setPadding(8);
            h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            breakdownTable.addCell(h2);

            // Calculate dynamic rates for PDF display
            double basicVal = payroll.getBasicSalary() != null && payroll.getBasicSalary() > 0 ? payroll.getBasicSalary() : 1.0;
            double allowancePercent = Math.round((payroll.getAllowances() / basicVal) * 100.0);
            double deductionPercent = Math.round((payroll.getDeductions() / basicVal) * 100.0);

            // Add Details
            addBreakdownRow(breakdownTable, "Basic Salary", payroll.getBasicSalary(), normalFont);
            addBreakdownRow(breakdownTable, "Allowances (" + (int) allowancePercent + "% of Basic)", payroll.getAllowances(), normalFont);
            addBreakdownRow(breakdownTable, "Deductions (" + (int) deductionPercent + "% tax & benefits)", -payroll.getDeductions(), normalFont);

            // Net Salary summary row
            PdfPCell netCellLabel = new PdfPCell(new Phrase("Net Salary Paid", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, themeColor)));
            netCellLabel.setPadding(10);
            netCellLabel.setBackgroundColor(lightGray);
            breakdownTable.addCell(netCellLabel);

            PdfPCell netCellVal = new PdfPCell(new Phrase("$" + payroll.getNetSalary(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, themeColor)));
            netCellVal.setPadding(10);
            netCellVal.setBackgroundColor(lightGray);
            netCellVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            breakdownTable.addCell(netCellVal);

            document.add(breakdownTable);

            // Signature block
            document.add(new Paragraph("\n\n\n"));
            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            sigTable.addCell(createBorderLessCell("_____________________\nEmployee Signature", normalFont));
            
            PdfPCell mgrSig = createBorderLessCell("_____________________\nHR Manager Signature", normalFont);
            mgrSig.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sigTable.addCell(mgrSig);
            document.add(sigTable);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while generating Payslip PDF", ex);
        }
    }

    private PdfPCell createBorderLessCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private void addBreakdownRow(PdfPTable table, String desc, double amount, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(desc, font));
        c1.setPadding(8);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(String.format("%.2f", amount), font));
        c2.setPadding(8);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);
    }
}
