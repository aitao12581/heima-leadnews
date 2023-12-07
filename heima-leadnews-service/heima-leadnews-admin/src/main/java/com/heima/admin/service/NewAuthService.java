package com.heima.admin.service;

import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;

public interface NewAuthService {

    /**
     * 查询所有文章信息
     * @param dto 条件
     * @return
     */
    ResponseResult listVo(NewsAuthDto dto);

    /**
     * 查看单个文章详情
     * @param id    文章id
     * @return
     */
    ResponseResult oneVo(Integer id);

    /**
     * 人工审核成功
     * @param dto   id 和 状态
     * @return
     */
    ResponseResult authFail(NewsAuthDto dto);

    /**
     * 人工审核失败
     * @param dto   id 和 状态
     * @return
     */
    ResponseResult authPass(NewsAuthDto dto);
}
