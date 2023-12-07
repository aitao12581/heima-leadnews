package com.heima.common.tess4j;

import lombok.Getter;
import lombok.Setter;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tess4j")
public class Tess4jClient {

//    @Value("${tess4j.data-path}")
    private String dataPath;
//    @Value("${tess4j.language}")
    private String language;

    public String doOCR(BufferedImage image) throws TesseractException {
        // 创建tesseract对象
        ITesseract tesseract = new Tesseract();
        // 设置字体库路径
        tesseract.setDatapath(dataPath);
        // 中文识别
        tesseract.setLanguage(language);
        // 执行ocr识别
        String result = tesseract.doOCR(image);
        // 替换回车和tal键  使结果为一行
        result = result.replaceAll("\\r|\\n", "-").replaceAll(" ", "");
        return result;
    }
}
