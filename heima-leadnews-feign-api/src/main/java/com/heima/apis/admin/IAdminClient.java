package com.heima.apis.admin;

import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmSensitive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("leadnews-wemedia")
public interface IAdminClient {

    @PostMapping("/api/v1/channel/list")
    ResponseResult list(@RequestBody ChannelDto channelDto);

    @PostMapping("/api/v1/channel/save")
    ResponseResult save(@RequestBody WmChannel wmChannel);

    @PostMapping("/api/v1/channel/update")
    ResponseResult update(@RequestBody WmChannel wmChannel);

    @GetMapping("/api/v1/channel/del/{id}")
    ResponseResult del(@PathVariable Integer id);

    @PostMapping("/api/v1/sensitive/list")
    ResponseResult senList(@RequestBody SensitiveDto dto);

    @PostMapping("/api/v1/sensitive/save")
    ResponseResult saveSen(@RequestBody WmSensitive sensitive);

    @DeleteMapping("/api/v1/sensitive/del/{id}")
    ResponseResult delSen(@PathVariable Integer id);

    @PostMapping("/api/v1/sensitive/update")
    ResponseResult updateData(@RequestBody WmSensitive sensitive);

    @PostMapping("/api/v1/news/list_vo")
    ResponseResult listVo(@RequestBody NewsAuthDto dto);

    @GetMapping("/api/v1/news/one_vo/{id}")
    ResponseResult oneVo(@PathVariable Integer id);

    @PostMapping("/api/v1/news/auth_fail")
    ResponseResult authFail(@RequestBody NewsAuthDto dto);

    @PostMapping("/api/v1/news/auth_pass")
    ResponseResult authPass(@RequestBody NewsAuthDto dto);

    @GetMapping("/api/v1/channel/list")
    ResponseResult getChannels();
}
