package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;

public interface ApUserSearchService {

    /**
     *  保存用户搜索记录历史
     * @param keyword   关键字
     * @param userId    用户id
     */
    void insert(String keyword, Integer userId);


    /**
     * 查询搜索历史
     * @return
     */
    ResponseResult findUserSearch();


    /**
     删除搜索历史
     @param dto 搜索历史的id
     @return
     */
    ResponseResult delUserSearch(HistorySearchDto dto);
}
