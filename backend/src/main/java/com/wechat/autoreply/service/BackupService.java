package com.wechat.autoreply.service;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 数据备份服务接口
 */
public interface BackupService {

    /**
     * 备份数据库
     *
     * @return 备份结果
     */
    Map<String, Object> backupDatabase();

    /**
     * 获取备份文件列表
     *
     * @return 备份文件列表
     */
    List<Map<String, Object>> listBackupFiles();

    /**
     * 获取备份文件
     *
     * @param filename 文件名
     * @return 备份文件
     */
    File getBackupFile(String filename);

    /**
     * 删除备份文件
     *
     * @param filename 文件名
     */
    void deleteBackupFile(String filename);
}
