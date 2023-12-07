package com.heima.file.service.impl;

import com.heima.file.config.MinIOConfig;
import com.heima.file.config.MinIOConfigProperties;
import com.heima.file.service.FileStorageService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@EnableConfigurationProperties(MinIOConfigProperties.class)
@Import(MinIOConfig.class)
public class MinIOFileStorageService implements FileStorageService {

    @Autowired
    private MinioClient client;

    @Autowired
    private MinIOConfigProperties properties;

    private final static String separator = "/";

    /**
     * @param dirPath   文件前缀 文件目录
     * @param filename  文件名称
     * @return  文件全路径
     */
    public String buildFilePath(String dirPath, String filename) {
        // 定义初始容量
        StringBuilder sb = new StringBuilder(50);
        if (!StringUtils.isEmpty(dirPath)) {
            sb.append(dirPath).append(separator);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        sb.append(todayStr).append(separator).append(filename);
        return sb.toString();
    }

    /**
     * 上传图片文件
     *
     * @param prefix      文件前缀
     * @param filename    文件名
     * @param inputStream 文件输入流
     * @return 文件全路径
     */
    @Override
    public String uploadImgFile(String prefix, String filename, InputStream inputStream) {
        String filePath = buildFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .contentType("image/jpg")
                    .object(filePath)
                    .bucket(properties.getBucket())
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
            client.putObject(putObjectArgs);
            StringBuilder sb = new StringBuilder(properties.getReadPath());
            sb.append(separator+properties.getBucket());
            sb.append(separator);
            sb.append(filePath);
            return sb.toString();
        } catch (Exception e) {
            log.error("minio put file error：",e);
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 上传html文件
     *
     * @param prefix      文件前缀
     * @param filename    文件名
     * @param inputStream 文件输入流
     * @return 文件全路径
     */
    @Override
    public String uploadHtmlFile(String prefix, String filename, InputStream inputStream) {
        String filePath = buildFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .contentType("text/html")
                    .object(filePath)
                    .bucket(properties.getBucket())
                    .stream(inputStream, inputStream.available(), -1)
                    .build();
            client.putObject(putObjectArgs);
            StringBuilder sb = new StringBuilder(properties.getReadPath());
            sb.append(separator+properties.getBucket());
            sb.append(separator);
            sb.append(filePath);
            return sb.toString();
        } catch (Exception e) {
            log.error("minio put file error：",e);
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 删除文件
     *
     * @param pathUrl 文件全路径
     */
    @Override
    public void delete(String pathUrl) {
        String key = pathUrl.replace(properties.getEndpoint()+"/", "");
        int index = key.indexOf("/");
        String bucket = key.substring(0, index);
        String filePath = key.substring(index+1);
        // 删除objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(filePath)
                .build();
        try {
            client.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error：", e);
            throw new RuntimeException("删除文件失败");
        }
    }

    /**
     * 下载文件
     *
     * @param pathUrl 文件全路径
     * @return
     */
    @Override
    public byte[] downLoadFile(String pathUrl) {
        String key = pathUrl.replace(properties.getEndpoint()+"/", "");
        int index = key.indexOf("/");
        String bucket = key.substring(0, index);
        String filePath = key.substring(index+1);
        InputStream is = null;
        try {
            // 获取桶中存储的文件
            is = client.getObject(GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filePath).build());
        } catch (Exception e) {
            log.error("minio download file error, pathUrl:{}", pathUrl);
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (!((rc = is.read(buff, 0, 100))>0)) break;
            } catch (Exception e) {
                e.printStackTrace();
            }
            baos.write(buff, 0, rc);
        }
        return baos.toByteArray();
    }
}
