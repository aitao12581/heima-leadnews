package com.heima.minio.test;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.io.FileInputStream;

/**
 * 向minio新创建的桶中上传文件
 */
public class MinIOTest {

    public static void main(String[] args) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("e:/list.html");

            // 创建minio链接客户端对象
            MinioClient client = MinioClient.builder()
                    // 账户 & 密码
                    .credentials("minio", "minio123")
                    // minio服务的请求链接
                    .endpoint("http://192.168.200.130:9000").build();

            // 上传
            PutObjectArgs build = PutObjectArgs.builder()
                    .object("list.html") // 文件名
                    .bucket("leadnews")   // 桶名词 与创建时相同
                    .contentType("text/html")   // 文件类型
                    .stream(fis, fis.available(), -1)  // 文件流
                    .build();
            client.putObject(build);

            System.out.println("http://192.168.200.130:9000/leadnews/ak47.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
