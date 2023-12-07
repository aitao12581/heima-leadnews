package com.heima.user.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.heima.user.interceptor.AuthizeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 添加拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthizeInterceptor()).addPathPatterns("/**");
    }

    // mybatis plus分页器
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    // 创建bean 通过ReplyingKafkaTemplate获得消息回复
    @Bean
    public ConcurrentMessageListenerContainer<String, String> repliesContainer(ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {
        ConcurrentMessageListenerContainer<String, String> replies = containerFactory.createContainer("replies");
        replies.getContainerProperties().setGroupId("repliesGroup");
        replies.setAutoStartup(false);
        return replies;
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate (ProducerFactory<String, String> pf, ConcurrentMessageListenerContainer<String, String> repliesContainer) {
        return new ReplyingKafkaTemplate(pf, repliesContainer);
    }

    @Bean
    public KafkaTemplate kafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate(pf);
    }
}
