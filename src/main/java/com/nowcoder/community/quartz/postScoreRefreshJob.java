package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import lombok.Data;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author yl
 * @date 2025-04-28 23:03
 */
@Component
public class postScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(postScoreRefreshJob.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2025-01-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化起始失败", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);

        if (operations.size() == 0) {
            logger.error("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 开始刷新帖子分数：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务介绍] 刷新帖子分数完毕!");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.getDiscussPostById(postId);

        if (post == null) {
            logger.error("该帖子不存在：id = " + postId);
            return;
        }

        // 是否为精
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.getEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 权重
        double w = (wonderful ? 75 : 0) + commentCount * 10L + likeCount * 2;
        double score = Math.log10(Math.max(w, 1))
            + (double) (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // 更新帖子分数
        discussPostService.updateScore(postId, score);

        // 更新es
        post.setScore(score);
        elasticsearchService.saveDiscssPost(post);
    }
}
