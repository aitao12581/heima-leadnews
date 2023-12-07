package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.ArticleVisitStreamMess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    // 单页查询的最大数字
    private static final Short MAX_PAGE_SIZE = 50;

    @Autowired
    private ApArticleMapper articleMapper;

    /**
     * 加载文章数据
     *
     * @param loadType 加载类型 1 加载更多 2 加载最新
     * @param dto      判断条件
     * @return
     */
    @Override
    public ResponseResult load(Short loadType, ArticleHomeDto dto) {
        // 校验参数
        Integer size = dto.getSize();
        if (size == null || size == 0) {
            // 默认分页查询的条目数为10
            size = 10;
        }
        // 比较前端传入的条目数，和最大条目数比较，取最小值存入dto模型中
        size = Math.min(size, MAX_PAGE_SIZE);
        dto.setSize(size);

        // 类型参数校验
        if (!loadType.equals(ArticleConstants.LOADTYPE_LOAD_MORE) &&
                !loadType.equals(ArticleConstants.LOADTYPE_LOAD_NEW)) {
            // 不是加载最新 和 加载最多 查询类型设置为默认
            loadType = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        // 文章类型校验
        if (StringUtils.isEmpty(dto.getTag())) {
            // 未传递文章类型 则 设置为默认
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }


        // 时间校验
        if (dto.getMinBehotTime() == null) dto.setMinBehotTime(new Date());
        if (dto.getMaxBehotTime() == null) dto.setMaxBehotTime(new Date());

        // 查询数据
        List<ApArticle> list = articleMapper.loadArticleList(dto, loadType);

        // 封装结果并返回
        return ResponseResult.okResult(list);
    }

    @Autowired
    private ApArticleConfigMapper articleConfigMapper;

    @Autowired
    private ApArticleContentMapper contentMapper;

    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;

    /**
     * 保存app端文章
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        // 检查参数
        if (dto==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);

        // 判断是否存在id
        if (dto.getId()==null) {
            // 不存在 id 保存 文章 文章配置 文章内容
            // 保存文章
            save(apArticle);

            // 保存配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            articleConfigMapper.insert(apArticleConfig);

            // 保存文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            contentMapper.insert(apArticleContent);
            log.info("数据保存成功");
        } else {
            // 存在id 修改 文章 文章内容
            // 修改文章
            updateById(apArticle);

            // 修改文章内容
            ApArticleContent content = contentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery()
                    .eq(ApArticleContent::getArticleId, apArticle.getId()));
            content.setContent(dto.getContent());
            contentMapper.updateById(content);
        }

        // 异步调用静态文件生成方法，并上传到minio中
        articleFreemarkerService.buildArticleToMinIO(apArticle, dto.getContent());

        // 返回结果
        return ResponseResult.okResult(apArticle.getId());
    }

    @Autowired
    private CacheService cacheService;

    /**
     * 加载文章列表
     *
     * @param dto       参数
     * @param type      1 加载更多   2 加载最新
     * @param firstPage true  是首页  flase 非首页
     * @return
     */
    @Override
    public ResponseResult load2(ArticleHomeDto dto, Short type, boolean firstPage) {
        if (firstPage) {
            // 加载首页推荐数据
            String jsonStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + dto.getTag());
            if (StringUtils.isNotBlank(jsonStr)) {
                List<HotArticleVo> hotArticleVoList = JSON.parseArray(jsonStr, HotArticleVo.class);
                return ResponseResult.okResult(hotArticleVoList);
            }
        }
        return load(type, dto);
    }

    /**
     * 更新文章的分值 同时更新缓存中的热点文章数据
     *
     * @param mess 要更新的数据
     */
    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        // 更新文章的阅读、点赞、收藏、评论0的数量
//        ApArticle apArticle = updateArticle(mess);

        // 根据id查询文章信息 (自写逻辑)
        ApArticle apArticle = getById(mess.getArticleId());

        // 计算分值
        Integer score = computeScore(apArticle);
        score = score * 3;

        // 替换当前文章对应频道的热点文章
        replaceDataToRedis(apArticle, score,
                ArticleConstants.HOT_ARTICLE_FIRST_PAGE+apArticle.getChannelId());

        // 替换推荐对应的热点文章
        replaceDataToRedis(apArticle, score,
                ArticleConstants.HOT_ARTICLE_FIRST_PAGE+ArticleConstants.DEFAULT_TAG);

    }

    /**
     * 替换对应数据并存入redis
     * @param apArticle 文章信息
     * @param score 文章评分
     * @param key   键
     */
    private void replaceDataToRedis(ApArticle apArticle, Integer score, String key) {
        String articleListStr = cacheService.get(key);
        // 能够查询到数据
        if (StringUtils.isNotBlank(articleListStr)) {
            List<HotArticleVo> hotArticleVoList = JSON.parseArray(articleListStr, HotArticleVo.class);

            // 定义标记
            boolean flag = true;

            // 如果缓存中存在该文章，只更新分数
            for (HotArticleVo hotArticleVo : hotArticleVoList) {
                if (hotArticleVo.getId().equals(apArticle.getId())) {
                    hotArticleVo.setScore(score);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                // 文章不在缓存文件中，查询缓存中分值最小的文章，如果当前文章的分值大于缓存数据，就替换
                if (hotArticleVoList.size()>=30) {
                    // 对缓存数据进行排序，获取分值最小的文章
                    hotArticleVoList = hotArticleVoList.stream()
                            .sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
                            .collect(Collectors.toList());
                    HotArticleVo lastHot = hotArticleVoList.get(hotArticleVoList.size() - 1);
                    // 最后一篇文章分数小于当前文章
                    if (lastHot.getScore() < score) {
                        hotArticleVoList.remove(lastHot);
                        HotArticleVo articleVo = new HotArticleVo();
                        BeanUtils.copyProperties(apArticle, articleVo);
                        articleVo.setScore(score);
                        hotArticleVoList.add(articleVo);
                    }
                } else {
                    // 文章总数小于30条
                    HotArticleVo articleVo = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle, articleVo);
                    articleVo.setScore(score);
                    hotArticleVoList.add(articleVo);
                }
            }
            // 缓存文章到redis
            hotArticleVoList = hotArticleVoList.stream()
                    .sorted(Comparator.comparing(HotArticleVo::getScore).reversed())
                    .collect(Collectors.toList());
            cacheService.set(key, JSON.toJSONString(hotArticleVoList));
        }
    }

    /**
     * 根据文章信息更新分值
     * @param apArticle 文章信息
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if (apArticle.getCollection()!=null) {
            score += apArticle.getCollection()*ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        if (apArticle.getLikes()!=null) {
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }

        if (apArticle.getComment()!=null) {
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }

        if (apArticle.getViews()!=null) {
            score += apArticle.getViews();
        }

        return score;
    }

    /**
     * 更新文章行为数量
     * @param mess 聚合函数返回方法
     * @return
     */
    private ApArticle updateArticle(ArticleVisitStreamMess mess) {
        ApArticle apArticle = getById(mess.getArticleId());
        apArticle.setCollection(apArticle.getCollection()==null ?
                0 : apArticle.getCollection()+mess.getCollect());
        apArticle.setComment(apArticle.getComment()==null ? 0 : apArticle.getCollection()+mess.getCommit());
        apArticle.setLikes(apArticle.getLikes()==null?0: apArticle.getLikes()+mess.getLike());
        apArticle.setViews(apArticle.getViews()==null?0: apArticle.getViews()+mess.getView());
        updateById(apArticle);
        return apArticle;
    }
}
