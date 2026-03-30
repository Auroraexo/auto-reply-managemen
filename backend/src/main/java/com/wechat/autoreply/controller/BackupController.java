package com.wechat.autoreply.controller;

import com.wechat.autoreply.service.BackupService;
import com.wechat.autoreply.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/admin/backup")
@Tag(name = "数据备份", description = "数据库备份相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    @Resource
    private BackupService backupService;

    @PostMapping("/manual")
    @Operation(summary = "手动备份数据库")
    public Result<Map<String, Object>> manualBackup() {
        Map<String, Object> result = backupService.backupDatabase();
        return Result.success(result);
    }

    @GetMapping("/list")
    @Operation(summary = "获取备份文件列表")
    public Result<List<Map<String, Object>>> listBackups() {
        List<Map<String, Object>> backups = backupService.listBackupFiles();
        return Result.success(backups);
    }

    @GetMapping("/download/{filename}")
    @Operation(summary = "下载备份文件")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable String filename) {
        try {
            File file = backupService.getBackupFile(filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] content = fis.readAllBytes();
            fis.close();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok().headers(headers).body(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{filename}")
    @Operation(summary = "删除备份文件")
    public Result<Void> deleteBackup(@PathVariable String filename) {
        backupService.deleteBackupFile(filename);
        return Result.success();
    }
}
