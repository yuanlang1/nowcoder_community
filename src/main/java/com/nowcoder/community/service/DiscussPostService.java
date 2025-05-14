package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-23 20:25
 */
public interface DiscussPostService {
    int addDiscussPost(DiscussPost discussPost);

    DiscussPost getDiscussPostById(int discussPostId);

    int getDiscussPostRows(int id);

    List<DiscussPost> getDiscussPosts(int i, int offset, int limit);

    void updateType(int id, int type);

    void updateStatus(int id, int status);

    void updateScore(int postId, double score);

    List<DiscussPost> getDiscussPostByOrderMode(int userId, int offset, int limit, int orderMode);
}
