package com.wechat.autoreply.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息记录实体
 */
@Data
@TableName("message_record")
public class MessageRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 记录 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 微信消息 ID
     */
    private Long msgId;

    /**
     * 用户 OpenID
     */
    private String openId;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 图片链接
     */
    private String imageUrl;

    /**
     * 媒体素材 ID(用于图片/语音/视频)
     */
    private String mediaId;

    /**
     * 语音链接
     */
    private String voiceUrl;

    /**
     * 语音格式
     */
    private String voiceFormat;

    /**
     * 视频链接
     */
    private String videoUrl;

    /**
     * 地理位置纬度
     */
    private String locationX;

    /**
     * 地理位置经度
     */
    private String locationY;

    /**
     * 地图缩放大小
     */
    private Integer scale;

    /**
     * 地理位置信息
     */
    private String label;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息描述
     */
    private String description;

    /**
     * 消息链接
     */
    private String url;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件 Key
     */
    private String eventKey;

    /**
     * 匹配的回复规则 ID
     */
    private Long replyRuleId;

    /**
     * 回复内容
     */
    private String replyContent;

    /**
     * 回复类型
     */
    private String replyType;

    /**
     * 回复状态：0-失败，1-成功
     */
    private Integer replyStatus;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
