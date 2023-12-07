package com.heima.es;

import com.alibaba.fastjson.JSON;
import com.heima.es.mapper.ApArticleMapper;
import com.heima.es.pojo.SearchArticleVo;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * 将所有的app端符合要求的文章上传到es索引库中
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ApArticleTest {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 注意：数据量的导入，如果数据量过大，需要分页导入
     * @throws Exception
     */
    @Test
    public void init() throws Exception {
        // 1.查询所有符合条件的文章数据
        List<SearchArticleVo> articleVos = apArticleMapper.loadArticleList();

        // 2.批量导入es索引库
        // 批量请求包含有序的请求，允许单个批处理执行，只支持批量请求刷新，不支持项目刷新
        BulkRequest bulkRequest = new BulkRequest("app_info_article");

        for (SearchArticleVo articleVo : articleVos) {
            IndexRequest indexRequest = new IndexRequest().id(articleVo.getId().toString())
                    .source(JSON.toJSONString(articleVo), XContentType.JSON);
            // 批量添加数据
            bulkRequest.add(indexRequest);
        }
        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

}