package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmSensitiveServiceImpl extends ServiceImpl<WmSensitiveMapper, WmSensitive>
implements WmSensitiveService
{

    /**
     * 分页 查询所有敏感词
     *
     * @param dto 条件
     * @return
     */
    @Override
    public ResponseResult findList(SensitiveDto dto) {
        // 判断参数是否有效
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 检查参数
        dto.checkParam();

        // 分页查询
        Page<WmSensitive> pageInfo = new Page<>(dto.getPage(), dto.getSize());
        // 设置筛选条件
        LambdaQueryWrapper<WmSensitive> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(dto.getName()),
                WmSensitive::getSensitives,
                dto.getName());
        lqw.orderByDesc(WmSensitive::getCreatedTime);
        // 查询数据
        pageInfo = page(pageInfo, lqw);

        System.out.println("总条目数为：" + pageInfo.getTotal());
        // 封装数据
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), (int) pageInfo.getTotal());
        List<WmSensitive> records = pageInfo.getRecords();
//        // 将日期转换为时间戳
        List<Map<String, Object>> list = records.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("sensitives", item.getSensitives());
            map.put("createdTime", item.getCreatedTime().getTime());
            return map;
        }).collect(Collectors.toList());
        result.setData(list);
        return result;
    }

    /**
     * 保存数据
     * @param sensitive 保存的内容
     * @return
     */
    @Override
    public ResponseResult saveSen(WmSensitive sensitive) {
        // 判断参数的有效性
        if (sensitive == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 补全数据
        sensitive.setCreatedTime(new Date());

        save(sensitive);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除敏感词
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult del(Integer id) {
        // 检查参数
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        removeById(id);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 修改敏感词
     *
     * @param sensitive 敏感词信息
     * @return
     */
    @Override
    public ResponseResult updateData(WmSensitive sensitive) {
        // 判断参数有效性
        if (sensitive==null || sensitive.getId()==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 获取当前id的信息
        WmSensitive wm = getById(sensitive.getId());
        if (wm==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        wm.setSensitives(sensitive.getSensitives());

        updateById(wm);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
