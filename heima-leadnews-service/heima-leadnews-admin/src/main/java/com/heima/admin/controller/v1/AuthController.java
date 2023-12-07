package com.heima.admin.controller.v1;

import com.heima.admin.service.AuthService;
import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户审核
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody AuthDto authDto) {
        return authService.list(authDto);
    }

    // 审核失败
    @PostMapping("/authFail")
    public ResponseResult authFail(@RequestBody AuthDto dto) {
        return authService.authFail(dto);
    }

    // 审核成功
    @PostMapping("/authPass")
    public ResponseResult authPass(@RequestBody AuthDto dto) {
        return authService.authPass(dto);
    }
}
