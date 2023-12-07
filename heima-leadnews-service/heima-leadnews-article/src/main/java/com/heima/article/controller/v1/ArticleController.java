package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article")
public class ArticleController {

    @Autowired
    private ApArticleService service;

    /**
     * 加载首页
     * @param articleHomeDto
     * @return
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto articleHomeDto) {
//        return service.load(ArticleConstants.LOADTYPE_LOAD_MORE, articleHomeDto);
        return service.load2(articleHomeDto, ArticleConstants.LOADTYPE_LOAD_MORE, true);
    }

    @PostMapping("/loadmore")
    public ResponseResult loadmore(@RequestBody ArticleHomeDto articleHomeDto) {
        return service.load(ArticleConstants.LOADTYPE_LOAD_MORE, articleHomeDto);
    }

    @PostMapping("/loadnew")
    public ResponseResult loadnew(@RequestBody ArticleHomeDto articleHomeDto) {
        return service.load(ArticleConstants.LOADTYPE_LOAD_NEW, articleHomeDto);
    }


}
