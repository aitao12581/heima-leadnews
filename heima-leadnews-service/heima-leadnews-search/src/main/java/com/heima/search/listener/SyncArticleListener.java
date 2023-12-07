package com.heima.search.listener;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 监听为文章创建索引并添加到es索引库的消息
 */
@Component
@Slf4j
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 接收文章内容 接收消息 并为文章创建索引到es库
     * @param message   消息
     */
    @KafkaListener(topics = ArticleConstants.ARTICLE_ES_SYNC_TOPIC)
    public void onMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            log.info("SyncArticleListener,message={}", message);

            // 解析消息
            SearchArticleVo vo = JSON.parseObject(message, SearchArticleVo.class);
            // 创建请求索引
            IndexRequest indexRequest = new IndexRequest("app_info_article");
            indexRequest.id(vo.getId().toString());
            indexRequest.source(message, XContentType.JSON);

            try {
                // 使用索引来为内容在索引库中编制索引
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                System.out.println("编制索引完成");
            } catch (Exception e) {
                e.printStackTrace();
                log.error("sync es error={}",e.toString());
            }
        }
    }
}
