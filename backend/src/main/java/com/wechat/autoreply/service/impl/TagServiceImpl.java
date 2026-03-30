package com.wechat.autoreply.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wechat.autoreply.entity.Tag;
import com.wechat.autoreply.mapper.TagMapper;
import com.wechat.autoreply.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}
