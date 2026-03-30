package com.wechat.autoreply.controller;

import com.wechat.autoreply.service.StatsService;
import com.wechat.autoreply.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/admin/stats")
@Tag(name = "数据统计", description = "数据统计相关接口")
public class StatsController {

    @Resource
    private StatsService statsService;

    @GetMapping("/overview")
    @Operation(summary = "获取概览数据")
    public Result<Object> getOverview() {
        return Result.success(statsService.getOverview());
    }

    @GetMapping("/users")
    @Operation(summary = "获取粉丝统计")
    public Result<Object> getUserStats(
            @Parameter(description = "时间范围：week/month")
            @RequestParam(defaultValue = "week") String range) {
        return Result.success(statsService.getUserStats(range));
    }

    @GetMapping("/messages")
    @Operation(summary = "获取消息统计")
    public Result<Object> getMessageStats(
            @Parameter(description = "时间范围：week/month")
            @RequestParam(defaultValue = "week") String range) {
        return Result.success(statsService.getMessageStats(range));
    }

    @GetMapping("/rules")
    @Operation(summary = "获取规则统计")
    public Result<Object> getRuleStats() {
        return Result.success(statsService.getRuleStats());
    }
}
