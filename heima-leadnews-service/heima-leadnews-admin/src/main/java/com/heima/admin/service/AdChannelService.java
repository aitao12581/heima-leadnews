package com.heima.admin.service;

import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;

/**
 * 后台员工 频道管理
 */
public interface AdChannelService {

    /**
     * 分页查询频道数据
     * @param dto 条件
     * @return
     */
    ResponseResult list(ChannelDto dto);

    /**
     * 新增频道信息
     * @param wmChannel 频道信息
     * @return
     */
    ResponseResult save(WmChannel wmChannel);

    /**
     * 修改频道信息
     * @param wmChannel 修改后的内容
     * @return
     */
    ResponseResult update(WmChannel wmChannel);

    /**
     * 删除频道信息
     * @param id    要删除的频道id
     * @return
     */
    ResponseResult del(Integer id);
}
