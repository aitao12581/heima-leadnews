package com.heima.admin.controller.v1;

import com.heima.admin.service.AdSensitiveService;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmSensitive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensitive")
public class AdSensitiveController {

    @Autowired
    private AdSensitiveService sensitiveService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody SensitiveDto dto) {
        return sensitiveService.list(dto);
    }

    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmSensitive sensitive) {
       return sensitiveService.save(sensitive);
    }

    @DeleteMapping("/del/{id}")
    public ResponseResult del(@PathVariable Integer id) {
        return sensitiveService.del(id);
    }

    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmSensitive sensitive) {
        return sensitiveService.update(sensitive);
    }
}
