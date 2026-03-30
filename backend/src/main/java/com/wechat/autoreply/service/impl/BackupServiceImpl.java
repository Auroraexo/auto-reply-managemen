package com.wechat.autoreply.service.impl;

import com.wechat.autoreply.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class BackupServiceImpl implements BackupService {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${backup.path:./backups}")
    private String backupPath;

    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";

    @PostConstruct
    public void init() {
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
    }

    @Override
    public Map<String, Object> backupDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            String dbName = extractDatabaseName(databaseUrl);
            String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            String filename = String.format("%s_%s.sql", dbName, timestamp);
            File backupFile = new File(backupPath, filename);

            List<String> command = new ArrayList<>();
            command.add("mysqldump");
            command.add("-u" + username);
            command.add("-p" + password);
            command.add("--default-character-set=utf8mb4");
            command.add(dbName);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupFile), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                result.put("success", true);
                result.put("filename", filename);
                result.put("size", backupFile.length());
                result.put("message", "备份成功");
                log.info("数据库备份成功：{}", filename);
            } else {
                result.put("success", false);
                result.put("message", "备份失败，退出码：" + exitCode);
                log.error("数据库备份失败，退出码：{}", exitCode);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "备份异常：" + e.getMessage());
            log.error("数据库备份异常", e);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listBackupFiles() {
        List<Map<String, Object>> backups = new ArrayList<>();
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) return backups;

        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
        if (files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            for (File file : files) {
                Map<String, Object> backup = new HashMap<>();
                backup.put("filename", file.getName());
                backup.put("size", file.length());
                backup.put("createTime", new Date(file.lastModified()));
                backups.add(backup);
            }
        }
        return backups;
    }

    @Override
    public File getBackupFile(String filename) {
        return new File(backupPath, filename);
    }

    @Override
    public void deleteBackupFile(String filename) {
        File file = new File(backupPath, filename);
        if (file.exists()) {
            file.delete();
            log.info("删除备份文件：{}", filename);
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        int lastSlashIndex = jdbcUrl.lastIndexOf('/');
        int questionMarkIndex = jdbcUrl.indexOf('?');
        if (lastSlashIndex != -1) {
            int endIndex = questionMarkIndex != -1 ? questionMarkIndex : jdbcUrl.length();
            return jdbcUrl.substring(lastSlashIndex + 1, endIndex);
        }
        return "wechat_auto_reply";
    }
}
