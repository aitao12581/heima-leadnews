package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.pojos.ApArticleConfig;

import java.util.Map;

public interface ApArticleConfigService extends IService<ApArticleConfig> {

    /**
     * 根据kafka中间件传递的消息 map数据修改app端文章配置
     * @param map
     */
    void updateByMap(Map map);
}
