package com.heima.wemedia.service;

import java.util.Date;

/**
 * 自媒体提交后将审核的功能提交到任务队列中
 */
public interface WmNewsTaskService {

    /**
     * 添加自媒体文章到延迟队列中
     * @param id    文章id
     * @param publishTime   文章发布时间 可以作为任务的执行时间
     */
    void addNewsToTask(Integer id, Date publishTime);

    /**
     *  消费延迟队列中的任务
     */
    void scanNewsByTask();
}
