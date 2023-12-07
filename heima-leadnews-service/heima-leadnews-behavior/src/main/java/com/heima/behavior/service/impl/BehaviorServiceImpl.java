package com.heima.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.behavior.service.BehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.constants.HotArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behaviior.dtos.LikesBehaviorDto;
import com.heima.model.behaviior.dtos.ReadBehaviorDto;
import com.heima.model.behaviior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.UpdateArticleMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BehaviorServiceImpl implements BehaviorService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 用户为文章点赞
     *
     * @param dto 文章的信息
     * @return
     */
    @Override
    public ResponseResult likesBehavior(LikesBehaviorDto dto) {
        // 判断参数有效性
        if (dto == null && dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断用户的登录状态
        ApUser user = AppThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 定义消息发送对象
        UpdateArticleMess mess = new UpdateArticleMess();
        mess.setArticleId(dto.getArticleId());
        mess.setType(UpdateArticleMess.UpdateArticleType.LIKES);

        // 将数据进行处理封装到redis中
        String key = BehaviorConstants.BEHAVIOR_INFO + user.getId() + "_" + dto.getArticleId();
        Map<String, Object> map = new HashMap<>();
        map.put("articleId", dto.getArticleId());
        map.put("type", dto.getType());
        if (dto.getOperation().equals((short) 0)) {
            // 点赞 将数据保存到redis中
            cacheService.hPut(key, "like", JSON.toJSONString(map));
            articleClient.add(dto.getArticleId(), "likes");
            mess.setAdd(1);
        } else {
            // 取消点赞
            cacheService.hDelete(key,"like");
            articleClient.sub(dto.getArticleId(), "likes");
            mess.setAdd(-1);
        }

        // 向热点文章主题发送消息
        kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 用户阅读文章次数
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult readBehavior(ReadBehaviorDto dto) {
        // 判断参数有效性
        if (dto == null && dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断用户是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 为redis中的数据添加内容
        if (dto.getCount()>=0) {
            cacheService.hPut(BehaviorConstants.BEHAVIOR_INFO+user.getId()+"_"+dto.getArticleId(), "count",
                    JSON.toJSONString(dto));
            articleClient.add(dto.getArticleId(), "views");

            // 发送消息，数据聚合
            UpdateArticleMess mess = new UpdateArticleMess();
            mess.setType(UpdateArticleMess.UpdateArticleType.VIEWS);
            mess.setArticleId(dto.getArticleId());
            mess.setAdd(1);

            kafkaTemplate.send(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC, JSON.toJSONString(mess));

            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "数量不能为负");
    }

    /**
     * 文章喜欢 或取消喜欢
     *
     * @param dto 信息
     * @return
     */
    @Override
    public ResponseResult unLikesBehavior(UnLikesBehaviorDto dto) {
        // 验证参数有效性
        if (dto == null && dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断用户登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        String key = BehaviorConstants.BEHAVIOR_INFO+user.getId()+"_"+dto.getArticleId();

        // 根据要求对redis进行操作
        if (dto.getType().equals((short) 0)) {
            // 喜欢 添加数据
            cacheService.hPut(key, "unlike", dto.getArticleId()+"");
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        // 不喜欢删除数据
        cacheService.hDelete(key, "unlike");

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
