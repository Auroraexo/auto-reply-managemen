package com.wechat.autoreply.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回复规则 DTO
 */
@Data
@Schema(description = "回复规则信息")
public class ReplyRuleDTO {

    @Schema(description = "规则 ID")
    private Long id;

    @Schema(description = "规则名称", required = true)
    private String ruleName;

    @Schema(description = "规则类型", required = true, 
            allowableValues = {"KEYWORD", "SUBSCRIBE", "DEFAULT", "MESSAGE_TYPE"})
    private String ruleType;

    @Schema(description = "优先级", example = "0")
    private Integer priority;

    @Schema(description = "匹配模式", example = "EXACT",
            allowableValues = {"EXACT", "CONTAIN", "REGEX"})
    private String matchMode;

    @Schema(description = "匹配内容")
    private String matchContent;

    @Schema(description = "消息类型",
            allowableValues = {"text", "image", "voice", "video", "location", "link", "event"})
    private String messageType;

    @Schema(description = "事件类型",
            allowableValues = {"subscribe", "unsubscribe", "SCAN", "LOCATION", "CLICK", "VIEW"})
    private String eventType;

    @Schema(description = "回复类型", required = true,
            allowableValues = {"TEXT", "IMAGE", "VOICE", "VIDEO", "MUSIC", "NEWS"})
    private String replyType;

    @Schema(description = "回复内容")
    private String replyContent;

    @Schema(description = "素材 ID")
    private String mediaId;

    @Schema(description = "图文数据 (JSON)")
    private String articleData;

    @Schema(description = "音乐数据 (JSON)")
    private String musicData;

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;

    @Schema(description = "命中次数", example = "0")
    private Long hitCount;

    @Schema(description = "生效日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveDate;

    @Schema(description = "过期日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireDate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
