package com.wechat.autoreply.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信用户实体
 */
@Data
@TableName("wechat_user")
public class WechatUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 OpenID
     */
    private String openId;

    /**
     * 用户 UnionID
     */
    private String unionId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 语言
     */
    private String language;

    /**
     * 头像 URL
     */
    private String headImgUrl;

    /**
     * 关注时间
     */
    private LocalDateTime subscribeTime;

    /**
     * 是否关注：0-未关注，1-已关注
     */
    private Integer subscribe;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标签 ID 列表 (逗号分隔)
     */
    private String tagIds;

    /**
     * 最后互动时间
     */
    private LocalDateTime lastInteractionTime;

    /**
     * 互动次数
     */
    private Integer interactionCount;

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
