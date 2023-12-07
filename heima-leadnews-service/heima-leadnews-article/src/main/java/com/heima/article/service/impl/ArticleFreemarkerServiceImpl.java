package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;

@Service
@Slf4j
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private Configuration configuration;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleService apArticleService;

    /**
     * 生成静态文件上传到minIO中
     * @param apArticle app端文件
     * @param content   文章内容
     */
    @Override
    @Async
    public void buildArticleToMinIO(ApArticle apArticle, String content) {
        // 已知文章的id
        // 获取文章内容
        if (StringUtils.isNotBlank(content)) {
            // 通过文章内容使用freemarker生成html文件
            Template template = null;
            StringWriter out = new StringWriter();
            try {
                template = configuration.getTemplate("article.ftl");
                // 数据模型
                HashMap<String, Object> contentDataModel = new HashMap<>();
                contentDataModel.put("content", JSON.parseArray(content));
                // 合成静态文件
                template.process(contentDataModel, out);
            } catch (Exception e) {
                e.printStackTrace();
                log.info("生成静态文件出现异常", e);
            }

            // 将静态文件上传到minio中
            InputStream is = new ByteArrayInputStream(out.toString().getBytes());

            String path = fileStorageService.
                    uploadHtmlFile("", apArticle.getId() + ".html", is);
            // 修改ap_article表，添加静态文件路径
            apArticleService.update(Wrappers.<ApArticle>lambdaUpdate()
                    .eq(ApArticle::getId, apArticle.getId())
                    .set(ApArticle::getStaticUrl, path));

            // 发送消息创建索引
            createArticleEsIndex(apArticle, content, path);
        }
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送消息，创建es库中的索引
     * @param apArticle 文章数据
     * @param content   文章内容
     * @param path  静态文件全路径
     */
    private void createArticleEsIndex(ApArticle apArticle, String content, String path) {
        SearchArticleVo vo = new SearchArticleVo();
        // 将文章信息封装到模型中方便发送
        BeanUtils.copyProperties(apArticle, vo);
        vo.setContent(content);
        vo.setStaticUrl(path);

        // 通过kafka发送消息
        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC, JSON.toJSONString(vo));
    }
}
