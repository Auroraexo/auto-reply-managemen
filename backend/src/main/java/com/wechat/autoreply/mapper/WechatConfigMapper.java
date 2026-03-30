package com.wechat.autoreply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.autoreply.entity.WechatConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信配置 Mapper
 */
@Mapper
public interface WechatConfigMapper extends BaseMapper<WechatConfig> {
}
