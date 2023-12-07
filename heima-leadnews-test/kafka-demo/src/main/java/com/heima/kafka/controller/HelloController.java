package com.heima.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 消息生产者
 */
@RestController
public class HelloController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/hello")
    public String hello() {
        User user = new User();
        user.setUsername("黑山老登山");
        user.setAge(40);
        user.setSex("?");

        // 发送消息时（对象）先将对象转换为json字符串
        kafkaTemplate.send("doyens-topic", JSON.toJSONString(user));
        return "ok";
    }
}
