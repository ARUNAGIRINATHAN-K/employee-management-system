package com.ems.service;

import com.ems.entity.Attendance;
import com.ems.entity.Employee;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public Attendance checkIn(Long employeeId, String username) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);
        if (existing.isPresent()) {
            throw new RuntimeException("Already checked in for today.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime checkInTime = now.toLocalTime();
        
        // Late marking policy: Standard shift starts at 09:00 AM. Grace period till 09:15 AM.
        String status = "PRESENT";
        String notes = "Checked in on time";
        if (checkInTime.isAfter(LocalTime.of(9, 15))) {
            status = "LATE";
            notes = "Checked in late at " + checkInTime.toString().substring(0, 5);
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(today)
                .checkIn(now)
                .status(status)
                .notes(notes)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        auditLogService.log("ATTENDANCE_CHECKIN", username, "Checked in at " + now + " (ID: " + saved.getId() + ")");
        return saved;
    }

    @Transactional
    public Attendance checkOut(Long employeeId, String username) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new RuntimeException("No check-in log found for today. Check-in first."));

        if (attendance.getCheckOut() != null) {
            throw new RuntimeException("Already checked out for today.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime checkOutTime = now.toLocalTime();
        attendance.setCheckOut(now);

        // Early departure check: standard shift ends at 05:00 PM.
        if (checkOutTime.isBefore(LocalTime.of(17, 0))) {
            if (!"LATE".equals(attendance.getStatus())) {
                attendance.setStatus("EARLY_DEPARTURE");
            }
            attendance.setNotes(attendance.getNotes() + "; Left early at " + checkOutTime.toString().substring(0, 5));
        } else {
            attendance.setNotes(attendance.getNotes() + "; Checked out on time");
        }

        Attendance saved = attendanceRepository.save(attendance);
        auditLogService.log("ATTENDANCE_CHECKOUT", username, "Checked out at " + now + " (ID: " + saved.getId() + ")");
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Attendance> getTodayAttendance(Long employeeId) {
        return attendanceRepository.findByEmployeeIdAndDate(employeeId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Attendance> getEmployeeAttendanceHistory(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }
}
