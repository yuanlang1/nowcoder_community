package com.nowcoder.community.service;

import java.util.List;
import java.util.Map;

/**
 * @author yl
 * @date 2025-04-25 10:38
 */
public interface FollowService {
    long getFolloweeCount(int userId, int entityType);

    long getFollowerCount(int entityType, int userId);

    boolean hasFollowed(int UserId, int entityType, int userId);

    void follow(int id, int entityType, int entityId);

    void unfollow(int id, int entityType, int entityId);

    List<Map<String, Object>> getFollowees(int userId, int offset, int limit);

    List<Map<String, Object>> getFollowers(int userId, int offset, int limit);
}
