package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author yl
 * @date 2025-04-25 10:37
 */
@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId)
                .setUserId(hostHolder.getUser().getId());
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注!");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注!");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user", user);

        page.setPath("/followees/" + userId);
        page.setRows((int) followService.getFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userlist = followService.getFollowees(userId, page.getOffset(), page.getLimit());

        if (userlist != null) {
            for (Map<String, Object> map : userlist) {
                User u = (User) map.get("user");
                map.put("hasFollowed", followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId));
            }
        }
        model.addAttribute("users", userlist);

        return "/site/follower";
    }


    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        model.addAttribute("user", user);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.getFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.getFollowers(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId));
            }
        }
        model.addAttribute("users", userList);

        return "/site/follower";
    }
}
