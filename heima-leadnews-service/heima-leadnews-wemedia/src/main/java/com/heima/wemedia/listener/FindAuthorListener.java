package com.heima.wemedia.listener;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.UserConstants;
import com.heima.model.user.dtos.UserRelationDto;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FindAuthorListener {

    @Autowired
    private WmUserMapper wmUserMapper;

    @KafkaListener(topics = UserConstants.AP_USER_NEWS_USER_TOPIC)
    @SendTo // 返回内容到消息提供者
    public String onMessage(String message) {
        if (StringUtils.isNotBlank(message)) {
            UserRelationDto dto = JSON.parseObject(message, UserRelationDto.class);
            WmUser user = wmUserMapper.selectOne(Wrappers.<WmUser>lambdaQuery()
                    .eq(WmUser::getId, dto.getAuthorId()));
            return JSON.toJSONString(user);
        }
        return null;
    }
}
