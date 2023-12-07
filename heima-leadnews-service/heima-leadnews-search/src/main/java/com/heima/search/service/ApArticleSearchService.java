package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

import java.io.IOException;

/**
 * es 索引库文章数据操作
 */
public interface ApArticleSearchService {

    /**
     * es 索引库文章分页搜索
     * @param dto   查询条件
     * @return  查询结果
     * @throws IOException  io异常
     */
    ResponseResult search(UserSearchDto dto) throws IOException;
}
