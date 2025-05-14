package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-24 13:58
 */
public interface MessageMapper {
    int getConversationCount(int userId);

    List<Message> getConversations(int userId, int offset, int limit);

    int getLetterUnreadCount(int userId, String conversationId);

    int getLetterCount(String conversationId);

    int getNoticeUnreadCount(int userId, int status, String topic);

    void insertMessage(Message message);

    List<Message> getLetterDetail(String conversationId, int offset, int limit);

    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

    int getNoticeCount();

    List<Message> getDetailConversation(int userId, int offset, int limit);

    Message getLastedNotice(int userId, String topic);

    int selectNoticeCount(int userId, String topic);

    List<Message> getNoticeDetail(int userId, String topic, int offset, int limit);
}
