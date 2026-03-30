package com.wechat.autoreply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.autoreply.entity.WechatUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信用户 Mapper
 */
@Mapper
public interface WechatUserMapper extends BaseMapper<WechatUser> {
}
