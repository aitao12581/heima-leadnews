package com.heima.apis.article.fallback;

import com.heima.apis.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * feign 失败配置
 */
@Component
@Slf4j
public class IArticleClientFallback implements IArticleClient {

    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "获取数据失败");
    }

    @Override
    public void add(Long id, String type) {
        log.info(type+"的数量操作异常");
    }

    @Override
    public void sub(Long id, String type) {
        log.info(type+"的数量操作异常");
    }
}
