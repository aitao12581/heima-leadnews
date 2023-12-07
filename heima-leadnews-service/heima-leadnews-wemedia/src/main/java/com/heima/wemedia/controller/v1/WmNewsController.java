package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.NewsEnableDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService service;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmNewsPageReqDto dto) {
        return service.findAll(dto);
    }

    @PostMapping("/submit")
    public ResponseResult submit(@RequestBody WmNewsDto dto) {
        return service.submitNews(dto);
    }

    @GetMapping("/del_news/{newId}")
    public ResponseResult delNews(@PathVariable Integer newId) {
        return service.delNews(newId);
    }
    
    @GetMapping("/one/{newId}")
    public ResponseResult getOne(@PathVariable Integer newId) {
        return service.getOne(newId);
    }

    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody NewsEnableDto dto) {
        return service.downOrUp(dto);
    }
}
