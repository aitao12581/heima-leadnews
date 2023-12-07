package com.heima.wemedia;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class Tess4jTest {


    @Test
    public void testTess4j() {
        try {
            // 获取本地图片
            File file = new File("D:\\桌面\\4SvIedib8MRqXDSSnbic6PibmqIlib58EtzibL1Nx6wQtxe9g38RTEfZnb36kw8qRh4iaP15AwGGbf9icW0aY1FNupDtw.jpeg");
            // 创建Tesseract对象
            ITesseract tesseract = new Tesseract();
            // 设置字体库路径
            tesseract.setDatapath("E:\\workspace\\tessdata");
            // 设置中文识别
            tesseract.setLanguage("chi_sim");
            // 设置Ocr识别
            String result = tesseract.doOCR(file);

            // 替换回车和tal键  使结果为一行
            result = result.replaceAll("\\r|\\n", "-").replaceAll(" ", "");
            System.out.println("识别的结果为："+result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
