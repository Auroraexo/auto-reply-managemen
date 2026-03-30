package com.wechat.autoreply.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wechat.autoreply.entity.WechatUser;
import com.wechat.autoreply.mapper.WechatUserMapper;
import com.wechat.autoreply.service.WechatUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信用户服务实现
 */
@Slf4j
@Service
public class WechatUserServiceImpl extends ServiceImpl<WechatUserMapper, WechatUser> implements WechatUserService {

    @Override
    public void batchAddTags(List<Long> userIds, Long tagId) {
        for (Long userId : userIds) {
            WechatUser user = getById(userId);
            if (user != null) {
                String tagIds = user.getTagIds();
                if (tagIds == null || tagIds.isEmpty()) {
                    tagIds = tagId.toString();
                } else if (!tagIds.contains(tagId.toString())) {
                    tagIds = tagIds + "," + tagId;
                }
                user.setTagIds(tagIds);
                updateById(user);
            }
        }
    }

    @Override
    public void removeTag(Long userId, Long tagId) {
        WechatUser user = getById(userId);
        if (user != null && user.getTagIds() != null) {
            List<String> tagIdList = Arrays.asList(user.getTagIds().split(","));
            String newTagIds = tagIdList.stream()
                    .filter(id -> !id.equals(tagId.toString()))
                    .collect(Collectors.joining(","));
            user.setTagIds(newTagIds);
            updateById(user);
        }
    }

    @Override
    public WechatUser getByOpenId(String openId) {
        LambdaQueryWrapper<WechatUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatUser::getOpenId, openId);
        return getOne(wrapper);
    }
}
