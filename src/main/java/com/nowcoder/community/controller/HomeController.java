package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.mapper.UserMapper;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yl
 * @date 2025-04-23 12:28
 */
@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String index(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        // 分页
        page.setPath("/index?orderMode = " + orderMode);
        page.setRows(discussPostService.getDiscussPostRows(0));

        // 所有帖子
        List<DiscussPost> discussPostList = discussPostService.getDiscussPostByOrderMode(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        if (discussPostList != null) {
            for (DiscussPost discussPost : discussPostList) {
                Map<String, Object> discussPostVo = new HashMap<>();
                // 帖子
                discussPostVo.put("post", discussPost);

                // 帖子作者
                User user = userMapper.getById(discussPost.getUserId());
                discussPostVo.put("user", user);

                // 帖子点赞数量
                long likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                discussPostVo.put("likeCount", likeCount);
                discussPosts.add(discussPostVo);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);

        return "/index";
    }

    @RequestMapping(path = "error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }
}
