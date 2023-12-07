package com.heima.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.constants.UserConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserFan;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.mapper.ApUserFanMapper;
import com.heima.user.mapper.ApUserFollowMapper;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.UserRelationService;
import com.heima.utils.common.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@Transactional
public class UserRelationServiceImpl implements UserRelationService {

    @Autowired
    private ApUserMapper apUserMapper;

    @Autowired
    private ApUserFollowMapper userFollowMapper;

    @Autowired
    private ApUserFanMapper userFanMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ReplyingKafkaTemplate template;

    @Autowired
    private CacheService cacheService;
    /**
     * 关注文章作者或取消关注
     *
     * @param dto 信息
     * @return
     */
    @Override
    public ResponseResult userFollow(UserRelationDto dto){
        // 判断参数有效性
        if (dto == null && dto.getAuthorId() == null && dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断当前用户是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 获取当前用户和作者的信息
        ApUser customer = apUserMapper.selectById(user.getId());    // 当前登录用户

        // 向自媒体端发送消息
//        kafkaTemplate.send(UserConstants.AP_USER_NEWS_USER_TOPIC, JSON.toJSONString(dto));
        WmUser author = null;

        // 生成redis中的键
        String key = BehaviorConstants.BEHAVIOR_INFO+user.getId()+"_"+dto.getArticleId();
        Map<String, Object> map = new HashMap<>();
        map.put("articleId", dto.getArticleId());
        map.put("authorId", dto.getAuthorId());
        try {
            // 使用ReplyingKafkaTemplate获取消费者返回数据
            ProducerRecord<String, String> record = new ProducerRecord<>(UserConstants.AP_USER_NEWS_USER_TOPIC,
                    JSON.toJSONString(dto));
            RequestReplyFuture<String, String, String> replyFuture = template.sendAndReceive(record);
            ConsumerRecord<String, String> consumerRecord = replyFuture.get();
            log.info("接收消息消费者发送的消息：{}", consumerRecord.value());
            author = JSON.parseObject(consumerRecord.value(), WmUser.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断状态是关注还是取消关注
        if (dto.getOperation() == 0) {
            // 关注 - 向数据库中的关注表和粉丝表添加数据
            ApUserFollow userFollow = new ApUserFollow();
            userFollow.setUserId(customer.getId());
            userFollow.setFollowId(author.getId());
            userFollow.setFollowName(author.getName());
            userFollow.setLevel(BehaviorConstants.LEVEL_OCCA);  // 默认 为偶尔关注
            userFollow.setIsNotice(BehaviorConstants.NOTICE_ON);   // 默认为开启动态通知
            userFollow.setCreatedTime(new Date());
            // 添加用户关注表
            userFollowMapper.insert(userFollow);

            // 向粉丝表填充数据
            ApUserFan userFan = new ApUserFan();
            userFan.setUserId(author.getId());
            userFan.setFansId(customer.getId());
            userFan.setFansName(customer.getName());
            userFan.setLevel(BehaviorConstants.LEVEL_FAN_DEFAULT); // 默认 正常
            userFan.setCreatedTime(new Date());
            userFan.setIsDisplay(BehaviorConstants.NOTICE_ON);  // 是否可见动态
            userFan.setIsShieldLetter(BehaviorConstants.NOTICE_ON); // 是否屏蔽私信
            userFan.setIsShieldComment(BehaviorConstants.NOTICE_ON);   // 是否屏蔽评论

            // 保存粉丝信息
            userFanMapper.insert(userFan);

            // 将数据保存到redis中 用户关注使用有序集合存储 粉丝使用set存储
//            editRedis(userFan, userFollow, "add");
            cacheService.hPut(key, "follow", JSON.toJSONString(map));

            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        // 取消关注，删除表中的数据即可
        ApUserFan apUserFan = userFanMapper.selectOne(Wrappers.<ApUserFan>lambdaQuery()
                .eq(ApUserFan::getUserId, dto.getAuthorId())
                .eq(ApUserFan::getFansId, user.getId()));

        userFanMapper.delete(Wrappers.<ApUserFan>lambdaQuery()
                .eq(ApUserFan::getUserId, dto.getAuthorId())
                .eq(ApUserFan::getFansId, user.getId()));

        ApUserFollow userFollow = userFollowMapper.selectOne(Wrappers.<ApUserFollow>lambdaQuery()
                .eq(ApUserFollow::getUserId, user.getId())
                .eq(ApUserFollow::getFollowId, dto.getAuthorId()));

        userFollowMapper.delete(Wrappers.<ApUserFollow>lambdaQuery()
                .eq(ApUserFollow::getUserId, user.getId())
                .eq(ApUserFollow::getFollowId, dto.getAuthorId()));

        // 删除redis中对应集合中的数据
//        editRedis(apUserFan, userFollow, "del");
        cacheService.hDelete(key, "follow");

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 将数据保存至redis
     * @param userFan  粉丝数据
     * @param userFollow 关注的作者数据
     */
    private void editRedis(ApUserFan userFan, ApUserFollow userFollow, String flag) {
        String keyFan = "fans_"+userFan.getUserId();    // 粉丝数据标识
        String keyFollow = "follow_"+userFollow.getUserId();    // 关注数据标识
        if ("add".equals(flag)) {
            // 将粉丝数据存入
            cacheService.sAdd(keyFan, JSON.toJSONString(userFan));
            // 将关注数据存入
            cacheService.sAdd(keyFollow, JSON.toJSONString(userFollow));
        } else if ("del".equals(flag)) {
            // 删除对应键中的值
            Long fanSize = cacheService.sSize(keyFan);
            Long followSize = cacheService.sSize(keyFollow);
            if (fanSize.equals(followSize) && fanSize == 1) {
                cacheService.delete(keyFan);
                cacheService.delete(keyFollow);
            }
            cacheService.sRemove(keyFan, JSON.toJSONString(userFan));
            cacheService.sRemove(keyFollow, JSON.toJSONString(userFollow));
        }
    }


}
