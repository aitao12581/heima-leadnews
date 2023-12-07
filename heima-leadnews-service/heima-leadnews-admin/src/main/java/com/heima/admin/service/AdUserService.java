package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.AdUserDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;

public interface AdUserService extends IService<AdUser> {

    /**
     *  登录后台管理
     * @param dto   用户登录的账户密码
     * @return
     */
    ResponseResult login(AdUserDto dto);
}
