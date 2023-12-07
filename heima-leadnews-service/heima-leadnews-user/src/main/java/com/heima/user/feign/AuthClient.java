package com.heima.user.feign;

import com.heima.apis.admin.IAuthClient;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.service.ApAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthClient implements IAuthClient {

    @Autowired
    private ApAuthService authService;

    @Override
    @PostMapping("/api/v1/auth/list")
    public ResponseResult list(@RequestBody AuthDto authDto) {
        return authService.findList(authDto);
    }

    @Override
    @PostMapping("/api/v1/auth/authFail")
    public ResponseResult authFail(@RequestBody AuthDto dto) {
        return authService.authFail(dto);
    }

    @Override
    @PostMapping("/api/v1/auth/authPass")
    public ResponseResult authPass(@RequestBody AuthDto dto) {
        return authService.authPass(dto);
    }
}
