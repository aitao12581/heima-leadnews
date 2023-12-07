package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
@Transactional
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Autowired
    private IScheduleClient scheduleClient;

    /**
     * 添加自媒体文章到延迟队列中
     *
     * @param id          文章id
     * @param publishTime 文章发布时间 可以作为任务的执行时间
     */
    @Override
    @Async
    public void addNewsToTask(Integer id, Date publishTime) {
        log.info("添加服务到延迟服务中----begin");

        // 创建任务对象
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        // 将自媒体文章id封装到WmNews对象中
        WmNews wmNews = new WmNews();
        wmNews.setId(id);

        // 将自媒体文章对象序列化存储到任务的 parameters 属性中
        task.setParameters(ProtostuffUtil.serialize(wmNews));

        // 调用远程服务添加任务
        scheduleClient.addTask(task);

        log.info("添加任务到延迟服务中----end");
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 消费延迟队列中的任务
     */
    @Scheduled(fixedRate = 1000)
    @SneakyThrows
    @Override
    public void scanNewsByTask() {
        log.info("文章审核---消费任务执行---begin");

        ResponseResult result = scheduleClient.poll(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(),
                TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        System.out.println("下拉任务的状态码："+result.getCode());
        if (result.getCode().equals(200) && result.getData()!=null) {
            // 将返回的任务信息转换为对象
//            System.out.println("文章审核开始");
            String json_str = JSON.toJSONString(result.getData());
            Task task = JSON.parseObject(json_str, Task.class);
            byte[] parameters = task.getParameters();
            WmNews wmNews = ProtostuffUtil.deserialize(parameters, WmNews.class);
            System.out.println(wmNews.getId()+"---------------");
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
//            System.out.println("文章审核结束");
        }

        log.info("文章审核---消费任务执行---end---");
    }
}
