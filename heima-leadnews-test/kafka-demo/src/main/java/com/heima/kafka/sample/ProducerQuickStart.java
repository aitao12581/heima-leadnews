package com.heima.kafka.sample;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * 主题生产者
 */
public class ProducerQuickStart {

    public static void main(String[] args) {
        // 1.kafka的配置信息
        Properties properties = new Properties();
        // 2.kafka的连接地址
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.200.130:9092");
        // 3.发送失败，失败的重试次数
        properties.put(ProducerConfig.RETRIES_CONFIG, 5);
        // 消息key的序列化器
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        // 消息value的序列化器
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        // 2.生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // 封装发送的消息
        ProducerRecord<String,String> record = new ProducerRecord<>("itcast-topic-input",
                "10001", "hello kafka");

        // 3.发送的消息
        producer.send(record);

        System.out.println("发送成功");
        // 4.关闭消息通道，否则无法发送消息
        producer.close();
    }
}
