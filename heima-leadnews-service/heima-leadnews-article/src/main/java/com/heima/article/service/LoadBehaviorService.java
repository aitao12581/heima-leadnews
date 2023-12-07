package com.heima.article.service;

import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;

public interface LoadBehaviorService {

    /**
     * 文章用户行为回显
     * @param dto   文章Id 作者id
     * @return
     */
    ResponseResult loadBehavior(ArticleInfoDto dto);
}
