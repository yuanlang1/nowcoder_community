package com.nowcoder.community.service.Impl;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.mapper.DiscussPostMapper;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-23 21:17
 */
@Service
public class CommentServiceImpl implements CommentService, CommunityConstant {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Override
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));

        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        // 判断评论的对象是否为帖子
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    @Override
    public List<Comment> findCommentEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.findCommentEntity(entityType, entityId, offset, limit);

    }

    @Override
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.getCommentCount(entityType, entityId);
    }

    @Override
    public Comment getCommentById(int id) {
        return commentMapper.getCommentById(id);
    }
}
