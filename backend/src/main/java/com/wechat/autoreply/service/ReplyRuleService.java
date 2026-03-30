package com.wechat.autoreply.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.autoreply.dto.ReplyRuleDTO;
import com.wechat.autoreply.entity.ReplyRule;

import java.util.List;

/**
 * 回复规则管理服务接口
 */
public interface ReplyRuleService {

    /**
     * 分页查询回复规则
     *
     * @param page      页码
     * @param size      每页大小
     * @param ruleName  规则名称（可选）
     * @param ruleType  规则类型（可选）
     * @param isEnabled 是否启用（可选）
     * @return 分页结果
     */
    Page<ReplyRuleDTO> getPageRules(int page, int size, String ruleName, String ruleType, Boolean isEnabled);

    /**
     * 根据 ID 查询回复规则
     *
     * @param id 规则 ID
     * @return 回复规则详情
     */
    ReplyRuleDTO getRuleById(Long id);

    /**
     * 创建回复规则
     *
     * @param ruleDTO 回复规则 DTO
     * @return 创建的规则
     */
    ReplyRuleDTO createRule(ReplyRuleDTO ruleDTO);

    /**
     * 更新回复规则
     *
     * @param id      规则 ID
     * @param ruleDTO 回复规则 DTO
     * @return 更新后的规则
     */
    ReplyRuleDTO updateRule(Long id, ReplyRuleDTO ruleDTO);

    /**
     * 删除回复规则
     *
     * @param id 规则 ID
     */
    void deleteRule(Long id);

    /**
     * 批量删除回复规则
     *
     * @param ids 规则 ID 列表
     */
    void batchDeleteRules(List<Long> ids);

    /**
     * 启用/禁用回复规则
     *
     * @param id      规则 ID
     * @param enabled 是否启用
     */
    void setRuleEnabled(Long id, boolean enabled);

    /**
     * 获取所有回复规则
     *
     * @return 规则列表
     */
    List<ReplyRuleDTO> getAllRules();

    /**
     * 测试规则匹配
     *
     * @param msgType   消息类型
     * @param content   消息内容
     * @param eventType 事件类型
     * @return 匹配的规则
     */
    ReplyRuleDTO testMatch(String msgType, String content, String eventType);
}
