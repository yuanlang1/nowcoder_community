package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yl
 * @date 2025-04-24 23:28
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    private String like(int entityType, int entityId, int entityUserId, int postId) {
        likeService.like(hostHolder.getUser().getId(), entityType, entityId, entityUserId);

        long likeCount = likeService.getEntityLikeCount(entityType, entityId);
        int likeStatus = likeService.getEntityLikeStatus(hostHolder.getUser().getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        // 点赞数量
        map.put("likeCount", likeCount);
        // 是否点赞
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeCount == 1) {
             Event event = new Event()
                     .setTopic(TOPIC_LIKE)
                     .setEntityType(entityType)
                     .setEntityId(entityId)
                     .setEntityUserId(entityUserId)
                     .setUserId(hostHolder.getUser().getId())
                     .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        // 计算帖子分数 对帖子点赞，帖子分数会更新
        if (ENTITY_TYPE_POST == entityType) {
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
