package com.heima.common.constants;

public class UserConstants {

    // 用户审核状态
    public static final short AUTH_CREATEING = 0;   // 创建中
    public static final short AUTH_NOT_AUDITING = 1;    // 待审核
    public static final short AUTH_AUDITING_ERROR = 2;  // 审核失败
    public static final short AUTH_AUDITING_SUCCESS = 9;    // 审核成功

    // 发送协议
    public static final String AP_USER_NEWS_USER_TOPIC = "ap.user.news.user.topic";
    public static final String AP_USER_NEWS_USER_RESP_TOPIC = "ap.user.news.user.resp.topic";
}
