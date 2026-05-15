package com.workforcehub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@EnableScheduling
@Slf4j
public class DatabaseBackupService {

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:root}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void backupDatabase() {
        if (!dbUrl.contains("mysql")) {
            log.info("Database is not MySQL, skipping automated backup.");
            return;
        }

        String dbName = "workforcehub"; // Parse from URL if needed
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "backup_" + dbName + "_" + timestamp + ".sql";

        log.info("Starting automated database backup: {}", fileName);
        
        try {
            // This assumes mysqldump is in the PATH of the OS running the application
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUser,
                    "-p" + dbPassword,
                    dbName,
                    "-r", "backups/" + fileName
            );
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Database backup completed successfully.");
            } else {
                log.error("Database backup failed with exit code: {}", exitCode);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Exception occurred during database backup", e);
        }
    }
}
