package com.heima.utils.common;

import com.heima.model.wemedia.pojos.WmUser;

/**
 *  线程副本工具类，在线程中存储用户信息
 */
public class WmThreadLocalUtil {

    private static final ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加用户
     * @param wmUser 用户信息
     */
    public static void setUser(WmUser wmUser) {
        WM_USER_THREAD_LOCAL.set(wmUser);
    }


    /**
     * 获取用户
     */
    public static WmUser getUser() {
        return WM_USER_THREAD_LOCAL.get();
    }


    /**
     * 删除用户
     */
    public static void remove() {
        WM_USER_THREAD_LOCAL.remove();
    }
}
