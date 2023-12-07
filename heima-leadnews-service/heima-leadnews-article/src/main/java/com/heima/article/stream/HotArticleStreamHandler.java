package com.heima.article.stream;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.mess.UpdateArticleMess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 消费方接受消息并对消息处理的聚合类
 */
@Configuration
@Slf4j
public class HotArticleStreamHandler {

    @Bean
    public KStream<String, String> kStream(StreamsBuilder streamsBuilder) {
        // 接收消息
        KStream<String, String> stream = streamsBuilder
                .stream(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC);

        // 聚合流式处理
        stream.map((key, value) -> {
            UpdateArticleMess mess = JSON.parseObject(value, UpdateArticleMess.class);
            // 重置消息的key为文章id， 值为类型及增量
            return new KeyValue<>(mess.getArticleId().toString(), mess.getType().name()+":"+mess.getAdd());
        })
                // 按照文章id进行聚合
                .groupBy((key, value) -> key)
                // 设置时间窗口
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                // 自行完成聚合计算
                .aggregate(new Initializer<String>() {
                    // 初始方法 返回值就是消息的value
                    @Override
                    public String apply() {
                        return "COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0";
                    }
                    // 真正的聚合操作，返回值是消息的value
                }, new Aggregator<String, String, String>() {
                    @Override
                    public String apply(String key, String value, String aggValue) {
                        if (StringUtils.isBlank(value)){
                            // value 为 null
                            return aggValue;
                        }
                        // value 不为空
                        String[] aggAry = aggValue.split(",");
                        int col = 0, com = 0, like = 0, vie = 0;
                        // 遍历数组
                        for (String agg : aggAry) {
                            String[] split = agg.split(":");
                            /**
                             * 获取初始值，也就是时间窗口内计算之后的值 初始化值
                             */
                            switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])) {
                                case COLLECTION:
                                    col = Integer.parseInt(split[1]);
                                    break;
                                case COMMENT:
                                    com = Integer.parseInt(split[1]);
                                    break;
                                case LIKES:
                                    like = Integer.parseInt(split[1]);
                                    break;
                                case VIEWS:
                                    vie = Integer.parseInt(split[1]);
                                    break;
                            }
                        }
                        /**
                         *  对计算出的值进行累加操作
                         */
                        String[] valAry = value.split(":");
                        switch (UpdateArticleMess.UpdateArticleType.valueOf(valAry[0])) {
                            case COLLECTION:
                                col += Integer.parseInt(valAry[1]);
                                break;
                            case COMMENT:
                                com += Integer.parseInt(valAry[1]);
                                break;
                            case LIKES:
                                like += Integer.parseInt(valAry[1]);
                                break;
                            case VIEWS:
                                vie += Integer.parseInt(valAry[1]);
                                break;
                        }

                        // 对结果进行格式化处理
                        String formatStr =
                                String.format("COLLECTION:%d,COMMENT:%d,LIKES:%d,VIEWS:%d", col, com, like, vie);
                        System.out.println("当前文章的id为:" + key);
                        System.out.println("当前时间窗口内消息处理的结果：" + formatStr);
                        return formatStr;
                    }
                }, Materialized.as("hot-atricle-stream-count-001"))
                .toStream()
                .map((key, value)->{
                    return new KeyValue<>(key.key().toString(), formatObj(key.key().toString(), value));
                })
                // 发送消息
                .to(HotArticleConstants.HOT_ARTICLE_INCR_HANDLE_TOPIC);

        return stream;
    }

    /**
     *  格式化消息的value值
     * @param articleId   键  文章id
     * @param value 进行变化的值
     * @return
     */
    private String formatObj(String articleId, String value) {
        // 封装向消费者返回的数据格式
        ArticleVisitStreamMess mess = new ArticleVisitStreamMess();
        mess.setArticleId(Long.valueOf(articleId));
        // "COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0"
        String[] valAry = value.split(",");
        for (String val : valAry) {
            String[] split = val.split(":");
            switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])) {
                case COLLECTION:
                    mess.setCollect(Integer.parseInt(split[1]));
                    break;
                case COMMENT:
                    mess.setCommit(Integer.parseInt(split[1]));
                    break;
                case LIKES:
                    mess.setLike(Integer.parseInt(split[1]));
                    break;
                case VIEWS:
                    mess.setView(Integer.parseInt(split[1]));
                    break;
            }
        }
        log.info("聚合消息处理之后返回的结果为：{}", JSON.toJSONString(mess));
        return JSON.toJSONString(mess);
    }
}
