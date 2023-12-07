package com.heima.admin.service.impl;

import com.heima.admin.service.AuthService;
import com.heima.apis.admin.IAuthClient;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.ADThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private IAuthClient authClient;

    /**
     * 分组查询app端的瀛湖
     *
     * @param authDto 查询条件
     * @return
     */
    @Override
    public ResponseResult list(AuthDto authDto) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return authClient.list(authDto);
    }

    /**
     * 审核失败
     *
     * @param dto 审核数据
     * @return
     */
    @Override
    public ResponseResult authFail(AuthDto dto) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return authClient.authFail(dto);
    }

    /**
     * 审核成功
     *
     * @param dto 审核用户id
     * @return
     */
    @Override
    public ResponseResult authPass(AuthDto dto) {
        return authClient.authPass(dto);
    }
}
