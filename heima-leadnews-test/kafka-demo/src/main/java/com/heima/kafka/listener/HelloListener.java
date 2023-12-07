package com.heima.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.entity.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HelloListener {

    @KafkaListener(topics = "doyens-topic")
    public void onMessage(String message) {
        if (!StringUtils.isEmpty(message)) {
            // 接收消息后先将字符串解析为对象
            User user = JSON.parseObject(message, User.class);
            System.out.println(user);
        }
    }
}
