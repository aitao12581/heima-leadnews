package com.heima.article.test;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.ArticleApplication;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ArticleApplication.class)
public class ArticleFreemarkerTest {

    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleMapper articleMapper;

    @Autowired
    private ApArticleContentMapper contentMapper;

    @Test
    public void createStaticUrlTest() throws Exception {
        // 获取文章内容
        ApArticleContent apArticleContent = contentMapper.selectOne(
                Wrappers.<ApArticleContent>lambdaQuery()
                .eq(ApArticleContent::getArticleId, "1729020669041184770L"));
        // 判断是否存在
        if (apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())) {
            // 文章内容 通过freemarker 模板生成器 生成 html 文件
            StringWriter out = new StringWriter();
            Template template = configuration.getTemplate("article.ftl");

            Map<String, Object> param = new HashMap<>();
            param.put("content", JSONArray.parseArray(apArticleContent.getContent()));

            template.process(param, out);

            ByteArrayInputStream bais = new ByteArrayInputStream(out.toString().getBytes());

            // 将文件上传至minio
            String path = fileStorageService.uploadHtmlFile("",
                    apArticleContent.getArticleId()+".html",
                    bais);
            // 修改ap_article表，保存static_url字段
            ApArticle apArticle = new ApArticle();
            apArticle.setId(apArticleContent.getArticleId());
            apArticle.setStaticUrl(path);
            articleMapper.updateById(apArticle);
        }
    }
}
