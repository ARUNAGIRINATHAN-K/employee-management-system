package com.ems.controller;

import com.ems.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/subscribe")
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribe() {
        var principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal == null || !(principal.getPrincipal() instanceof com.ems.security.UserPrincipal)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        return notificationService.subscribe();
    }
}
