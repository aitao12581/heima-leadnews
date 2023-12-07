package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    /**
     * 添加任务也
     *
     * @param task 任务对象
     * @return 任务id
     */
    @Override
    public long addTask(Task task) {
        // 添加任务到数据库中
        boolean success = addtaskToDb(task);

        if (success) {
            addtaskCache(task);
        }

        return task.getTaskId();
    }

    @Autowired
    private CacheService cacheService;

    /**
     * 将任务信息添加到redis
     * @param task  添加内容
     */
    private void addtaskCache(Task task) {
        String key = task.getTaskType()+"_"+task.getPriority();

        // 获取5分钟之后的毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();

        // 如果任务执行时间小于当前时间，存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= nextScheduleTime) {
            // 如果任务的执行时间大于当前时间 && 小于等于预设时间（未来时间5分钟）则存入zset
            cacheService.zAdd(ScheduleConstants.FUTURE+key, JSON.toJSONString(task),
                    task.getExecuteTime());
        }
    }

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    /**
     * 将任务信息添加到mysql数据库
     * @param task  任务模型
     * @return  添加状态
     */
    private boolean addtaskToDb(Task task) {
        boolean flag = false;

        try {
            // 保存任务表
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            // 设置taskId
            task.setTaskId(taskinfo.getTaskId());

            // 保存任务日志数据
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo, taskinfoLogs);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);

            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


    /**
     * 取消任务
     * @param taskId 任务id
     * @return 取消结果
     */
    @Override
    public boolean cancelTask(long taskId) {
        // 定义标记
        boolean flag = false;

        // 删除任务，更新日志
        Task task = updateDb(taskId, ScheduleConstants.CANCELLED);

        // 删除redis任务
        if (task!=null) {
            removeTaskCache(task);
            flag = true;
        }

        return flag;
    }

    /**
     * 删除redis中存储的数据
     * @param task 任务
     */
    private void removeTaskCache(Task task) {
        // 拼接键名
        String key = task.getTaskType()+"_"+task.getPriority();

        // 判断任务执行时间是否小于当前时间
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(ScheduleConstants.TOPIC+key,
                    0, JSON.toJSONString(task));
        } else {
            // 删除有序集合中存储的未来任务
            cacheService.zRemove(ScheduleConstants.FUTURE+key,
                    JSON.toJSONString(task));
        }

    }

    /**
     * 删除任务，更新任务日志
     * @param taskId 任务id
     * @param status  已执行状态
     * @return
     */
    private Task updateDb(long taskId, int status) {
        Task task = null;

        try {
            // 删除任务
            taskinfoMapper.deleteById(taskId);

            // 修改日志状态
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            // 将日志内容存入task对象并返回
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (Exception e) {
            log.info("task cancel exception taskId={}", taskId);
        }

        return task;
    }


    /**
     * 按照类型和优先级拉取任务
     *
     * @param type     类型
     * @param priority 优先级
     * @return 拉取到的任务
     */
    @Override
    public Task poll(int type, int priority) {
        Task task = null;

        try {
            // 获取数据库中的部分名称
            String key = type+"_"+priority;
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC+key);
            if (StringUtils.isNotBlank(task_json)) {
                task = JSON.parseObject(task_json, Task.class);
                // 更新数据库中的数据
                // 删除任务数据， 更新日志的状态
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("poll task error ...");
        }

        return task;
    }

    /**
     * 未来数据定时刷新
     */
    @Override
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh() {
//        System.out.println(System.currentTimeMillis()/1000+"执行了定时任务");
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if (StringUtils.isNotBlank(token)) {
            log.info("未来数据定时刷新---定时任务 {}", token);

            // 获取所有未来数据集合的key值
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*"); // future_*
            for (String futureKey : futureKeys) {  // future_250_250
                // 将未来任务的键值拼接为当时任务
                String topic = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
                // 获取当前key下需要消费的任务数据
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
                if (!tasks.isEmpty()) {
                    // 将这些任务数据添加到消费者队列中
                    cacheService.refreshWithPipeline(futureKey, topic, tasks);
                    System.out.println("成功将" + futureKey + "下的当前需要执行的任务数据刷新到" + topic + "下");
                }
            }
        }
    }

    /**
     * mysql数据库定时同步数据到redis
     */
    @Override
    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct
    public void reloadData() {
        clearCache();
        log.info("数据库数据同步到缓存");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);

        // 查看所有小于未来5分钟的任务
        List<Taskinfo> taskinfos = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery()
                .lt(Taskinfo::getExecuteTime, calendar.getTime()));
        // 遍历所有任务
        if (taskinfos!=null && taskinfos.size()>0) {
            for (Taskinfo taskinfo : taskinfos) {
                // 将所有任务封装到Task类中，通过方法将task存储到redis中
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addtaskCache(task);
            }
        }
    }

    /**
     * 删除redis多有任务队列
     */
    private void clearCache() {
        // 删除缓存中所有的消费者队列和未来任务数据集合的key
        Set<String> topickeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futurekeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        cacheService.delete(topickeys);
        cacheService.delete(futurekeys);
    }
}
