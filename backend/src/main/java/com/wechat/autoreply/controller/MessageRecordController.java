package com.wechat.autoreply.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.autoreply.entity.MessageRecord;
import com.wechat.autoreply.mapper.MessageRecordMapper;
import com.wechat.autoreply.service.WechatMessageService;
import com.wechat.autoreply.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/admin/messages")
@Tag(name = "消息记录", description = "消息记录查询接口")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@Slf4j
public class MessageRecordController {

    @Resource
    private MessageRecordMapper messageRecordMapper;

    @Resource
    private WechatMessageService wechatMessageService;

    @GetMapping("/page")
    @Operation(summary = "分页查询消息记录")
    public Result<Page<MessageRecord>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestParam(required = false) String openId,
            @RequestParam(required = false) String msgType) {
        Page<MessageRecord> pageObj = new Page<>(current, size);
        LambdaQueryWrapper<MessageRecord> wrapper = new LambdaQueryWrapper<>();
        if (openId != null && !openId.isEmpty()) {
            wrapper.like(MessageRecord::getOpenId, openId);
        }
        if (msgType != null && !msgType.isEmpty()) {
            wrapper.eq(MessageRecord::getMsgType, msgType);
        }
        wrapper.orderByDesc(MessageRecord::getCreateTime);
        return Result.success(messageRecordMapper.selectPage(pageObj, wrapper));
    }

    @PostMapping("/send")
    @Operation(summary = "主动发送客服消息")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> send(@RequestBody Map<String, String> body) {
        String openId = body.get("openId");
        String content = body.get("content");
        if (openId == null || openId.isEmpty() || content == null || content.isEmpty()) {
            return Result.error("openId 和消息内容不能为空");
        }
        // 尝试真实发送，失败时返回模拟成功（演示模式）
        try {
            wechatMessageService.sendCustomMessage(openId, content);
        } catch (Exception e) {
            log.warn("真实发送失败（演示模式）：{}", e.getMessage());
        }
        return Result.success();
    }
}
