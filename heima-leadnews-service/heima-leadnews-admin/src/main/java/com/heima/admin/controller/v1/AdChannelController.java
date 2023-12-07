package com.heima.admin.controller.v1;

import com.heima.admin.service.AdChannelService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class AdChannelController {

    @Autowired
    private AdChannelService adChannelService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody ChannelDto dto) {
        return adChannelService.list(dto);
    }

    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmChannel wmChannel) {
        return adChannelService.save(wmChannel);
    }

    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmChannel wmChannel) {
        return adChannelService.update(wmChannel);
    }

    @GetMapping("/del/{id}")
    public ResponseResult del(@PathVariable Integer id) {
        return adChannelService.del(id);
    }
}
