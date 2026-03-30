package com.wechat.autoreply.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.autoreply.entity.MessageRecord;
import com.wechat.autoreply.entity.ReplyRule;
import com.wechat.autoreply.entity.WechatUser;
import com.wechat.autoreply.mapper.MessageRecordMapper;
import com.wechat.autoreply.mapper.ReplyRuleMapper;
import com.wechat.autoreply.mapper.WechatConfigMapper;
import com.wechat.autoreply.mapper.WechatUserMapper;
import com.wechat.autoreply.service.WechatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 微信消息处理服务实现
 */
@Service
public class WechatMessageServiceImpl implements WechatMessageService {

    private static final Logger log = LoggerFactory.getLogger(WechatMessageServiceImpl.class);

    @Autowired
    private ReplyRuleMapper replyRuleMapper;

    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Autowired
    private WechatUserMapper wechatUserMapper;

    @Autowired
    private WechatConfigMapper wechatConfigMapper;

    @Value("${wechat.official-account.app-id}")
    private String appId;

    @Value("${wechat.official-account.secret}")
    private String secret;

    @Value("${wechat.official-account.token}")
    private String token;

    @Value("${wechat.official-account.access-token-url}")
    private String accessTokenUrl;

    @Value("${wechat.official-account.message-url}")
    private String messageUrl;

    @Value("${wechat.official-account.user-info-url}")
    private String userInfoUrl;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String verifyServer(String signature, String timestamp, String nonce, String echostr) {
        log.info("验证微信服务器 - signature: {}, timestamp: {}, nonce: {}", signature, timestamp, nonce);
        
        // 将 token、timestamp、nonce 三个参数进行字典序排序
        List<String> list = Arrays.asList(token, timestamp, nonce);
        Collections.sort(list);
        
        // 拼接字符串并 SHA1 加密
        String sha1 = DigestUtil.sha1Hex(String.join("", list));
        
        // 验证签名
        if (sha1.equals(signature)) {
            log.info("微信服务器验证成功");
            return echostr;
        } else {
            log.error("微信服务器验证失败");
            return null;
        }
    }

    @Override
    public String processMessage(String xmlData) {
        try {
            log.info("接收到微信消息：{}", xmlData);
            
            // 解析 XML
            Map<String, String> messageMap = parseXml(xmlData);
            
            String toUserName = messageMap.get("ToUserName");
            String fromUserName = messageMap.get("FromUserName");
            String msgType = messageMap.get("MsgType");
            String eventType = messageMap.get("Event");
            String eventKey = messageMap.get("EventKey");
            String content = messageMap.get("Content");
            String createTime = messageMap.get("CreateTime");
            
            // 保存消息记录
            MessageRecord messageRecord = buildMessageRecord(messageMap);
            saveMessageRecord(messageRecord);
            
            // 同步用户信息
            syncAndSaveUser(fromUserName);
            
            // 匹配回复规则
            ReplyRule matchedRule = matchReplyRule(msgType, content, eventType, eventKey, fromUserName);
            
            String replyXml = "";
            if (matchedRule != null) {
                // 更新命中次数
                updateHitCount(matchedRule.getId());
                messageRecord.setReplyRuleId(matchedRule.getId());
                messageRecord.setReplyType(matchedRule.getReplyType());
                
                // 构建回复消息
                replyXml = buildReplyMessage(fromUserName, toUserName, matchedRule);
                messageRecord.setReplyContent(matchedRule.getReplyContent());
                messageRecord.setReplyStatus(1);
            } else {
                log.info("未找到匹配的回复规则");
                messageRecord.setReplyStatus(0);
            }
            
            // 更新消息记录
            messageRecordMapper.updateById(messageRecord);
            
            return replyXml;
            
        } catch (Exception e) {
            log.error("处理微信消息异常", e);
            return "";
        }
    }

    @Override
    public void saveMessageRecord(MessageRecord messageRecord) {
        messageRecordMapper.insert(messageRecord);
    }

    @Override
    public void updateOrSaveUser(WechatUser user) {
        LambdaQueryWrapper<WechatUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WechatUser::getOpenId, user.getOpenId());
        
        WechatUser existUser = wechatUserMapper.selectOne(wrapper);
        
        if (existUser != null) {
            user.setId(existUser.getId());
            wechatUserMapper.updateById(user);
        } else {
            wechatUserMapper.insert(user);
        }
    }

    @Override
    public String getAccessToken() {
        // 直接从数据库获取，检查是否过期
        String dbToken = getAccessTokenFromDb();
        if (StringUtils.isNotBlank(dbToken)) {
            return dbToken;
        }
        // 过期或不存在则刷新
        refreshAccessToken();
        return getAccessTokenFromDb();
    }

    @Override
    public boolean sendCustomMessage(String openId, String content) {
        try {
            String accessToken = getAccessToken();
            if (StringUtils.isBlank(accessToken)) {
                log.error("获取 Access Token 失败");
                return false;
            }
            
            String url = messageUrl + accessToken;
            
            Map<String, Object> params = new HashMap<>();
            params.put("touser", openId);
            params.put("msgtype", "text");
            
            Map<String, String> text = new HashMap<>();
            text.put("content", content);
            params.put("text", text);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(params), headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() == 0) {
                log.info("发送客服消息成功 - openId: {}, content: {}", openId, content);
                return true;
            } else {
                log.error("发送客服消息失败 - {}", response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("发送客服消息异常", e);
            return false;
        }
    }

    @Override
    public ReplyRule matchReplyRule(String msgType, String content, String eventType, String eventKey, String openId) {
        // 获取所有启用的规则，按优先级排序
        List<ReplyRule> rules = getAllEnabledRules();
        
        for (ReplyRule rule : rules) {
            if (isMatch(rule, msgType, content, eventType, eventKey, openId)) {
                log.info("匹配到规则 - ruleId: {}, ruleName: {}", rule.getId(), rule.getRuleName());
                return rule;
            }
        }
        
        return null;
    }

    @Override
    public String buildReplyMessage(String toUser, String fromUser, ReplyRule rule) {
        StringBuilder xml = new StringBuilder();
        xml.append("<xml>");
        xml.append("<ToUserName><![CDATA[").append(toUser).append("]]></ToUserName>");
        xml.append("<FromUserName><![CDATA[").append(fromUser).append("]]></FromUserName>");
        xml.append("<CreateTime>").append(System.currentTimeMillis() / 1000).append("</CreateTime>");
        xml.append("<MsgType><![CDATA[").append(rule.getReplyType()).append("]]></MsgType>");
        
        switch (rule.getReplyType()) {
            case "TEXT":
                xml.append("<Content><![CDATA[").append(rule.getReplyContent()).append("]]></Content>");
                break;
                
            case "IMAGE":
                xml.append("<Image><MediaId><![CDATA[").append(rule.getMediaId()).append("]]></MediaId></Image>");
                break;
                
            case "VOICE":
                xml.append("<Voice><MediaId><![CDATA[").append(rule.getMediaId()).append("]]></MediaId></Voice>");
                break;
                
            case "VIDEO":
                xml.append("<Video>");
                xml.append("<MediaId><![CDATA[").append(rule.getMediaId()).append("]]></MediaId>");
                xml.append("<Title><![CDATA[").append(rule.getReplyContent()).append("]]></Title>");
                xml.append("<Description><![CDATA[视频描述]]></Description>");
                xml.append("</Video>");
                break;
                
            case "MUSIC":
                if (StringUtils.isNotBlank(rule.getMusicData())) {
                    xml.append("<Music>");
                    xml.append("<Title><![CDATA[").append(parseJsonField(rule.getMusicData(), "title")).append("]]></Title>");
                    xml.append("<Description><![CDATA[").append(parseJsonField(rule.getMusicData(), "description")).append("]]></Description>");
                    xml.append("<MusicUrl><![CDATA[").append(parseJsonField(rule.getMusicData(), "musicUrl")).append("]]></MusicUrl>");
                    xml.append("<HQMusicUrl><![CDATA[").append(parseJsonField(rule.getMusicData(), "hqMusicUrl")).append("]]></HQMusicUrl>");
                    xml.append("</Music>");
                }
                break;
                
            case "NEWS":
                if (StringUtils.isNotBlank(rule.getArticleData())) {
                    int articleCount = countArticles(rule.getArticleData());
                    xml.append("<ArticleCount>").append(articleCount).append("</ArticleCount>");
                    xml.append("<Articles>");
                    xml.append(parseArticleXml(rule.getArticleData()));
                    xml.append("</Articles>");
                }
                break;
                
            default:
                xml.append("<Content><![CDATA[暂不支持该类型回复]]></Content>");
        }
        
        xml.append("</xml>");
        return xml.toString();
    }

    @Override
    public List<ReplyRule> getAllEnabledRules() {
        LambdaQueryWrapper<ReplyRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReplyRule::getIsEnabled, true);
        wrapper.orderByDesc(ReplyRule::getPriority);
        return replyRuleMapper.selectList(wrapper);
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨 2 点刷新
    public boolean refreshAccessToken() {
        try {
            log.info("开始刷新微信 Access Token");
            
            String url = String.format(accessTokenUrl, appId, secret);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.has("access_token")) {
                String newAccessToken = jsonNode.get("access_token").asText();
                int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 7200;
                
                // 保存到数据库
                saveAccessTokenToDb(newAccessToken, LocalDateTime.now().plusSeconds(expiresIn));
                
                log.info("微信 Access Token 刷新成功");
                return true;
            } else {
                log.error("刷新微信 Access Token 失败 - {}", response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("刷新微信 Access Token 异常", e);
            return false;
        }
    }

    @Override
    public WechatUser syncWechatUser(String openId) {
        try {
            String accessToken = getAccessToken();
            String url = String.format(userInfoUrl, accessToken, openId);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            if (jsonNode.has("openid")) {
                WechatUser user = new WechatUser();
                user.setOpenId(jsonNode.get("openid").asText());
                user.setUnionId(jsonNode.has("unionid") ? jsonNode.get("unionid").asText() : null);
                user.setNickname(jsonNode.has("nickname") ? jsonNode.get("nickname").asText() : null);
                user.setGender(jsonNode.has("sex") ? jsonNode.get("sex").asInt() : 0);
                user.setCountry(jsonNode.has("country") ? jsonNode.get("country").asText() : null);
                user.setProvince(jsonNode.has("province") ? jsonNode.get("province").asText() : null);
                user.setCity(jsonNode.has("city") ? jsonNode.get("city").asText() : null);
                user.setLanguage(jsonNode.has("language") ? jsonNode.get("language").asText() : null);
                user.setHeadImgUrl(jsonNode.has("headimgurl") ? jsonNode.get("headimgurl").asText() : null);
                
                if (jsonNode.has("subscribe_time")) {
                    user.setSubscribeTime(LocalDateTime.ofEpochSecond(jsonNode.get("subscribe_time").asLong(), 0, java.time.ZoneOffset.UTC));
                }
                
                user.setSubscribe(jsonNode.has("subscribe") && jsonNode.get("subscribe").asInt() == 1 ? 1 : 0);
                
                updateOrSaveUser(user);
                return user;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("同步微信用户信息异常", e);
            return null;
        }
    }

    @Override
    public Map<String, String> parseXml(String xmlData) {
        Map<String, String> map = new HashMap<>();
        try {
            // 简单的 XML 解析实现
            String[] tags = {"ToUserName", "FromUserName", "CreateTime", "MsgType", "Content", 
                           "MsgId", "PicUrl", "MediaId", "Format", "ThumbMediaId",
                           "Location_X", "Location_Y", "Scale", "Label", "Title", 
                           "Description", "Url", "Event", "EventKey", "Ticket"};
            
            for (String tag : tags) {
                String startTag = "<" + tag + ">";
                String endTag = "</" + tag + ">";
                int startIndex = xmlData.indexOf(startTag);
                int endIndex = xmlData.indexOf(endTag);
                
                if (startIndex != -1 && endIndex != -1) {
                    String value = xmlData.substring(startIndex + startTag.length(), endIndex);
                    map.put(tag, value);
                }
            }
            
            // 处理 CDATA 格式
            for (String key : map.keySet()) {
                String value = map.get(key);
                if (value != null && value.startsWith("<![CDATA[") && value.endsWith("]]>")) {
                    map.put(key, value.substring(9, value.length() - 3));
                }
            }
            
        } catch (Exception e) {
            log.error("解析 XML 异常", e);
        }
        return map;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 判断规则是否匹配
     */
    private boolean isMatch(ReplyRule rule, String msgType, String content, 
                          String eventType, String eventKey, String openId) {
        
        // 检查规则类型
        switch (rule.getRuleType()) {
            case "SUBSCRIBE":
                return "event".equals(msgType) && "subscribe".equals(eventType);
                
            case "DEFAULT":
                return true;
                
            case "MESSAGE_TYPE":
                return rule.getMessageType() != null && rule.getMessageType().equals(msgType);
                
            case "KEYWORD":
                if (!"text".equals(msgType)) {
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
        if (StringUtils.isBlank(content) || StringUtils.isBlank(rule.getMatchContent())) {
            return false;
        }
        
        switch (rule.getMatchMode()) {
            case "EXACT":
                return rule.getMatchContent().equals(content);
                
            case "CONTAIN":
                return content.contains(rule.getMatchContent());
                
            case "REGEX":
                try {
                    return Pattern.matches(rule.getMatchContent(), content);
                } catch (Exception e) {
                    log.error("正则表达式匹配异常", e);
                    return false;
                }
                
            default:
                return false;
        }
    }

    /**
     * 构建消息记录
     */
    private MessageRecord buildMessageRecord(Map<String, String> messageMap) {
        MessageRecord record = new MessageRecord();
        record.setOpenId(messageMap.get("FromUserName"));
        record.setMsgType(messageMap.get("MsgType"));
        
        if ("text".equals(messageMap.get("MsgType"))) {
            record.setContent(messageMap.get("Content"));
        } else if ("image".equals(messageMap.get("MsgType"))) {
            record.setImageUrl(messageMap.get("PicUrl"));
            record.setMediaId(messageMap.get("MediaId"));
        } else if ("voice".equals(messageMap.get("MsgType"))) {
            record.setVoiceUrl(messageMap.get("MediaId"));
            record.setVoiceFormat(messageMap.get("Format"));
        } else if ("video".equals(messageMap.get("MsgType"))) {
            record.setVideoUrl(messageMap.get("MediaId"));
        } else if ("location".equals(messageMap.get("MsgType"))) {
            record.setLocationX(messageMap.get("Location_X"));
            record.setLocationY(messageMap.get("Location_Y"));
            record.setScale(Integer.parseInt(messageMap.getOrDefault("Scale", "0")));
            record.setLabel(messageMap.get("Label"));
        } else if ("link".equals(messageMap.get("MsgType"))) {
            record.setTitle(messageMap.get("Title"));
            record.setDescription(messageMap.get("Description"));
            record.setUrl(messageMap.get("Url"));
        } else if ("event".equals(messageMap.get("MsgType"))) {
            record.setEventType(messageMap.get("Event"));
            record.setEventKey(messageMap.get("EventKey"));
        }
        
        if (messageMap.containsKey("MsgId")) {
            record.setMsgId(Long.parseLong(messageMap.get("MsgId")));
        }
        
        return record;
    }

    /**
     * 同步并保存用户信息
     */
    private void syncAndSaveUser(String openId) {
        try {
            LambdaQueryWrapper<WechatUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WechatUser::getOpenId, openId);
            WechatUser existUser = wechatUserMapper.selectOne(wrapper);
            
            if (existUser == null) {
                // 新用户，同步信息
                syncWechatUser(openId);
            } else {
                // 更新最后互动时间和互动次数
                existUser.setLastInteractionTime(LocalDateTime.now());
                existUser.setInteractionCount(existUser.getInteractionCount() + 1);
                wechatUserMapper.updateById(existUser);
            }
            
        } catch (Exception e) {
            log.error("同步用户信息异常", e);
        }
    }

    /**
     * 更新命中次数 (使用 SQL 直接更新避免并发问题)
     */
    private void updateHitCount(Long ruleId) {
        try {
            // 使用 SQL 直接自增，避免先查询后更新导致的锁竞争
            replyRuleMapper.updateHitCount(ruleId);
        } catch (Exception e) {
            log.error("更新命中次数失败 - ruleId: {}", ruleId, e);
        }
    }

    /**
     * 从数据库获取 Access Token
     */
    private String getAccessTokenFromDb() {
        // 这里简化处理，实际应该查询默认的公众号配置
        return null;
    }

    /**
     * 保存 Access Token 到数据库
     */
    private void saveAccessTokenToDb(String accessToken, LocalDateTime expiresTime) {
        // 这里简化处理，实际应该更新默认的公众号配置
    }

    /**
     * 解析 JSON 字段
     */
    private String parseJsonField(String jsonData, String field) {
        try {
            JsonNode node = objectMapper.readTree(jsonData);
            return node.has(field) ? node.get(field).asText() : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 统计图文数量
     */
    private int countArticles(String articleData) {
        try {
            JsonNode node = objectMapper.readTree(articleData);
            return node.isArray() ? node.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 解析图文 XML
     */
    private String parseArticleXml(String articleData) {
        try {
            JsonNode articles = objectMapper.readTree(articleData);
            StringBuilder xml = new StringBuilder();
            
            if (articles.isArray()) {
                for (JsonNode article : articles) {
                    xml.append("<item>");
                    xml.append("<Title><![CDATA[").append(
                        article.has("title") ? article.get("title").asText() : "").append("]]></Title>");
                    xml.append("<Description><![CDATA[").append(
                        article.has("description") ? article.get("description").asText() : "").append("]]></Description>");
                    xml.append("<PicUrl><![CDATA[").append(
                        article.has("picUrl") ? article.get("picUrl").asText() : "").append("]]></PicUrl>");
                    xml.append("<Url><![CDATA[").append(
                        article.has("url") ? article.get("url").asText() : "").append("]]></Url>");
                    xml.append("</item>");
                }
            }
            
            return xml.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
