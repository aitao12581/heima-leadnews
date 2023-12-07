package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

/**
 * 对外访问的接口
 */
public interface TaskService {

    /**
     * 添加任务也
     * @param task  任务对象
     * @return  任务id
     */
    long addTask(Task task);


    /**
     * 取消任务
     * @param taskId        任务id
     * @return              取消结果
     */
    boolean cancelTask(long taskId);


    /**
     * 按照类型和优先级拉取任务
     * @param type  类型
     * @param priority 优先级
     * @return 拉取到的任务
     */
    Task poll(int type, int priority);

    /**
     * 未来数据定时刷新
     */
    void refresh();

    /**
     * mysql数据库定时同步数据到redis
     */
    void reloadData();
}
