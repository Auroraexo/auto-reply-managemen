package com.wechat.autoreply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.autoreply.entity.MessageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息记录 Mapper
 */
@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {
}
