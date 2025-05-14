package com.nowcoder.community.service;

/**
 * @author yl
 * @date 2025-04-24 22:16
 */
public interface LikeService {
    void like(int userId, int entityType, int entityId, int entityUserId);

    // 查询某实体的点赞数量
    long getEntityLikeCount(int entityType, int entityId);

    // 查询某人对莫实体的点赞状态
    int getEntityLikeStatus(int userId, int entityType, int entityId);

    // 查询某个用户获得的赞
    long getUserLikeCount(int userId);
}
