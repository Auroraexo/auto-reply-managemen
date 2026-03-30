package com.wechat.autoreply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.autoreply.entity.ReplyRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 回复规则 Mapper
 */
@Mapper
public interface ReplyRuleMapper extends BaseMapper<ReplyRule> {
    
    /**
     * 更新命中次数 (直接 SQL 自增，避免并发锁问题)
     */
    @Update("UPDATE reply_rule SET hit_count = hit_count + 1 WHERE id = #{ruleId}")
    void updateHitCount(@Param("ruleId") Long ruleId);
}
