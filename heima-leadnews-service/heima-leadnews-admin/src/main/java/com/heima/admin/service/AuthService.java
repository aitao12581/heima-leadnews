package com.heima.admin.service;

import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.ResponseResult;

public interface AuthService {

    /**
     * 分组查询app端的瀛湖
     * @param authDto   查询条件
     * @return
     */
    ResponseResult list(AuthDto authDto);

    /**
     * 审核失败
     * @param dto 审核数据
     * @return
     */
    ResponseResult authFail(AuthDto dto);

    /**
     * 审核成功
     * @param dto   审核用户id
     * @return
     */
    ResponseResult authPass(AuthDto dto);
}
