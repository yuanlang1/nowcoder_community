package com.nowcoder.community.service;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

/**
 * @author yl
 * @date 2025-04-27 11:24
 */
public interface ElasticsearchService {
    // 保存
    void saveDiscssPost(DiscussPost post);

    // 通过id删除
    void deleteDiscussPost(int id);

    // 查找
    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit);
}
