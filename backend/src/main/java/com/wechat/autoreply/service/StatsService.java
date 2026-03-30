package com.wechat.autoreply.service;

import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatsService {

    /**
     * 获取概览数据
     *
     * @return 概览数据
     */
    Map<String, Object> getOverview();

    /**
     * 获取粉丝统计数据
     *
     * @param range 时间范围（week/month）
     * @return 粉丝统计数据
     */
    Map<String, Object> getUserStats(String range);

    /**
     * 获取消息统计数据
     *
     * @param range 时间范围（week/month）
     * @return 消息统计数据
     */
    Map<String, Object> getMessageStats(String range);

    /**
     * 获取规则触发统计
     *
     * @return 规则触发统计
     */
    Map<String, Object> getRuleStats();
}
