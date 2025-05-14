package com.nowcoder.community.service;

import com.nowcoder.community.entity.Comment;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-23 21:17
 */
public interface CommentService {
    int addComment(Comment comment);

    List<Comment> findCommentEntity(int entityTypePost, int id, int offset, int limit);

    int findCommentCount(int entityTypeComment, int id);

    Comment getCommentById(int id);
}
