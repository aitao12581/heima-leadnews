package com.heima.admin.controller.v1;

import com.heima.admin.service.NewAuthService;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
public class NewAuthController {

    @Autowired
    private NewAuthService newAuthService;

    @PostMapping("/list_vo")
    public ResponseResult listVo(@RequestBody NewsAuthDto dto) {
        return newAuthService.listVo(dto);
    }

    @GetMapping("/one_vo/{id}")
    public ResponseResult oneVo(@PathVariable Integer id) {
        return newAuthService.oneVo(id);
    }

    @PostMapping("/auth_fail")
    public ResponseResult authFail(@RequestBody NewsAuthDto dto) {
        return newAuthService.authFail(dto);
    }

    @PostMapping("/auth_pass")
    public ResponseResult authPass(@RequestBody NewsAuthDto dto) {
        return newAuthService.authPass(dto);
    }
}
