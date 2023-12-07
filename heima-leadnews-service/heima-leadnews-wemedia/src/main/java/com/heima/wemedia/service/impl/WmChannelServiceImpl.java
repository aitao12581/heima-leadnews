package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class WmChannelServiceImpl extends ServiceImpl<WmChannelMapper,WmChannel> implements WmChannelService {

    /**
     * 查询所有频道
     *
     * @return
     */
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }

    /**
     * 分页查询频道
     * @return
     */
    @Override
    public ResponseResult getList(ChannelDto dto) {
        if (dto==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 检查参数
        dto.checkParam();

        // 根据分页条件查询数据库数据
        Page<WmChannel> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmChannel> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(dto.getName()),WmChannel::getName, dto.getName());
        lqw.orderByDesc(WmChannel::getCreatedTime);
        page(page, lqw);

        // 封装结果
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(),
                (int) page.getTotal());
        result.setData(page.getRecords());
        return result;
    }

    /**
     * 修改频道信息
     * @param wmChannel 修改后的信息
     * @return
     */
    @Override
    public ResponseResult updateOfData(WmChannel wmChannel) {
        // 检查参数
        if (wmChannel==null || wmChannel.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 修改内容是否存在
        WmChannel channel = getOne(Wrappers.<WmChannel>lambdaQuery()
                .eq(WmChannel::getId, wmChannel.getId()));
        if (channel == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        // 修改参数
        channel.setName(wmChannel.getName());
        if (wmChannel.getDescription()!=null) {
            channel.setDescription(wmChannel.getDescription());
        }
        if (wmChannel.getOrd()!=null) {
            channel.setOrd(wmChannel.getOrd());
        }
        channel.setStatus(wmChannel.getStatus());

        updateById(channel);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 保存数据
     *
     * @param wmChannel 需要保存的数据
     * @return
     */
    @Override
    public ResponseResult saveData(WmChannel wmChannel) {
        // 判断参数有效性
        if (wmChannel == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 补充值
        wmChannel.setIsDefault(true);
        wmChannel.setCreatedTime(new Date());

        // 判断是否有重名
        boolean rs = getDuplicateName(wmChannel.getName());
        if (rs) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "该频道已存在");
        }

        // 保存数据
        save(wmChannel);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 判断是否有重复名称
     * @return
     */
    private boolean getDuplicateName(String name) {
        WmChannel channel = getOne(Wrappers.<WmChannel>lambdaQuery()
                .eq(WmChannel::getName, name));
        return channel != null;
    }

    /**
     * 删除频道数据
     *
     * @param id 频道id
     * @return
     */
    @Override
    public ResponseResult del(Integer id) {
        // 检查参数
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断状态
        WmChannel wmChannel = getById(id);
        if (wmChannel.getStatus()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "该频道正在启用");
        }

        // 根据id删除数据
        removeById(id);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
