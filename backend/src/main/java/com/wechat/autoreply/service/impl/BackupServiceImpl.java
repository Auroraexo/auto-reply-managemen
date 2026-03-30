package com.wechat.autoreply.service.impl;

import com.wechat.autoreply.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Slf4j
@Service
public class BackupServiceImpl implements BackupService {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${backup.path:./backups}")
    private String backupPath;

    @Autowired
    private DataSource dataSource;

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
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) backupDir.mkdirs();

            String dbName = extractDatabaseName(databaseUrl);
            String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            String filename = String.format("%s_%s.sql", dbName, timestamp);
            File backupFile = new File(backupDir.getAbsolutePath(), filename);

            log.info("开始备份数据库：{}，目标文件：{}", dbName, backupFile.getAbsolutePath());

            try (Connection conn = dataSource.getConnection();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupFile), "UTF-8"))) {

                writer.write("-- Database backup: " + dbName);
                writer.newLine();
                writer.write("-- Generated: " + new Date());
                writer.newLine();
                writer.write("SET NAMES utf8mb4;");
                writer.newLine();
                writer.write("SET FOREIGN_KEY_CHECKS=0;");
                writer.newLine();
                writer.newLine();

                DatabaseMetaData meta = conn.getMetaData();

                // 获取所有表
                try (ResultSet tables = meta.getTables(dbName, null, "%", new String[]{"TABLE"})) {
                    while (tables.next()) {
                        String tableName = tables.getString("TABLE_NAME");
                        exportTable(conn, writer, tableName);
                    }
                }

                writer.write("SET FOREIGN_KEY_CHECKS=1;");
                writer.newLine();
            }

            result.put("success", true);
            result.put("filename", filename);
            result.put("size", backupFile.length());
            result.put("message", "备份成功");
            log.info("数据库备份成功：{}", filename);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "备份异常：" + e.getMessage());
            log.error("数据库备份异常", e);
        }
        return result;
    }

    private void exportTable(Connection conn, BufferedWriter writer, String tableName) throws Exception {
        // 写入 DROP + CREATE TABLE
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {
            if (rs.next()) {
                writer.write("-- Table: " + tableName);
                writer.newLine();
                writer.write("DROP TABLE IF EXISTS `" + tableName + "`;");
                writer.newLine();
                writer.write(rs.getString(2) + ";");
                writer.newLine();
                writer.newLine();
            }
        }

        // 写入数据
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {
            ResultSetMetaData rsMeta = rs.getMetaData();
            int colCount = rsMeta.getColumnCount();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder("INSERT INTO `").append(tableName).append("` VALUES (");
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1) sb.append(", ");
                    Object val = rs.getObject(i);
                    if (val == null) {
                        sb.append("NULL");
                    } else if (val instanceof Number) {
                        sb.append(val);
                    } else {
                        sb.append("'").append(val.toString().replace("\\", "\\\\").replace("'", "\\'")).append("'");
                    }
                }
                sb.append(");");
                writer.write(sb.toString());
                writer.newLine();
            }
        }
        writer.newLine();
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
        // 去掉 jdbc:mysql:// 前缀后，找第一个 / 即库名分隔符
        // 格式: jdbc:mysql://host:port/dbname?params
        try {
            String withoutScheme = jdbcUrl.replaceFirst("^jdbc:[^:]+://", "");
            int slashIndex = withoutScheme.indexOf('/');
            if (slashIndex == -1) return "wechat_auto_reply";
            String rest = withoutScheme.substring(slashIndex + 1);
            int qIndex = rest.indexOf('?');
            return qIndex != -1 ? rest.substring(0, qIndex) : rest;
        } catch (Exception e) {
            return "wechat_auto_reply";
        }
    }
}
