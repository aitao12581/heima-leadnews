package com.heima.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApArticleSearchService;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.common.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ArticleSearchServiceImpl implements ApArticleSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ApUserSearchService apUserSearchService;

    /**
     * es 索引库文章分页搜索  前端问题无法显示
     *
     * @param dto 查询条件
     * @return 查询结果
     * @throws IOException io异常
     */
    @Override
    public ResponseResult search(UserSearchDto dto) throws IOException {

        // 检查参数
        if (dto == null || StringUtils.isBlank(dto.getSearchWords())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser user = AppThreadLocalUtil.getUser();
        // 异步调用保存搜索记录
        if (user!=null && dto.getFromIndex() == 0) {
            apUserSearchService.insert(dto.getSearchWords(), user.getId());
        }

        // 设置查询条件
        SearchRequest searchRequest = new SearchRequest("app_info_article");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 对关键字分词后查询
        // field添加查询的关联字段
        QueryStringQueryBuilder defaulted = QueryBuilders.queryStringQuery(dto.getSearchWords())
                .field("title").field("content").defaultOperator(Operator.OR);
        // 向布尔查询中添加一个查询
        boolQuery.must(defaulted);

        // 筛选小于mindate的数据
//        RangeQueryBuilder publishTime = QueryBuilders.rangeQuery("publishTime")
//                .lt(dto.getMinBehotTime().getTime());
        RangeQueryBuilder publishTime = QueryBuilders.rangeQuery("publishTime")
                .lt(new Date().getTime());
        boolQuery.filter(publishTime);

        // 进行分页查询
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(dto.getPageSize());

        // 按照发布时间倒序查询
        searchSourceBuilder.sort("publishTime", SortOrder.DESC);

        // 设置高亮  title
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color: red; font-size: inherit;'>");
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);

        // 将boolean查询添加到搜索源中
        searchSourceBuilder.query(boolQuery);
        searchRequest.source(searchSourceBuilder);
        SearchResponse result = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 封装结果并返回
        List<Map> list = new ArrayList<>();

        SearchHit[] hits = result.getHits().getHits();
        for (SearchHit hit : hits) {
            String rsStr = hit.getSourceAsString();
            Map map = JSON.parseObject(rsStr, Map.class);
            // 处理高亮
            if (hit.getHighlightFields() != null && hit.getHighlightFields().size()>0) {
                Text[] titles = hit.getHighlightFields().get("title").getFragments();
                String title = StringUtils.join(titles);
                // 高亮标题
                map.put("h_title", title);
            } else {
                map.put("h_title", map.get("title"));
            }
            list.add(map);
        }

        return ResponseResult.okResult(list);
    }
}
