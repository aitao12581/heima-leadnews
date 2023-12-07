package com.heima.kafka.sample;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import javax.xml.crypto.Data;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * 消息消费者
 */
public class ConsumerQuickStart {

    public static void main(String[] args) {
        // 添加kafka配置
        Properties properties = new Properties();
        // 添加连接地址
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.200.130:9092");
        // 消费者组
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "group2");
        // 消息的反序列化
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");

        // 创建kafka消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // 订阅主题
        consumer.subscribe(Collections.singletonList("itcast-topic-out"));
        System.out.println("开始接收消息");
        // 当前线程一直处于监听状态
        try {
            // 同步加异步方式提交偏移量
            while (true) {
                ConsumerRecords<String, String> poll = consumer.poll(Duration.ofMillis(1000));
//                System.out.println("接收消息。。。"+new Date()+poll);
                for (ConsumerRecord<String, String> record : poll) {
                    System.out.println(record.key());
                    System.out.println(record.value());
                    System.out.println("----------------");
//                try {
//                    // 同步提交偏移量
//                    consumer.commitSync();
//                } catch (Exception e) {
//                    System.out.println("记录提交失败的异常："+e);
//                }
                    // 异步提交偏移量
//                consumer.commitAsync(new OffsetCommitCallback() {
//                    @Override
//                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
//                        if (exception!=null) {
//                            System.out.println("记录错误的提交偏移量："+ offsets+",异常信息"+exception);
//                        }
//                    }
//                });
                }
                // 先异步提交
                consumer.commitAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("记录错误日志："+e);
        } finally {
            try {
                // 无论异步提交是否成功，都会去执行同步提交
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }

    }
}
