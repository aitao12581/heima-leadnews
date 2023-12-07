package com.heima.article.service;

import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface CollectionService {

    /**
     * 收藏文章
     * @param dto   收藏文章的部分信息
     * @return
     */
    ResponseResult collection(CollectionBehaviorDto dto);
}
