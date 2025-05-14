package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-23 20:27
 */
@Mapper
public interface DiscussPostMapper {
    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost getDiscussPostById(int discussPostId);

    void updateCommentCount(int id, int count);

    int getDiscussPostCount(int userId);

    List<DiscussPost> getDiscussPosts(int userId, int offset, int limit);

    void updateType(int id, int type);

    void updateStatus(int id, int status);

    void updateScore(int id, double score);

    List<DiscussPost> selectDiscussPostByOrderMode(int userId, int offset, int limit, int orderMode);

}

