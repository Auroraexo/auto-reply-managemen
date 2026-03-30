package com.wechat.autoreply.service;

import com.wechat.autoreply.entity.MessageRecord;
import com.wechat.autoreply.entity.ReplyRule;
import com.wechat.autoreply.entity.WechatUser;

import java.util.List;
import java.util.Map;

/**
 * 微信消息处理服务接口
 */
public interface WechatMessageService {

    /**
     * 验证微信服务器
     *
     * @param signature 签名
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   随机字符串
     * @return 验证结果
     */
    String verifyServer(String signature, String timestamp, String nonce, String echostr);

    /**
     * 处理微信消息
     *
     * @param xmlData XML 格式的消息数据
     * @return 回复消息的 XML
     */
    String processMessage(String xmlData);

    /**
     * 保存消息记录
     *
     * @param messageRecord 消息记录
     */
    void saveMessageRecord(MessageRecord messageRecord);

    /**
     * 更新或保存微信用户信息
     *
     * @param user 微信用户信息
     */
    void updateOrSaveUser(WechatUser user);

    /**
     * 获取用户的 access token
     *
     * @return access token
     */
    String getAccessToken();

    /**
     * 发送客服消息
     *
     * @param openId  用户 OpenID
     * @param content 消息内容
     * @return 是否成功
     */
    boolean sendCustomMessage(String openId, String content);

    /**
     * 匹配回复规则
     *
     * @param msgType    消息类型
     * @param content    消息内容
     * @param eventType  事件类型
     * @param eventKey   事件 Key
     * @param openId     用户 OpenID
     * @return 匹配的回复规则
     */
    ReplyRule matchReplyRule(String msgType, String content, String eventType, String eventKey, String openId);

    /**
     * 构建回复消息
     *
     * @param toUser   接收者 OpenID
     * @param fromUser 发送者 AppID
     * @param rule     回复规则
     * @return XML 格式的回复消息
     */
    String buildReplyMessage(String toUser, String fromUser, ReplyRule rule);

    /**
     * 获取所有启用的回复规则
     *
     * @return 回复规则列表
     */
    List<ReplyRule> getAllEnabledRules();

    /**
     * 刷新微信 Access Token
     *
     * @return 是否成功
     */
    boolean refreshAccessToken();

    /**
     * 同步微信用户信息
     *
     * @param openId 用户 OpenID
     * @return 用户信息
     */
    WechatUser syncWechatUser(String openId);

    /**
     * 解析 XML 消息
     *
     * @param xmlData XML 数据
     * @return 解析后的 Map
     */
    Map<String, String> parseXml(String xmlData);
}
