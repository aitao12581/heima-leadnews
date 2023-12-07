package com.heima.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@ConfigurationProperties(prefix = "minio")  // 文件上传 配置前缀file.oss
public class MinIOConfigProperties implements Serializable {
    private String accessKey;   // 账号
    private String secretKey;   // 密码
    private String bucket;  // 桶名
    private String endpoint;    // 请求路径
    private String readPath;    // 文件路径
}
