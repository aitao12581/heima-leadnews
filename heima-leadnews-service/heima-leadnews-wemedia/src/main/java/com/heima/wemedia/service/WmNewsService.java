package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsEnableDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {

    /**
     * 查询所以频道数据
     * @param dto   查询条件
     * @return
     */
    ResponseResult findAll(WmNewsPageReqDto dto);

    /**
     * 发布文章或保存草稿
     * @param dto   修改后的内容
     * @return
     */
    ResponseResult submitNews(WmNewsDto dto);

    /**
     * 删除文章
     * @param newId 文章的id
     * @return
     */
    ResponseResult delNews(Integer newId);

    /**
     *  获取当前id下的文章数据
     * @param newId 文章id
     * @return
     */
    ResponseResult getOne(Integer newId);

    /**
     * 根据自媒体文章id 修改后的状态 空值文章上下架
     * @param dto 数据模型 id 和 状态
     * @return  是否成功
     */
    ResponseResult downOrUp(NewsEnableDto dto);

    /**
     * 为后台管理提供自媒体文章信息
     * @param dto   条件
     * @return
     */
    ResponseResult listVo(NewsAuthDto dto);

    /**
     * 查询单个用户数据
     * @param id    文章id
     * @return
     */
    ResponseResult oneVo(Integer id);

    /**
     * 人工审核失败
     * @param dto id  状态  原因
     * @return
     */
    ResponseResult authFail(NewsAuthDto dto);

    /**
     * 人工审核成功
     * @param dto id 状态
     * @return
     */
    ResponseResult authPass(NewsAuthDto dto);
}
