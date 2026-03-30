package com.wechat.autoreply.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.autoreply.dto.ReplyRuleDTO;
import com.wechat.autoreply.service.ReplyRuleService;
import com.wechat.autoreply.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 回复规则管理控制器
 */
@RestController
@RequestMapping("/admin/rules")
@Tag(name = "回复规则管理", description = "自动回复规则配置管理")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class ReplyRuleController {

    @Resource
    private ReplyRuleService replyRuleService;

    /**
     * 分页查询回复规则
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询回复规则")
    public Result<Page<ReplyRuleDTO>> getPageRules(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "规则名称") @RequestParam(required = false) String ruleName,
            @Parameter(description = "规则类型") @RequestParam(required = false) String ruleType,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean isEnabled) {
        
        Page<ReplyRuleDTO> result = replyRuleService.getPageRules(page, size, ruleName, ruleType, isEnabled);
        return Result.success(result);
    }

    /**
     * 根据 ID 查询回复规则
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询回复规则")
    public Result<ReplyRuleDTO> getRuleById(@Parameter(description = "规则 ID") @PathVariable Long id) {
        ReplyRuleDTO rule = replyRuleService.getRuleById(id);
        return Result.success(rule);
    }

    /**
     * 创建回复规则
     */
    @PostMapping
    @Operation(summary = "创建回复规则")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ReplyRuleDTO> createRule(@RequestBody ReplyRuleDTO ruleDTO) {
        ReplyRuleDTO result = replyRuleService.createRule(ruleDTO);
        return Result.success(result);
    }

    /**
     * 更新回复规则
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新回复规则")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ReplyRuleDTO> updateRule(
            @Parameter(description = "规则 ID") @PathVariable Long id,
            @RequestBody ReplyRuleDTO ruleDTO) {
        ReplyRuleDTO result = replyRuleService.updateRule(id, ruleDTO);
        return Result.success(result);
    }

    /**
     * 删除回复规则
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除回复规则")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteRule(@Parameter(description = "规则 ID") @PathVariable Long id) {
        replyRuleService.deleteRule(id);
        return Result.success();
    }

    /**
     * 批量删除回复规则
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除回复规则")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> batchDeleteRules(@RequestBody List<Long> ids) {
        replyRuleService.batchDeleteRules(ids);
        return Result.success();
    }

    /**
     * 启用/禁用回复规则
     */
    @PutMapping("/{id}/enabled")
    @Operation(summary = "启用/禁用回复规则")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> setRuleEnabled(
            @Parameter(description = "规则 ID") @PathVariable Long id,
            @Parameter(description = "是否启用", example = "true") @RequestParam boolean enabled) {
        replyRuleService.setRuleEnabled(id, enabled);
        return Result.success();
    }

    /**
     * 获取所有回复规则
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有回复规则")
    public Result<List<ReplyRuleDTO>> getAllRules() {
        List<ReplyRuleDTO> rules = replyRuleService.getAllRules();
        return Result.success(rules);
    }

    /**
     * 测试规则匹配
     */
    @PostMapping("/test-match")
    @Operation(summary = "测试规则匹配")
    public Result<ReplyRuleDTO> testMatch(
            @Parameter(description = "消息类型", required = true) @RequestParam String msgType,
            @Parameter(description = "消息内容") @RequestParam(required = false) String content,
            @Parameter(description = "事件类型") @RequestParam(required = false) String eventType) {
        
        ReplyRuleDTO matchedRule = replyRuleService.testMatch(msgType, content, eventType);
        return Result.success(matchedRule);
    }
}
