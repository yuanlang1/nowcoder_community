package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-23 21:18
 */
@Mapper
public interface CommentMapper {
     List<Comment> findCommentEntity(int entityType, int entityId, int offset, int limit);

    int insertComment(Comment comment);

    int selectCountEntity(int entityType, int entityId);

    int getCommentCount(int entityType, int entityId);

    Comment getCommentById(int id);
}
