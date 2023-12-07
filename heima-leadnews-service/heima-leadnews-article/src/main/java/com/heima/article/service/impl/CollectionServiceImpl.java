package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.mapper.ApCollectionMapper;
import com.heima.article.service.CollectionService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.constants.HotArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApCollection;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    private ApCollectionMapper collectionMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 收藏文章
     *
     * @param dto 收藏文章的部分信息
     * @return
     */
    @Override
    public ResponseResult collection(CollectionBehaviorDto dto) {
        // 判断参数
        if (dto == null && dto.getEntryId()==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断用户是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setType(UpdateArticleMess.UpdateArticleType.COLLECTION);
        mess.setArticleId(dto.getEntryId());

        Short operation = dto.getOperation();
        if (operation.equals((short) 0)) {
            // 收藏
            // 封装数据
            ApCollection apCollection = new ApCollection();
            BeanUtils.copyProperties(dto, apCollection);
            apCollection.setArticleId(dto.getEntryId());
            apCollection.setEntryId(user.getId());
            apCollection.setCollectionTime(new Date());

            // 在文章数据中添加数量
            addArticleToDb(dto.getEntryId(), "collection");

            // 将数据保存到mysql表中
            collectionMapper.insert(apCollection);

            mess.setAdd(1);
            // 将数据保存到redis中
//            editRedis(apCollection, "add");
        } else {
            // 取消收藏
            // 删除对应表中数据
            ApCollection apCollection = collectionMapper.selectOne(Wrappers.<ApCollection>lambdaQuery()
                    .eq(ApCollection::getEntryId, user.getId())
                    .eq(ApCollection::getArticleId, dto.getEntryId()));
            collectionMapper.delete(Wrappers.<ApCollection>lambdaQuery()
                    .eq(ApCollection::getEntryId, user.getId())
                    .eq(ApCollection::getArticleId, dto.getEntryId()));
//        editRedis(apCollection, "del");
            subArticleToDb(dto.getEntryId(), "collection");
            mess.setAdd(-1);
        }
        // 向热点文章主题发送消息
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 数据库文章收藏数量减一
     * @param entryId
     * @param type
     */
    public void subArticleToDb(Long entryId, String type) {
        // 获取当前文章信息
        ApArticle apArticle = apArticleMapper.selectById(entryId);
        // 根据类型对数据进行减一操作
        switch (type) {
            case "collection":
                if (apArticle.getCollection()!=null) {
                    apArticleMapper.subOftype(entryId, type);
                    break;
                }
            case "likes":
                if (apArticle.getLikes()!=null) {
                    apArticleMapper.subOftype(entryId, type);
                    break;
                }
            case "views":
                if (apArticle.getViews()!=null) {
                    apArticleMapper.subOftype(entryId, type);
                    break;
                }
        }
    }

    @Autowired
    private ApArticleMapper apArticleMapper;

    /**
     * 数据库文章收藏数量+1
     * @param entryId
     */
    public void addArticleToDb(Long entryId, String type) {
        // 根据文章id查询文章数据
        ApArticle apArticle = apArticleMapper.selectById(entryId);

        boolean flag = false;
        // 根据标识判断对哪个数据做操作
        switch (type) {
            case "collection":
                if (apArticle.getCollection()==null) {
                    apArticle.setCollection(1);
                    apArticleMapper.updateById(apArticle);
                    flag = true;
                    break;
                }
            case "likes":
                if (apArticle.getLikes()==null) {
                    apArticle.setLikes(1);
                    apArticleMapper.updateById(apArticle);
                    flag = true;
                    break;
                }
            case "views":
                if (apArticle.getViews()==null) {
                    apArticle.setViews(1);
                    apArticleMapper.updateById(apArticle);
                    flag = true;
                    break;
                }
        }
        // 数量不为null
        if (!flag) {
            apArticleMapper.updateOfType(entryId, type);
        }
    }

    @Autowired
    private CacheService cacheService;

    /**
     * 对redis数据库进行操作
     * @param apCollection  需要操作的数据内容
     * @param flag  操作方式
     */
    private void editRedis(ApCollection apCollection, String flag) {
        String key = BehaviorConstants.BEHAVIOR_INFO+apCollection.getEntryId()+"_"+apCollection.getArticleId();
        if ("add".equals(flag)) {
            // 保存数据到redis
            cacheService.hPut(key, "collection", JSON.toJSONString(apCollection));
        } else if ("del".equals(flag)) {
            // 删除指定键的相关数据
            cacheService.hDelete(key, "collection");
        }
    }
}
