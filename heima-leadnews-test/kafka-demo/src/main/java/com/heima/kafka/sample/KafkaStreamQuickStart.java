package com.heima.kafka.sample;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * 流式处理
 */
public class KafkaStreamQuickStart {

    public static void main(String[] args) {
        // kafka的核心配置
        Properties pro = new Properties();
        pro.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.200.130:9092");
        // serdes 用于创建序列化和反序列化的工厂
        // 设置键和值的序列化和反序列化配置
        pro.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        pro.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        pro.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-quickstart");

        // stream 构建器
        StreamsBuilder builder = new StreamsBuilder();

        // 进行流式计算
        streamProcessor(builder);

        // 创建kafkaStream对象
        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), pro);

        // 开启流式计算
        kafkaStreams.start();
    }

    /**
     * 流式计算
     *  消息的内容：hello kafka hello itcast
     * @param builder   流式构建器
     */
    private static void streamProcessor(StreamsBuilder builder) {
        // 创建kstream对象，同时指定从哪个topic主题中接收消息
        KStream<String, String> stream = builder.stream("itcast-topic-input");

        // 处理消息的 value 值
        stream.flatMapValues(new ValueMapper<String, Iterable<?>>() {
            @Override
            public Iterable<?> apply(String value) {
                return Arrays.asList(value.split(" "));
            }
        })
                // 按照value进行聚合处理
                .groupBy((key, value) -> value)
                // 时间窗口 创建新的聚合时间窗口实例
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                // 统计单词个数
                .count()
                // 转换为kstream
                .toStream()
                .map((key, value) -> {
                    System.out.println("key:" + key.toString() + ", value:" + value.toString());
                    return new KeyValue<>(key.key().toString(),value.toString());
                })
                // 发送消息
                .to("itcast-topic-out");
    }
}
