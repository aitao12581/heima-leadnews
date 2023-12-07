package com.heima.wemedia.service;

/**
 * 实现自媒体文章内容和图片内容审核的业务层
 */
public interface WmNewsAutoScanService {

    /**
     * 自媒体文章审核
     * @param id 文章id
     */
    void autoScanWmNews(Integer id);
}
