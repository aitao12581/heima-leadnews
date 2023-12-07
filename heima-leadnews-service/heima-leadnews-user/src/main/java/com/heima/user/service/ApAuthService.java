package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.admin.pojos.ApUserRealname;
import com.heima.model.common.dtos.ResponseResult;

public interface ApAuthService extends IService<ApUserRealname> {

    /**
     * 分页查询用户审核的数据
     * @param authDto   分页条件
     * @return
     */
    ResponseResult findList(AuthDto authDto);

    /**
     * 审核失败
     * @param dto   审核数据
     * @return
     */
    ResponseResult authFail(AuthDto dto);

    /**
     * 审核成功
     * @param dto   审核id
     * @return
     */
    ResponseResult authPass(AuthDto dto);
}
