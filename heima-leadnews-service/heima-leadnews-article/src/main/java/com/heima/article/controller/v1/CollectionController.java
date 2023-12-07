package com.heima.article.controller.v1;

import com.heima.article.service.CollectionService;
import com.heima.model.article.dtos.CollectionBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章收藏功能
 */
@RestController
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @PostMapping("/api/v1/collection_behavior")
    public ResponseResult collection(@RequestBody CollectionBehaviorDto dto) {
        return collectionService.collection(dto);
    }
}
