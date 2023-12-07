package com.heima.minio.test;

import com.heima.file.service.FileStorageService;
import com.heima.minio.MinIOApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MinIOApplication.class)
public class MinIOTest2 {

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void testUpdateImgFile() {
        try {
            FileInputStream fileInputStream = new FileInputStream("D:\\Python\\workspace\\WebCrawler\\chap19\\又美又飒 可盐可甜 充斥着少女的迷人风采\\mv.jpg");
            String filePath = fileStorageService.uploadImgFile("", "mv.jpg", fileInputStream);
            System.out.println(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
