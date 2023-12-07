package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.UserConstants;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.admin.pojos.ApUserRealname;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.user.mapper.ApAuthmapper;
import com.heima.user.service.ApAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class ApAuthServiceImpl extends ServiceImpl<ApAuthmapper, ApUserRealname>
implements ApAuthService
{

    /**
     * 分页查询用户审核的数据
     *
     * @param authDto 分页条件
     * @return
     */
    @Override
    public ResponseResult findList(AuthDto authDto) {
        // 判断参数有效
        if (authDto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 检查参数
        authDto.checkParam();
        // 创建分页器
        Page<ApUserRealname> page = new Page<>(authDto.getPage(), authDto.getSize());

        // 查询条件
        LambdaQueryWrapper<ApUserRealname> lqw = new LambdaQueryWrapper<>();
        lqw.eq(authDto.getStatus()!=null, ApUserRealname::getStatus, authDto.getStatus());

        page = page(page, lqw);

        // 封装数据并返回
        ResponseResult result = new PageResponseResult(authDto.getPage(), authDto.getSize(), (int) page.getTotal());
        result.setData(page.getRecords());
        return result;
    }

    /**
     * 审核失败
     *
     * @param dto 审核数据
     * @return
     */
    @Override
    public ResponseResult authFail(AuthDto dto) {
        // 判断参数有效性
        if (dto==null || dto.getId()==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 获取当前信息
        ApUserRealname userRealname = getById(dto.getId());
        if (userRealname == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        // 添加拒绝原因
        userRealname.setReason(dto.getMsg());
        // 修改状态 为审核失败
        userRealname.setStatus(UserConstants.AUTH_AUDITING_ERROR);

        // 修改数据
        updateById(userRealname);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 审核成功
     *
     * @param dto 审核id
     * @return
     */
    @Override
    public ResponseResult authPass(AuthDto dto) {
        // 判断参数有效性
        if (dto==null || dto.getId()==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 获取当前信息
        ApUserRealname userRealname = getById(dto.getId());
        if (userRealname == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        // 修改状态 审核成功
        userRealname.setStatus(UserConstants.AUTH_AUDITING_SUCCESS);

        updateById(userRealname);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
