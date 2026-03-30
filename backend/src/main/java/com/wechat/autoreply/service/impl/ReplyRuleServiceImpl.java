package com.wechat.autoreply.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wechat.autoreply.dto.ReplyRuleDTO;
import com.wechat.autoreply.entity.ReplyRule;
import com.wechat.autoreply.mapper.ReplyRuleMapper;
import com.wechat.autoreply.service.ReplyRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 回复规则管理服务实现
 */
@Slf4j
@Service
public class ReplyRuleServiceImpl implements ReplyRuleService {

    @Resource
    private ReplyRuleMapper replyRuleMapper;

    @Override
    public Page<ReplyRuleDTO> getPageRules(int page, int size, String ruleName, String ruleType, Boolean isEnabled) {
        Page<ReplyRule> replyRulePage = new Page<>(page, size);
        
        LambdaQueryWrapper<ReplyRule> wrapper = new LambdaQueryWrapper<>();
        if (ruleName != null && !ruleName.isEmpty()) {
            wrapper.like(ReplyRule::getRuleName, ruleName);
        }
        if (ruleType != null && !ruleType.isEmpty()) {
            wrapper.eq(ReplyRule::getRuleType, ruleType);
        }
        if (isEnabled != null) {
            wrapper.eq(ReplyRule::getIsEnabled, isEnabled);
        }
        
        wrapper.orderByDesc(ReplyRule::getPriority)
               .orderByDesc(ReplyRule::getCreateTime);
        
        Page<ReplyRule> resultPage = replyRuleMapper.selectPage(replyRulePage, wrapper);
        
        // 转换为 DTO
        List<ReplyRuleDTO> dtoList = resultPage.getRecords().stream()
                .map(rule -> BeanUtil.copyProperties(rule, ReplyRuleDTO.class))
                .collect(Collectors.toList());
        
        Page<ReplyRuleDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(resultPage.getTotal());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public ReplyRuleDTO getRuleById(Long id) {
        ReplyRule rule = replyRuleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }
        return BeanUtil.copyProperties(rule, ReplyRuleDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReplyRuleDTO createRule(ReplyRuleDTO ruleDTO) {
        ReplyRule rule = BeanUtil.copyProperties(ruleDTO, ReplyRule.class);
        rule.setHitCount(0L);
        rule.setCreateTime(LocalDateTime.now());
        
        replyRuleMapper.insert(rule);
        log.info("创建回复规则成功 - id: {}, name: {}", rule.getId(), rule.getRuleName());
        
        return BeanUtil.copyProperties(rule, ReplyRuleDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReplyRuleDTO updateRule(Long id, ReplyRuleDTO ruleDTO) {
        ReplyRule existRule = replyRuleMapper.selectById(id);
        if (existRule == null) {
            throw new RuntimeException("规则不存在");
        }
        
        BeanUtil.copyProperties(ruleDTO, existRule, "id", "hitCount", "createTime");
        existRule.setUpdateTime(LocalDateTime.now());
        
        replyRuleMapper.updateById(existRule);
        log.info("更新回复规则成功 - id: {}, name: {}", existRule.getId(), existRule.getRuleName());
        
        return BeanUtil.copyProperties(existRule, ReplyRuleDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        replyRuleMapper.deleteById(id);
        log.info("删除回复规则成功 - id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRules(List<Long> ids) {
        ids.forEach(this::deleteRule);
        log.info("批量删除回复规则成功 - ids: {}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setRuleEnabled(Long id, boolean enabled) {
        ReplyRule rule = replyRuleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }
        
        rule.setIsEnabled(enabled);
        replyRuleMapper.updateById(rule);
        log.info("{}回复规则 - id: {}", enabled ? "启用" : "禁用", id);
    }

    @Override
    public List<ReplyRuleDTO> getAllRules() {
        LambdaQueryWrapper<ReplyRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ReplyRule::getPriority);
        List<ReplyRule> rules = replyRuleMapper.selectList(wrapper);
        
        return rules.stream()
                .map(rule -> BeanUtil.copyProperties(rule, ReplyRuleDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ReplyRuleDTO testMatch(String msgType, String content, String eventType) {
        List<ReplyRule> rules = getAllRules().stream()
                .filter(ReplyRuleDTO::getIsEnabled)
                .map(dto -> BeanUtil.copyProperties(dto, ReplyRule.class))
                .collect(Collectors.toList());
        
        for (ReplyRule rule : rules) {
            if (isMatch(rule, msgType, content, eventType)) {
                return BeanUtil.copyProperties(rule, ReplyRuleDTO.class);
            }
        }
        
        return null;
    }

    /**
     * 判断规则是否匹配
     */
    private boolean isMatch(ReplyRule rule, String msgType, String content, String eventType) {
        switch (rule.getRuleType()) {
            case "SUBSCRIBE":
                return "event".equals(msgType) && "subscribe".equals(eventType);
                
            case "DEFAULT":
                return true;
                
            case "MESSAGE_TYPE":
                return rule.getMessageType() != null && rule.getMessageType().equals(msgType);
                
            case "KEYWORD":
                if (!"text".equals(msgType) || content == null) {
                    return false;
                }
                return matchKeyword(rule, content);
                
            default:
                return false;
        }
    }

    /**
     * 关键词匹配
     */
    private boolean matchKeyword(ReplyRule rule, String content) {
        if (content == null || rule.getMatchContent() == null) {
            return false;
        }
        
        switch (rule.getMatchMode()) {
            case "EXACT":
                return rule.getMatchContent().equals(content);
                
            case "CONTAIN":
                return content.contains(rule.getMatchContent());
                
            case "REGEX":
                try {
                    return content.matches(rule.getMatchContent());
                } catch (Exception e) {
                    log.error("正则表达式匹配异常", e);
                    return false;
                }
                
            default:
                return false;
        }
    }
}
