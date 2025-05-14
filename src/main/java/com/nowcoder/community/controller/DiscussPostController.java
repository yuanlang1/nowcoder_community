package com.nowcoder.community.controller;

import com.google.gson.JsonObject;
import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.mapper.CommentMapper;
import com.nowcoder.community.mapper.DiscussPostMapper;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author yl
 * @date 2025-04-23 17:06
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostController.class);

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;

    // 添加帖子
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还未登录哦！");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityId(discussPost.getId())
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, discussPost.getId());

        return CommunityUtil.getJSONString(0, "发布成功!");
    }

    // 帖子详情
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        logger.info("discussPostId：" + discussPostId);
        // 查找帖子
        DiscussPost discussPost = discussPostService.getDiscussPostById(discussPostId);
        model.addAttribute("post", discussPost);

        // 查找作者
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.getEntityLikeStatus(hostHolder.getUser().getId(),
                ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);


        // 分页
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());

        // 评论  评论 / 回贴
        // 评论的类型为帖子 entity_type=ENTITY_TYPE_POST  entity_id=post.id
        List<Comment> commentList = commentService.findCommentEntity(ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());

        List<Map<String, Object>> commentVoList = new ArrayList<>();

        // 遍历查询
        if (commentList != null) {
            for (Comment comment: commentList) {
                // 评论列表
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 评论作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 点赞状态
                commentVo.put("likeStatus", hostHolder.getUser() == null ? 0 : likeService.getEntityLikeStatus(hostHolder.getUser().getId(),
                        ENTITY_TYPE_COMMENT, comment.getId()));
                // 点赞数量
                commentVo.put("likeCount", likeService.getEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId()));


                // 回复该评论的回帖
                List<Comment> replyList = commentService.findCommentEntity(ENTITY_TYPE_COMMENT, comment.getId(),
                        0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply: replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复信息
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞状态
                        replyVo.put("likeStatus", hostHolder.getUser() == null ? 0 : likeService.getEntityLikeStatus(hostHolder.getUser().getId(),
                                ENTITY_TYPE_COMMENT, reply.getId()));
                        // 点赞数量
                        replyVo.put("likeCount", likeService.getEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId()));

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                commentVo.put("replyCount", commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId()));

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST);
        eventProducer.fireEvent(event);

        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String delete(int id){
        discussPostService.updateStatus(id, 2);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setEntityId(id)
                .setEntityType(ENTITY_TYPE_POST)
                .setUserId(hostHolder.getUser().getId());
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
