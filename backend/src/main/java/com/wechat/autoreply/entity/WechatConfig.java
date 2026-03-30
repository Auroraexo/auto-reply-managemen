package com.wechat.autoreply.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信公众号配置实体
 */
@Data
@TableName("wechat_config")
public class WechatConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 公众号 AppID
     */
    private String appId;

    /**
     * 公众号 Secret
     */
    private String secret;

    /**
     * 微信服务器 Token
     */
    private String token;

    /**
     * 消息加解密密钥
     */
    private String aesKey;

    /**
     * 微信接口调用凭证
     */
    private String accessToken;

    /**
     * Access Token 过期时间
     */
    private LocalDateTime accessTokenExpires;

    /**
     * 是否为默认配置：0-否，1-是
     */
    private Boolean isDefault;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
