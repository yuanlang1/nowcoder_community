package com.nowcoder.community.service.Impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.mapper.DiscussPostMapper;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.SensitiveFilter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.swing.text.html.HTML;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yl
 * @date 2025-04-23 20:25
 */
@Service
public class DiscussPostServiceImpl implements DiscussPostService {

    private static  final Logger logger = LoggerFactory.getLogger(DiscussPostServiceImpl.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // 缓存帖子
    private LoadingCache<String, List<DiscussPost>> postListCache;
    // 缓存帖子总数
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@Nonnull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误");
                        }
                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.debug("load list from DB");
                        return discussPostMapper.selectDiscussPostByOrderMode(0, offset, limit, 1);
                    }
                });
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@Nonnull Integer key) throws Exception {
                        logger.debug("load post rows from DB");
                        return discussPostMapper.getDiscussPostCount(key);
                    }
                });
    }

    @Override
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 转义HTML标记
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    @Override
    public DiscussPost getDiscussPostById(int discussPostId) {
        return discussPostMapper.getDiscussPostById(discussPostId);
    }

    @Override
    public int getDiscussPostRows(int userId) {
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        logger.debug("Rows load post from DB");
        return discussPostMapper.getDiscussPostCount(userId);
    }

    @Override
    public List<DiscussPost> getDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.getDiscussPosts(userId, offset, limit);
    }

    @Override
    public void updateType(int id, int type) {
        discussPostMapper.updateType(id, type);
    }

    @Override
    public void updateStatus(int id, int status) {
        discussPostMapper.updateStatus(id, status);
    }

    @Override
    public void updateScore(int id, double score) {
        discussPostMapper.updateScore(id, score);
    }

    @Override
    public List<DiscussPost> getDiscussPostByOrderMode(int userId, int offset, int limit, int orderMode) {
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load list from DB");
        return discussPostMapper.selectDiscussPostByOrderMode(userId, offset, limit, orderMode);
    }

}
