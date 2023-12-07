package com.heima.utils.common;

import com.heima.model.user.pojos.ApUser;

/**
 * app端用户登录后信息保存
 */
public class AppThreadLocalUtil {

    private static final ThreadLocal<ApUser> AP_USER_THREAD_LOCAL = new ThreadLocal<>();


    public static ApUser getUser() {
        return AP_USER_THREAD_LOCAL.get();
    }

    public static void setUser(ApUser apUser) {
        AP_USER_THREAD_LOCAL.set(apUser);
    }

    public static void remove() {
        AP_USER_THREAD_LOCAL.remove();
    }
}
