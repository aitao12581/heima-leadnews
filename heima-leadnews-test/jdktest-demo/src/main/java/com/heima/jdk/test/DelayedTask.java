package com.heima.jdk.test;

import java.util.Calendar;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *  测试Jdk下delayQueue队列的延迟任务
 * 1：在测试包jdk下创建延迟任务元素对象DelayedTask，实现compareTo和getDelay方法，
 * 2：在main方法中创建DelayQueue并向延迟队列中添加三个延迟任务，
 * 3：循环的从延迟队列中拉取任务
 */
public class DelayedTask implements Delayed {

    // 任务的执行时间
    private int executeTime = 0;

    public DelayedTask(int delay) {
        Calendar calendar = Calendar.getInstance();
        // 将传入的秒数与当前时间的秒数相加
        calendar.add(Calendar.SECOND, delay);
        // 获取时间的毫秒值，计算秒值 赋值到属性
        this.executeTime = (int) (calendar.getTimeInMillis() / 1000);
    }

    /**
     * 元素在队列中的剩余时间
     * @param unit the time unit 时间单位
     * @return 剩余时间
     */
    @Override
    public long getDelay(TimeUnit unit) {
        // 获取当前时间
        Calendar calendar = Calendar.getInstance();
        // 返回 延迟时间 - 当前时间 计算剩余时间
        return executeTime-(calendar.getTimeInMillis()/1000) ;
    }

    /**
     * 元素排序，添加两个以上的数据会执行排序规则来判断破排序
     * @param o 需要排序的对象
     */
    @Override
    public int compareTo(Delayed o) {
        long val = this.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return val == 0 ? 0 : (val < 0 ? -1 : 1);
    }

    public static void main(String[] args) {
        DelayQueue<DelayedTask> queue = new DelayQueue<>();

        queue.add(new DelayedTask(5));
        queue.add(new DelayedTask(10));
        queue.add(new DelayedTask(15));

        System.out.println(System.currentTimeMillis() / 1000 + "start consume");
        while (queue.size() != 0) {
            // 获取当前过期的队列头元素，没有返回null
            DelayedTask delayedTask = queue.poll();
            if (delayedTask != null) {
                System.out.println(System.currentTimeMillis() / 1000 + " consume task");
            }
            // 进行延时等待，每隔一秒访问一次
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
