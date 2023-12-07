package com.heima.article.controller.v1;

import com.heima.article.service.LoadBehaviorService;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article")
public class LoadBehaviorController {

    @Autowired
    private LoadBehaviorService service;

    @PostMapping("/load_article_behavior")
    public ResponseResult loadBehavior(@RequestBody ArticleInfoDto dto) {
        return service.loadBehavior(dto);
    }
}
