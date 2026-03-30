package com.wechat.autoreply.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wechat.autoreply.entity.WechatUser;

import java.util.List;

public interface WechatUserService extends IService<WechatUser> {

    void batchAddTags(List<Long> userIds, Long tagId);

    void removeTag(Long userId, Long tagId);

    WechatUser getByOpenId(String openId);
}
