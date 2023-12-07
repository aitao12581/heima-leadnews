package com.heima.behavior;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.heima.apis")
@EnableKafka
public class BehaviorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BehaviorApplication.class);
    }
}
