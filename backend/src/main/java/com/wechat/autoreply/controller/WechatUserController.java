package com.wechat.autoreply.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.autoreply.entity.WechatUser;
import com.wechat.autoreply.service.WechatUserService;
import com.wechat.autoreply.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "用户管理", description = "微信用户管理相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class WechatUserController {

    @Resource
    private WechatUserService wechatUserService;

    @GetMapping("/page")
    @Operation(summary = "获取用户列表")
    public Result<Page<WechatUser>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Long size,
            @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "是否关注") @RequestParam(required = false) Integer subscribe,
            @Parameter(description = "标签ID") @RequestParam(required = false) String tagId) {

        Page<WechatUser> page = new Page<>(current, size);
        LambdaQueryWrapper<WechatUser> wrapper = new LambdaQueryWrapper<>();

        if (nickname != null && !nickname.isEmpty()) {
            wrapper.like(WechatUser::getNickname, nickname);
        }
        if (subscribe != null) {
            wrapper.eq(WechatUser::getSubscribe, subscribe);
        }
        if (tagId != null && !tagId.isEmpty()) {
            wrapper.like(WechatUser::getTagIds, tagId);
        }
        wrapper.orderByDesc(WechatUser::getSubscribeTime);

        Page<WechatUser> userPage = wechatUserService.page(page, wrapper);
        return Result.success(userPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<WechatUser> detail(@PathVariable Long id) {
        WechatUser user = wechatUserService.getById(id);
        return Result.success(user);
    }

    @PutMapping("/{id}/remark")
    @Operation(summary = "更新用户备注")
    public Result<Void> updateRemark(@PathVariable Long id, @RequestParam String remark) {
        WechatUser user = new WechatUser();
        user.setId(id);
        user.setRemark(remark);
        wechatUserService.updateById(user);
        return Result.success();
    }

    @PutMapping("/batch-tag")
    @Operation(summary = "批量打标签")
    public Result<Void> batchTag(
            @RequestBody List<Long> userIds,
            @RequestParam Long tagId) {
        wechatUserService.batchAddTags(userIds, tagId);
        return Result.success();
    }

    @PutMapping("/{userId}/remove-tag/{tagId}")
    @Operation(summary = "移除用户标签")
    public Result<Void> removeTag(@PathVariable Long userId, @PathVariable Long tagId) {
        wechatUserService.removeTag(userId, tagId);
        return Result.success();
    }

    @PutMapping("/{id}/blacklist")
    @Operation(summary = "拉黑用户")
    public Result<Void> blacklist(@PathVariable Long id, @RequestParam Boolean enable) {
        WechatUser user = new WechatUser();
        user.setId(id);
        user.setSubscribe(enable ? 0 : 1);
        wechatUserService.updateById(user);
        return Result.success();
    }
}
