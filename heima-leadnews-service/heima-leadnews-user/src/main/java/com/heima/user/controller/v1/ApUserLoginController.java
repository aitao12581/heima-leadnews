package com.heima.user.controller.v1;

import com.heima.model.common.dtos.LoginDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/login")
@Api(value = "app端用户登录", tags = "ap_user", description = "app端用户登录API")
public class ApUserLoginController {

    @Autowired
    private ApUserService apUserService;

    @ApiOperation("app用户登录")
    @PostMapping("/login_auth")
    public ResponseResult login(@RequestBody LoginDto loginDto) {
        return apUserService.login(loginDto);
    }
}
