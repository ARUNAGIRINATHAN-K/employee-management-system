package com.ems.controller;

import com.ems.entity.Holiday;
import com.ems.service.HolidayService;
import com.ems.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/holidays")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Holiday>> list() {
        return ResponseEntity.ok(holidayService.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> create(@RequestBody Holiday h) {
        Holiday created = holidayService.create(h);
        auditLogService.log("CREATE_HOLIDAY", "admin", "Created holiday: " + h.getName());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Holiday h) {
        Holiday updated = holidayService.update(id, h);
        auditLogService.log("UPDATE_HOLIDAY", "admin", "Updated holiday: " + h.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        holidayService.delete(id);
        auditLogService.log("DELETE_HOLIDAY", "admin", "Deleted holiday: " + id);
        return ResponseEntity.ok(Map.of("message", "deleted"));
    }
}
