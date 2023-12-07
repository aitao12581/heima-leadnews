package com.heima.apis.admin;

import com.heima.model.admin.dtos.AuthDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("leadnews-user")
public interface IAuthClient {

    @PostMapping("/api/v1/auth/list")
    ResponseResult list(@RequestBody AuthDto authDto);

    @PostMapping("/api/v1/auth/authFail")
    ResponseResult authFail(@RequestBody AuthDto dto);

    @PostMapping("/api/v1/auth/authPass")
    ResponseResult authPass(@RequestBody AuthDto dto);
}
