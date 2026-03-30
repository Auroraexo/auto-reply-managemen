package com.wechat.autoreply.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.autoreply.entity.MessageRecord;
import com.wechat.autoreply.entity.ReplyRule;
import com.wechat.autoreply.entity.WechatUser;
import com.wechat.autoreply.mapper.MessageRecordMapper;
import com.wechat.autoreply.mapper.ReplyRuleMapper;
import com.wechat.autoreply.mapper.WechatUserMapper;
import com.wechat.autoreply.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private WechatUserMapper wechatUserMapper;

    @Resource
    private MessageRecordMapper messageRecordMapper;

    @Resource
    private ReplyRuleMapper replyRuleMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();

        LambdaQueryWrapper<WechatUser> userCountWrapper = new LambdaQueryWrapper<>();
        userCountWrapper.eq(WechatUser::getSubscribe, 1);
        long totalUsers = wechatUserMapper.selectCount(userCountWrapper);
        overview.put("totalUsers", totalUsers);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<WechatUser> newUserWrapper = new LambdaQueryWrapper<>();
        newUserWrapper.eq(WechatUser::getSubscribe, 1).ge(WechatUser::getSubscribeTime, todayStart);
        long newUsersToday = wechatUserMapper.selectCount(newUserWrapper);
        overview.put("newUsersToday", newUsersToday);

        LambdaQueryWrapper<MessageRecord> messageCountWrapper = new LambdaQueryWrapper<>();
        messageCountWrapper.ge(MessageRecord::getCreateTime, todayStart);
        long messagesToday = messageRecordMapper.selectCount(messageCountWrapper);
        overview.put("messagesToday", messagesToday);

        LambdaQueryWrapper<ReplyRule> activeRuleWrapper = new LambdaQueryWrapper<>();
        activeRuleWrapper.eq(ReplyRule::getIsEnabled, true);
        long activeRules = replyRuleMapper.selectCount(activeRuleWrapper);
        overview.put("activeRules", activeRules);

        return overview;
    }

    @Override
    public Map<String, Object> getUserStats(String range) {
        Map<String, Object> result = new HashMap<>();
        int days = "month".equals(range) ? 30 : 7;
        List<String> dates = new ArrayList<>();
        List<Integer> newUsers = new ArrayList<>();
        List<Integer> lostUsers = new ArrayList<>();
        List<Integer> netGrowth = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            dates.add(LocalDate.now().minusDays(i).format(DATE_FORMATTER));
        }

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            LambdaQueryWrapper<WechatUser> newUserWrapper = new LambdaQueryWrapper<>();
            newUserWrapper.eq(WechatUser::getSubscribe, 1)
                         .between(WechatUser::getSubscribeTime, startOfDay, endOfDay);
            int newCount = Math.toIntExact(wechatUserMapper.selectCount(newUserWrapper));
            int lostCount = 0;
            newUsers.add(newCount);
            lostUsers.add(lostCount);
            netGrowth.add(newCount - lostCount);
        }

        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("lostUsers", lostUsers);
        result.put("netGrowth", netGrowth);
        return result;
    }

    @Override
    public Map<String, Object> getMessageStats(String range) {
        Map<String, Object> result = new HashMap<>();
        int days = "month".equals(range) ? 30 : 7;
        List<String> dates = new ArrayList<>();
        List<Integer> userMessages = new ArrayList<>();
        List<Integer> autoReplies = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            dates.add(LocalDate.now().minusDays(i).format(DATE_FORMATTER));
        }

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            LambdaQueryWrapper<MessageRecord> messageWrapper = new LambdaQueryWrapper<>();
            messageWrapper.between(MessageRecord::getCreateTime, startOfDay, endOfDay);
            int messageCount = Math.toIntExact(messageRecordMapper.selectCount(messageWrapper));

            LambdaQueryWrapper<MessageRecord> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.between(MessageRecord::getCreateTime, startOfDay, endOfDay)
                       .eq(MessageRecord::getReplyStatus, 1);
            int replyCount = Math.toIntExact(messageRecordMapper.selectCount(replyWrapper));

            userMessages.add(messageCount);
            autoReplies.add(replyCount);
        }

        result.put("dates", dates);
        result.put("userMessages", userMessages);
        result.put("autoReplies", autoReplies);
        return result;
    }

    @Override
    public Map<String, Object> getRuleStats() {
        Map<String, Object> result = new HashMap<>();

        LambdaQueryWrapper<ReplyRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(ReplyRule::getIsEnabled, true).orderByDesc(ReplyRule::getHitCount);
        List<ReplyRule> rules = replyRuleMapper.selectList(ruleWrapper);

        List<Map<String, Object>> topRules = rules.stream()
                .limit(10)
                .map(rule -> {
                    Map<String, Object> ruleData = new HashMap<>();
                    ruleData.put("name", rule.getRuleName());
                    ruleData.put("value", rule.getHitCount());
                    return ruleData;
                })
                .collect(Collectors.toList());

        result.put("rules", topRules);
        return result;
    }
}
