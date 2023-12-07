package com.heima.model.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类型的枚举类
 */
@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    NEWS_SCAN_TIME(1001, 1, "文章定时审核"),
    REMOTEERROR(1002, 2, "第三方接口调用失败，重试");

    private final int taskType; // 对应的具体业务
    private final int priority; // 业务的优先级
    private final String desc; // 描述信息
}
