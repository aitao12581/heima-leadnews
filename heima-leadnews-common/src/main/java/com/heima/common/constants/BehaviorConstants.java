package com.heima.common.constants;

/**
 * 用户行为常量表
 */
public class BehaviorConstants {

    // 用户关注作者关注度
    public static final Short LEVEL_OCCA = 0;   // 偶尔
    public static final Short LEVEL_JUST = 1;   // 一般
    public static final Short LEVEL_OFTEN = 2;  // 经常
    public static final Short LEVEL_HIGH = 3;   // 高度

    // 是否开启动态通知
    public static final Short NOTICE_OFF = 0;   // 关闭
    public static final Short NOTICE_ON = 1;    // 开启

    // 粉丝忠诚度
    public static final Short LEVEL_FAN_DEFAULT = 0;   // 正常
    public static final Short LEVEL_FAN_POTENTIAL = 1;   // 潜力股
    public static final Short LEVEL_FAN_WARRIOR = 2;   // 潜力股
    public static final Short LEVEL_FAN_IRON_ROD = 3;   // 铁杆
    public static final Short LEVEL_FAN_OLD_IRON = 4;   // 老铁

    // 标记名称
    public static final String BEHAVIOR_INFO = "behavior_";  // 文章点赞
//    public static final String BEHAVIOR_NOT_LIKE = "unlike_";  // 文章不喜欢
}
