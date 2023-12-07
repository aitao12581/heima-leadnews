package com.heima.article.feign;

import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.impl.CollectionServiceImpl;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article")
public class ArticleClient implements IArticleClient {

    @Autowired
    private ApArticleService service;

    @Override
    @PostMapping("/save")
    public ResponseResult saveArticle(ArticleDto dto) {
        return service.saveArticle(dto);
    }

    @Autowired
    private CollectionServiceImpl collectionService;

    @GetMapping("/add")
    public void add(@RequestParam("id") Long id,  @RequestParam("type") String type) {
        collectionService.addArticleToDb(id, type);
    }

    @GetMapping("/sub")
    public void sub(@RequestParam("id") Long id, @RequestParam("type") String type) {
        collectionService.subArticleToDb(id, type);
    }
}
