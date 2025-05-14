package com.nowcoder.community.service.Impl;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.Order;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yl
 * @date 2025-04-27 11:25
 */
@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    // 保存
    @Override
    public void saveDiscssPost(DiscussPost post){
        discussPostRepository.save(post);
    }
    // 通过id删除
    @Override
    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }
    // 查找
    @Override
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit){
        List<HighlightField> highlightFields = List.of(
                new HighlightField("title"),
                new HighlightField("content")
        );

        HighlightParameters parameters = HighlightParameters
                .builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .withNumberOfFragments(0)
                .build();

        Highlight highlight = new Highlight(parameters, highlightFields);

        NativeQuery queryBuilder = new NativeQueryBuilder()
                .withQuery(QueryBuilders.multiMatch(f->f.query(keyword).fields("title", "content")))
                .withSort(Sort.by(
                        new Order(Sort.Direction.DESC, "type"),
                        new Order(Sort.Direction.DESC, "score"),
                        new Order(Sort.Direction.DESC, "createTime")))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightQuery(new HighlightQuery(highlight, DiscussPost.class))
                .build();

        SearchHits<DiscussPost> searchHits = elasticsearchTemplate.search(queryBuilder, DiscussPost.class);

        List<DiscussPost> posts = searchHits.getSearchHits().stream().map(hit ->{
            DiscussPost post = hit.getContent();
            hit.getHighlightFields().forEach((field, fragments) ->  {
                String joined = String.join("", fragments);
                if ("title".equals(field)) {
                    post.setTitle(joined);
                } else if ("content".equals(field)) {
                    post.setContent(joined);
                }
            });
            return post;
        }).collect(Collectors.toList());

        return new PageImpl<>(
                posts,
                PageRequest.of(current, limit),
                searchHits.getTotalHits()
        );

    }
}
