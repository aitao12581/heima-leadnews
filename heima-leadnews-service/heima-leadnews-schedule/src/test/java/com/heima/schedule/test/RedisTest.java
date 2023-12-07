package com.heima.schedule.test;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Nullable;
import javax.xml.crypto.Data;
import java.util.Date;
import java.util.Set;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private CacheService cacheService;

    @Test
    public void testList() {
        // 向list的左边添加元素
//        cacheService.lLeftPush("list_001", "hello, redis");

        // 从list的右边获取元素，并删除
        String value = cacheService.lRightPop("list_001");
        System.out.println(value);
    }

    @Test
    public void testZset() {
        // 添加数据到zset当中 分值
        /*cacheService.zAdd("zset_key_001", "hello zset 001", 1000);
        cacheService.zAdd("zset_key_001", "hello zset 002", 8888);
        cacheService.zAdd("zset_key_001", "hello zset 003", 7777);
        cacheService.zAdd("zset_key_001", "hello zset 004", 999999);*/
        // 按照分值获取数据
        Set<String> set = cacheService.zRangeByScore("zset_key_001", 0, 8888);
        System.out.println(set);
    }


    @Autowired
    private TaskService taskService;

    @Test
    public void testAddTask() {
        Task task = new Task();

        task.setTaskId(2L);
        task.setTaskType(2);
        task.setPriority(2);
        task.setParameters("成功".getBytes());
        task.setExecuteTime(System.currentTimeMillis()+1000*60*3);

        taskService.addTask(task);
        System.out.println("任务添加完成");
    }

    @Test
    public void testCancle() {
        taskService.cancelTask(1L);
        System.out.println("任务取消完成");
    }

    @Test
    public void testKeys(){
//        Set<String> keys = cacheService.keys("future_*");
//        System.out.println(keys);
//
//        Set<String> scan = cacheService.scan("future_*");
//        System.out.println(scan);
        Set<String> scan = cacheService.scan(ScheduleConstants.FUTURE + "*");
        System.out.println(scan);
    }

    // 普通方法向redis中存入数据
    // 耗时 6285 ms
    @Test
    public void testPipel() {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            Task task = new Task();
            task.setTaskType(1001);
            task.setPriority(1);
            task.setExecuteTime(new Date().getTime());
            cacheService.lLeftPush("1001_1", JSON.toJSONString(task));
        }

        System.out.println("耗时"+(System.currentTimeMillis()- start));
    }

    // 使用管道技术向redis中存储数据
    // 耗时 1508 ms
    @Test
    public void testPipel2() {
        long start = System.currentTimeMillis();

        // 使用管道技术
        cacheService.getstringRedisTemplate().executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (int i = 0; i < 10000; i++) {
                    Task task = new Task();
                    task.setTaskType(1001);
                    task.setPriority(1);
                    task.setExecuteTime(new Date().getTime());
                    connection.lPush("1001_1".getBytes(), JSON.toJSONString(task).getBytes());
                }
                return null;
            }
        });
        System.out.println("使用管道技术执行10000次自增操作共耗时:"+(System.currentTimeMillis()-start)+"毫秒");
    }
}
