package com.heima.utils.common;

import com.heima.model.admin.pojos.AdUser;

/**
 * app端用户登录后信息保存
 */
public class ADThreadLocalUtil {

    private static final ThreadLocal<AdUser> AD_USER_THREAD_LOCAL = new ThreadLocal<>();


    public static AdUser getUser() {
        return AD_USER_THREAD_LOCAL.get();
    }

    public static void setUser(AdUser adUser) {
        AD_USER_THREAD_LOCAL.set(adUser);
    }

    public static void remove() {
        AD_USER_THREAD_LOCAL.remove();
    }
}
