package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.LoginDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.pojos.ApUser;

public interface ApUserService extends IService<ApUser> {

    /**
     * app端登录
     */
    ResponseResult login(LoginDto dto);
}
