package com.nowcoder.community.service.Impl;

import com.nowcoder.community.entity.Message;
import com.nowcoder.community.mapper.MessageMapper;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-24 13:53
 */
@Service
public class MessageServiceImpl implements MessageService, CommunityConstant {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;
    @Override
    public int getConversationCount(int userId) {
        return messageMapper.getConversationCount(userId);
    }

    @Override
    public List<Message> getConversations(int userId, int offset, int limit) {
        return messageMapper.getConversations(userId, offset, limit);
    }

    @Override
    public int getLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.getLetterUnreadCount(userId, conversationId);
    }

    @Override
    public int getLetterCount(String conversationId) {
        return messageMapper.getLetterCount(conversationId);
    }

    @Override
    public int getNoticeUnreadCount(int userId, int status, String topic) {
        return messageMapper.getNoticeUnreadCount(userId, status, topic);
    }

    @Override
    public void addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        messageMapper.insertMessage(message);
    }

    @Override
    public List<Message> getLetterDetail(String conversationId, int offset, int limit) {
        return messageMapper.getLetterDetail(conversationId,  offset, limit);
    }

    @Override
    public int readMessages(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    @Override
    public int getNoticeCount() {
        return messageMapper.getNoticeCount();
    }

    @Override
    public List<Message> getDetailConversation(int userId, int offset, int limit) {
        return messageMapper.getDetailConversation(userId, offset, limit);
    }

    @Override
    public Message getLastedNotice(int userId, String topic) {
        return messageMapper.getLastedNotice(userId, topic);
    }

    @Override
    public int getNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    @Override
    public List<Message> getNoticeDetail(int userId, String topic, int offset, int limit) {
        return messageMapper.getNoticeDetail(userId, topic, offset, limit);
    }
}
