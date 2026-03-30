package com.wechat.autoreply.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.autoreply.entity.WechatConfig;
import com.wechat.autoreply.mapper.WechatConfigMapper;
import com.wechat.autoreply.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/admin/config")
@Tag(name = "公众号配置", description = "公众号配置管理")
@PreAuthorize("hasRole('ADMIN')")
public class WechatConfigController {

    @Resource
    private WechatConfigMapper wechatConfigMapper;

    @GetMapping
    @Operation(summary = "获取公众号配置")
    public Result<WechatConfig> get() {
        WechatConfig config = wechatConfigMapper.selectOne(
            new LambdaQueryWrapper<WechatConfig>().eq(WechatConfig::getIsDefault, true)
        );
        if (config != null) {
            config.setSecret(null); // 不返回 secret
            config.setAccessToken(null);
        }
        return Result.success(config);
    }

    @PostMapping
    @Operation(summary = "保存公众号配置")
    public Result<Void> save(@RequestBody WechatConfig config) {
        WechatConfig existing = wechatConfigMapper.selectOne(
            new LambdaQueryWrapper<WechatConfig>().eq(WechatConfig::getIsDefault, true)
        );
        if (existing != null) {
            config.setId(existing.getId());
            wechatConfigMapper.updateById(config);
        } else {
            config.setIsDefault(true);
            wechatConfigMapper.insert(config);
        }
        return Result.success();
    }
}
