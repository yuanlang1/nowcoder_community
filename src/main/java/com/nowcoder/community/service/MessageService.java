package com.nowcoder.community.service;

import com.nowcoder.community.entity.Message;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-24 13:53
 */
public interface MessageService {
    int getConversationCount(int id);

    List<Message> getConversations(int id, int offset, int limit);

    int getLetterUnreadCount(int id, String conversationId);

    int getLetterCount(String conversationId);

    int getNoticeUnreadCount(int id, int status, String topic);

    void addMessage(Message message);

    List<Message> getLetterDetail(String conversationId, int offset, int limit);

    int readMessages(List<Integer> ids);

    int getNoticeCount();

    List<Message> getDetailConversation(int userId, int offset, int limit);

    Message getLastedNotice(int UserId, String topic);

    int getNoticeCount(int userId, String topic);

    List<Message> getNoticeDetail(int userId, String topic, int offset, int limit);
}
