package com.heima.wemedia.feign;

import com.heima.apis.admin.IAdminClient;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmSensitiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminClient implements IAdminClient {

    @Autowired
    private WmChannelService service;

    /**
     *   频道管理调用
     */
    @Override
    @PostMapping("/api/v1/channel/list")
    public ResponseResult list(@RequestBody ChannelDto channelDto) {
        return service.getList(channelDto);
    }

    @Override
    @PostMapping("/api/v1/channel/save")
    public ResponseResult save(@RequestBody  WmChannel wmChannel) {
        return service.saveData(wmChannel);
    }

    @Override
    @PostMapping("/api/v1/channel/update")
    public ResponseResult update(@RequestBody WmChannel wmChannel) {
        return service.updateOfData(wmChannel);
    }

    @Override
    @GetMapping("/api/v1/channel/del/{id}")
    public ResponseResult del(@PathVariable Integer id) {
        return service.del(id);
    }


    @Autowired
    private WmSensitiveService sensitiveService;

    /**
     *  敏感词管理调用
     */
    @Override
    @PostMapping("/api/v1/sensitive/list")
    public ResponseResult senList(@RequestBody SensitiveDto dto) {
        return sensitiveService.findList(dto);
    }

    @Override
    @PostMapping("/api/v1/sensitive/save")
    public ResponseResult saveSen(@RequestBody WmSensitive sensitive) {
        return sensitiveService.saveSen(sensitive);
    }

    @Override
    @DeleteMapping("/api/v1/sensitive/del/{id}")
    public ResponseResult delSen(@PathVariable Integer id) {
        return sensitiveService.del(id);
    }

    @Override
    @PostMapping("/api/v1/sensitive/update")
    public ResponseResult updateData(@RequestBody WmSensitive sensitive) {
        return sensitiveService.updateData(sensitive);
    }

    @Autowired
    private WmNewsService wmNewsService;

    // 查询所有文章
    @Override
    @PostMapping("/api/v1/news/list_vo")
    public ResponseResult listVo(@RequestBody NewsAuthDto dto) {
        return wmNewsService.listVo(dto);
    }

    // 查询单个文章
    @Override
    @GetMapping("/api/v1/news/one_vo/{id}")
    public ResponseResult oneVo(@PathVariable Integer id) {
        return wmNewsService.oneVo(id);
    }

    // 人工审核失败
    @Override
    @PostMapping("/api/v1/news/auth_fail")
    public ResponseResult authFail(@RequestBody NewsAuthDto dto) {
        return wmNewsService.authFail(dto);
    }

    // 人工审核成功
    @Override
    @PostMapping("/api/v1/news/auth_pass")
    public ResponseResult authPass(@RequestBody NewsAuthDto dto) {
        return wmNewsService.authPass(dto);
    }


    @GetMapping("/api/v1/channel/list")
    @Override
    public ResponseResult getChannels() {
        return service.findAll();
    }
}
