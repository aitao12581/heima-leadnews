package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.admin.IAdminClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HotArticleServiceImpl implements HotArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    /**
     * 计算热点文章
     */
    @Override
    public void computeHotArticle() {
        // 查询前5天的文章
        Date dateParam = DateTime.now().minusDays(50).toDate();
        List<ApArticle> articleList = apArticleMapper.findArticleListByLast5days(dateParam);
//        System.out.println("文章信息"+articleList);
        // 计算文章的分值
        List<HotArticleVo> hotArticleVoList = computeHotArticle(articleList);

        // 为每个频道缓存30条分值较高的文章
        cacheTagToRedis(hotArticleVoList);
    }

    @Autowired
    private IAdminClient wemediaClient;

    @Autowired
    private CacheService cacheService;

    /**
     * 为每个频道缓存30条分值较高的文章
     * @param hotArticleVoList 所有文章的分数及基本信息
     */
    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        // 每个频道缓存30条分数较高的文章
        ResponseResult responseResult = wemediaClient.getChannels();
        if (responseResult.getCode().equals(200)) {
//            System.out.println("远程调用code："+responseResult.getCode());
            String channelStr = JSON.toJSONString(responseResult.getData());
            List<WmChannel> wmChannels = JSON.parseArray(channelStr, WmChannel.class);
            // 检索每个频道的文章
            if (wmChannels != null && wmChannels.size() > 0) {
                for (WmChannel wmChannel : wmChannels) {
                    List<HotArticleVo> voList = hotArticleVoList.stream()
                            .filter(x -> x.getChannelId().equals(wmChannel.getId()))
                            .collect(Collectors.toList());
                    // 给文章进行排序，取30条分值较高的文章存入redis key：频道id value：30条分支高的文章
                    sortAndCache(voList, ArticleConstants.HOT_ARTICLE_FIRST_PAGE+wmChannel.getId());
                }
            }
        }
        // 设置推荐数据
        // 直接给所有文章进行排序，取30 条 存入缓存
        sortAndCache(hotArticleVoList, ArticleConstants.HOT_ARTICLE_FIRST_PAGE+ArticleConstants.DEFAULT_TAG);
    }

    /**
     * 对当前频道的文章信息进行排序并保存到redis
     * @param voList 文章数据
     * @param key 键
     */
    private void sortAndCache(List<HotArticleVo> voList, String key) {
        List<HotArticleVo> sortList = voList.stream()
                .sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
                .collect(Collectors.toList());
        if (sortList.size() > 30) {
            sortList = sortList.subList(0, 30);
        }

        // 进行缓存
        cacheService.set(key, JSON.toJSONString(sortList));
    }

    /**
     * 计算各个文章的分数
     * @param apArticleList  所有上架的文章数据
     * @return
     */
    public List<HotArticleVo> computeHotArticle(List<ApArticle> apArticleList) {
        List<HotArticleVo> hotArticleVoList = new ArrayList<>();

        // 判断参数有效性
        if (apArticleList != null && apArticleList.size() > 0) {
            for (ApArticle apArticle : apArticleList) {
                HotArticleVo hot = new HotArticleVo();
                // 将属性值复制到热点文章
                BeanUtils.copyProperties(apArticle, hot);
                // 计算分数
                int score = computeScore(apArticle);
                hot.setScore(score);
                hotArticleVoList.add(hot);
            }
        }
        return hotArticleVoList;
    }

    /**
     * 计算单篇文章的分数
     * @param apArticle 单篇文章数据
     * @return
     */
    private int computeScore(ApArticle apArticle) {
        Integer score = 0;
        // 喜欢数 分数+5
        if (apArticle.getLikes()!=null) {
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }

        // 阅读数量
        if (apArticle.getViews() != null) {
            score += apArticle.getViews();
        }

        // 收藏量
        if (apArticle.getCollection() != null) {
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        // 评论量
        if (apArticle.getComment() != null) {
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
//        System.out.println(score);
        return score;
    }
}
