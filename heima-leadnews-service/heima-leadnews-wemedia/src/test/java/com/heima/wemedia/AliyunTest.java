package com.heima.wemedia;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WemediaApplication.class)
public class AliyunTest {

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void testTestScan() throws Exception {
        Map map = greenTextScan.greeTextScan("我是一个好人，冰毒");
        System.out.println(map);
    }

    @Test
    public void testImgScan() throws Exception {
        byte[] files = fileStorageService.downLoadFile("http://192.168.200.130:9000/leadnews" +
                "/2023/11/24/3f2b688436624312a4d13f2602da340b.jpeg");
        Map map = greenImageScan.imageScan(Arrays.asList(files));
        System.out.println(map);
    }
}
