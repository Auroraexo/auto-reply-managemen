package com.wechat.autoreply.controller;

import com.wechat.autoreply.service.WechatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 微信消息接收控制器
 */
@RestController
@RequestMapping("/wechat")
@Tag(name = "微信消息接口", description = "微信公众号消息接收与处理")
public class WechatMessageController {

    private static final Logger log = LoggerFactory.getLogger(WechatMessageController.class);

    @Resource
    private WechatMessageService wechatMessageService;

    /**
     * 微信服务器验证（GET 请求）
     */
    @GetMapping
    @Operation(summary = "微信服务器验证")
    public String verifyWechatServer(
            @Parameter(description = "签名") @RequestParam String signature,
            @Parameter(description = "时间戳") @RequestParam String timestamp,
            @Parameter(description = "随机数") @RequestParam String nonce,
            @Parameter(description = "随机字符串") @RequestParam String echostr) {
        
        return wechatMessageService.verifyServer(signature, timestamp, nonce, echostr);
    }

    /**
     * 接收微信消息 (POST 请求)
     */
    @PostMapping
    @Operation(summary = "接收微信消息")
    public String receiveWechatMessage(
            HttpServletRequest request,
            @Parameter(description = "签名") @RequestParam(required = false) String signature,
            @Parameter(description = "时间戳") @RequestParam(required = false) String timestamp,
            @Parameter(description = "随机数") @RequestParam(required = false) String nonce,
            @Parameter(description = "随机字符串") @RequestParam(required = false) String echostr) {
        
        // 如果是验证请求，先验证
        if (signature != null && timestamp != null && nonce != null && echostr != null) {
            String result = wechatMessageService.verifyServer(signature, timestamp, nonce, echostr);
            if (result != null) {
                return result;
            }
        }
        
        try {
            // 读取请求体中的 XML 数据
            String xmlData = readRequestBody(request);
            log.info("接收到微信消息：{}", xmlData);
            
            // 处理消息并返回回复
            return wechatMessageService.processMessage(xmlData);
            
        } catch (IOException e) {
            log.error("读取微信消息异常", e);
            return "";
        }
    }

    /**
     * 读取请求体
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
