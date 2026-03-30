-- 微信公众号自动回复管理系统数据库设计
-- Database: wechat_auto_reply

CREATE DATABASE IF NOT EXISTS wechat_auto_reply DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE wechat_auto_reply;

-- 1. 用户表（后台管理系统管理员）
CREATE TABLE `sys_user` (
    `id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码 (加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像 URL',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN, USER',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 2. 微信公众号配置表
CREATE TABLE `wechat_config` (
    `id` BIGINT(20) NOT NULL COMMENT '配置 ID',
    `app_id` VARCHAR(100) NOT NULL COMMENT '公众号 AppID',
    `secret` VARCHAR(255) NOT NULL COMMENT '公众号 Secret',
    `token` VARCHAR(255) NOT NULL COMMENT '微信服务器 Token',
    `aes_key` VARCHAR(255) DEFAULT NULL COMMENT '消息加解密密钥',
    `access_token` VARCHAR(255) DEFAULT NULL COMMENT '微信接口调用凭证',
    `access_token_expires` DATETIME DEFAULT NULL COMMENT 'Access Token 过期时间',
    `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否为默认配置：0-否，1-是',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信公众号配置表';

-- 3. 自动回复规则表
CREATE TABLE `reply_rule` (
    `id` BIGINT(20) NOT NULL COMMENT '规则 ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(20) NOT NULL COMMENT '规则类型：KEYWORD-关键词，SUBSCRIBE-关注，DEFAULT-默认，MESSAGE_TYPE-消息类型',
    `priority` INT(11) DEFAULT 0 COMMENT '优先级：数值越大优先级越高',
    `match_mode` VARCHAR(20) DEFAULT 'EXACT' COMMENT '匹配模式：EXACT-完全匹配，CONTAIN-包含，REGEX-正则',
    `match_content` VARCHAR(500) DEFAULT NULL COMMENT '匹配内容 (关键词或正则表达式)',
    `message_type` VARCHAR(20) DEFAULT NULL COMMENT '消息类型：text, image, voice, video, location, link, event',
    `event_type` VARCHAR(50) DEFAULT NULL COMMENT '事件类型：subscribe, unsubscribe, SCAN, LOCATION, CLICK, VIEW',
    `reply_type` VARCHAR(20) DEFAULT 'TEXT' COMMENT '回复类型：TEXT-文本，IMAGE-图片，VOICE-语音，VIDEO-视频，MUSIC-音乐，NEWS-图文',
    `reply_content` TEXT COMMENT '回复内容',
    `media_id` VARCHAR(255) DEFAULT NULL COMMENT '素材 ID(用于图片/语音/视频等)',
    `article_data` JSON DEFAULT NULL COMMENT '图文数据 (JSON 格式)',
    `music_data` JSON DEFAULT NULL COMMENT '音乐数据 (JSON 格式)',
    `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `hit_count` BIGINT(20) DEFAULT 0 COMMENT '命中次数',
    `effective_date` DATE DEFAULT NULL COMMENT '生效日期',
    `expire_date` DATE DEFAULT NULL COMMENT '过期日期',
    `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_rule_type` (`rule_type`),
    INDEX `idx_match_mode` (`match_mode`),
    INDEX `idx_is_enabled` (`is_enabled`),
    INDEX `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自动回复规则表';

-- 4. 消息记录表
CREATE TABLE `message_record` (
    `id` BIGINT(20) NOT NULL COMMENT '记录 ID',
    `msg_id` BIGINT(20) DEFAULT NULL COMMENT '微信消息 ID',
    `open_id` VARCHAR(100) NOT NULL COMMENT '用户 OpenID',
    `msg_type` VARCHAR(20) NOT NULL COMMENT '消息类型',
    `content` TEXT COMMENT '消息内容',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '图片链接',
    `voice_url` VARCHAR(500) DEFAULT NULL COMMENT '语音链接',
    `voice_format` VARCHAR(20) DEFAULT NULL COMMENT '语音格式',
    `video_url` VARCHAR(500) DEFAULT NULL COMMENT '视频链接',
    `location_x` DECIMAL(10, 8) DEFAULT NULL COMMENT '地理位置纬度',
    `location_y` DECIMAL(11, 8) DEFAULT NULL COMMENT '地理位置经度',
    `scale` INT(11) DEFAULT NULL COMMENT '地图缩放大小',
    `label` VARCHAR(200) DEFAULT NULL COMMENT '地理位置信息',
    `title` VARCHAR(200) DEFAULT NULL COMMENT '消息标题',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '消息描述',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '消息链接',
    `event_type` VARCHAR(50) DEFAULT NULL COMMENT '事件类型',
    `event_key` VARCHAR(255) DEFAULT NULL COMMENT '事件 Key',
    `reply_rule_id` BIGINT(20) DEFAULT NULL COMMENT '匹配的回复规则 ID',
    `reply_content` TEXT COMMENT '回复内容',
    `reply_type` VARCHAR(20) DEFAULT NULL COMMENT '回复类型',
    `reply_status` TINYINT(1) DEFAULT 0 COMMENT '回复状态：0-失败，1-成功',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_open_id` (`open_id`),
    INDEX `idx_msg_type` (`msg_type`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_reply_rule_id` (`reply_rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息记录表';

-- 5. 用户表（微信粉丝）
CREATE TABLE `wechat_user` (
    `id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `open_id` VARCHAR(100) NOT NULL COMMENT '用户 OpenID',
    `union_id` VARCHAR(100) DEFAULT NULL COMMENT '用户 UnionID',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `country` VARCHAR(50) DEFAULT NULL COMMENT '国家',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
    `language` VARCHAR(20) DEFAULT NULL COMMENT '语言',
    `head_img_url` VARCHAR(500) DEFAULT NULL COMMENT '头像 URL',
    `subscribe_time` DATETIME DEFAULT NULL COMMENT '关注时间',
    `subscribe` TINYINT(1) DEFAULT 0 COMMENT '是否关注：0-未关注，1-已关注',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `tag_ids` VARCHAR(500) DEFAULT NULL COMMENT '标签 ID 列表 (逗号分隔)',
    `last_interaction_time` DATETIME DEFAULT NULL COMMENT '最后互动时间',
    `interaction_count` INT(11) DEFAULT 0 COMMENT '互动次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_open_id` (`open_id`),
    INDEX `idx_subscribe` (`subscribe`),
    INDEX `idx_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信用户表';

-- 6. 素材管理表
CREATE TABLE `material` (
    `id` BIGINT(20) NOT NULL COMMENT '素材 ID',
    `media_id` VARCHAR(255) NOT NULL COMMENT '微信返回的素材 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '素材名称',
    `type` VARCHAR(20) NOT NULL COMMENT '素材类型：image, voice, video, thumb',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '素材 URL',
    `file_size` BIGINT(20) DEFAULT NULL COMMENT '文件大小 (字节)',
    `file_format` VARCHAR(20) DEFAULT NULL COMMENT '文件格式',
    `duration` INT(11) DEFAULT NULL COMMENT '时长 (秒)',
    `is_permanent` TINYINT(1) DEFAULT 0 COMMENT '是否永久素材：0-临时，1-永久',
    `create_by` BIGINT(20) DEFAULT NULL COMMENT '上传人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_media_id` (`media_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_is_permanent` (`is_permanent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='素材管理表';

-- 7. 操作日志表
CREATE TABLE `operation_log` (
    `id` BIGINT(20) NOT NULL COMMENT '日志 ID',
    `user_id` BIGINT(20) DEFAULT NULL COMMENT '操作用户 ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作用户名',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作描述',
    `module` VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
    `method` VARCHAR(100) DEFAULT NULL COMMENT '请求方法',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '请求 URL',
    `params` TEXT COMMENT '请求参数',
    `result` TEXT COMMENT '操作结果',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP 地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
    `execute_time` BIGINT(20) DEFAULT NULL COMMENT '执行时长 (毫秒)',
    `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation` (`operation`),
    INDEX `idx_module` (`module`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 8. 标签表
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id` BIGINT(20) NOT NULL COMMENT '标签 ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '标签描述',
    `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序顺序',
    `create_by` BIGINT(20) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 9. 数据统计表
DROP TABLE IF EXISTS `stats_record`;
CREATE TABLE `stats_record` (
    `id` BIGINT(20) NOT NULL COMMENT '统计 ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `stat_type` VARCHAR(20) DEFAULT 'daily' COMMENT '统计类型：daily, weekly, monthly',
    `new_user_count` INT(11) DEFAULT 0 COMMENT '新增用户数',
    `lost_user_count` INT(11) DEFAULT 0 COMMENT '流失用户数',
    `total_user_count` INT(11) DEFAULT 0 COMMENT '总用户数',
    `message_count` INT(11) DEFAULT 0 COMMENT '消息发送量',
    `reply_count` INT(11) DEFAULT 0 COMMENT '回复触发量',
    `rule_hit_counts` JSON DEFAULT NULL COMMENT '规则命中统计 (JSON)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_type` (`stat_date`, `stat_type`),
    INDEX `idx_stat_date` (`stat_date`),
    INDEX `idx_stat_type` (`stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据统计表';

-- 插入默认数据
-- 默认运营者账户 (密码：admin123, BCrypt 加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `role`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJd1X6YzKJvQlPJ9FKbxqQO7jKi', '系统运营者', 'OPERATOR', 1);

-- 默认公众号配置示例
INSERT INTO `wechat_config` (`id`, `app_id`, `secret`, `token`, `is_default`, `remark`) VALUES
(1, 'your_app_id', 'your_secret', 'your_token', 1, '默认公众号配置');
