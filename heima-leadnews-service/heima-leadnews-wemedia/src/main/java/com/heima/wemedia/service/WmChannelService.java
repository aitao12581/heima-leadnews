package com.heima.wemedia.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;

public interface WmChannelService extends IService<WmChannel> {

    /**
     * 查询所有频道
     * @return
     */
    ResponseResult findAll();

    /**
     * 分页查询频道
     * @param dto
     * @return
     */
    ResponseResult getList(ChannelDto dto);

    /**
     * 修改数据
     * @param wmChannel
     * @return
     */
    ResponseResult updateOfData(WmChannel wmChannel);

    /**
     * 保存数据
     * @param wmChannel 需要保存的数据
     * @return
     */
    ResponseResult saveData(WmChannel wmChannel);

    /**
     * 删除频道数据
     * @param id  频道id
     * @return
     */
    ResponseResult del(Integer id);
}
