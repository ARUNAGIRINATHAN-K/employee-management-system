package com.ems.controller;

import com.ems.entity.SystemConfig;
import com.ems.service.SystemConfigService;
import com.ems.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/system-config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService service;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_HR')")
    public ResponseEntity<List<SystemConfig>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> create(@RequestBody SystemConfig cfg) {
        SystemConfig created = service.create(cfg);
        auditLogService.log("SYSTEM_CONFIG_CREATE", "admin", "Created config: " + cfg.getConfigKey());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SystemConfig cfg) {
        SystemConfig updated = service.update(id, cfg);
        auditLogService.log("SYSTEM_CONFIG_UPDATE", "admin", "Updated config: " + cfg.getConfigKey());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        auditLogService.log("SYSTEM_CONFIG_DELETE", "admin", "Deleted config id: " + id);
        return ResponseEntity.ok(Map.of("message", "deleted"));
    }
}
