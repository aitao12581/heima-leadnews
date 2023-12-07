package com.heima.article.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka配置类 以及初始化流容器
 */
@Getter
@Setter
@Configuration
@EnableKafkaStreams
@ConfigurationProperties(prefix = "kafka")
public class KafkaStreamConfig {
    private static final int MAX_MESSAGE_SIZE = 16 * 1024 * 1024;

    private String hosts;
    private String group;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration defaultKafkaStreamsConfig() {
        Map<String, Object> prop = new HashMap<>();
        prop.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, hosts);
        prop.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getGroup()+"_stream_aid");
        prop.put(StreamsConfig.CLIENT_ID_CONFIG, this.getGroup()+"_stream_cid");
        prop.put(StreamsConfig.RETRIES_CONFIG, 10);
        prop.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        prop.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return new KafkaStreamsConfiguration(prop);
    }
}
