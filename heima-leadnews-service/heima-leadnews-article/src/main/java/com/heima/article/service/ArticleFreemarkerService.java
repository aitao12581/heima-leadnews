package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

/**
 * 根据存储的app端文章信息生成静态页面文件上传到minio中
 */
public interface ArticleFreemarkerService {

    /**
     *  生成静态文件上传到minIO中
     * @param apArticle app端文件
     * @param content 文章内容
     */
    void buildArticleToMinIO(ApArticle apArticle, String content);
}
