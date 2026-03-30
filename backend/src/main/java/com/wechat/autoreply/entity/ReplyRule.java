package com.wechat.autoreply.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自动回复规则实体
 */
@Data
@TableName("reply_rule")
public class ReplyRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型：KEYWORD-关键词，SUBSCRIBE-关注，DEFAULT-默认，MESSAGE_TYPE-消息类型
     */
    private String ruleType;

    /**
     * 优先级：数值越大优先级越高
     */
    private Integer priority;

    /**
     * 匹配模式：EXACT-完全匹配，CONTAIN-包含，REGEX-正则
     */
    private String matchMode;

    /**
     * 匹配内容 (关键词或正则表达式)
     */
    private String matchContent;

    /**
     * 消息类型：text, image, voice, video, location, link, event
     */
    private String messageType;

    /**
     * 事件类型：subscribe, unsubscribe, SCAN, LOCATION, CLICK, VIEW
     */
    private String eventType;

    /**
     * 回复类型：TEXT-文本，IMAGE-图片，VOICE-语音，VIDEO-视频，MUSIC-音乐，NEWS-图文
     */
    private String replyType;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 素材 ID(用于图片/语音/视频等)
     */
    private String mediaId;

    /**
     * 图文数据 (JSON 格式)
     */
    private String articleData;

    /**
     * 音乐数据 (JSON 格式)
     */
    private String musicData;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Boolean isEnabled;

    /**
     * 命中次数
     */
    private Long hitCount;

    /**
     * 生效日期
     */
    private LocalDateTime effectiveDate;

    /**
     * 过期日期
     */
    private LocalDateTime expireDate;

    /**
     * 创建人
     */
    private Long createBy;

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
