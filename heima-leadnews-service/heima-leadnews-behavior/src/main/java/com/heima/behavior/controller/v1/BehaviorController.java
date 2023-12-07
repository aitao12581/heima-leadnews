package com.heima.behavior.controller.v1;

import com.heima.behavior.service.BehaviorService;
import com.heima.model.behaviior.dtos.LikesBehaviorDto;
import com.heima.model.behaviior.dtos.ReadBehaviorDto;
import com.heima.model.behaviior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BehaviorController {

    @Autowired
    private BehaviorService service;

    @PostMapping("/likes_behavior")
    public ResponseResult likesBehavior(@RequestBody LikesBehaviorDto dto) {
        return service.likesBehavior(dto);
    }

    @PostMapping("/read_behavior")
    public ResponseResult readBehavior(@RequestBody ReadBehaviorDto dto) {
        return service.readBehavior(dto);
    }

    @PostMapping("/un_likes_behavior")
    public ResponseResult unLikesBehavior(@RequestBody UnLikesBehaviorDto dto) {
        return service.unLikesBehavior(dto);
    }
}
