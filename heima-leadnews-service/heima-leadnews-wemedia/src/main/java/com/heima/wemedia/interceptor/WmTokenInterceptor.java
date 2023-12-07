package com.heima.wemedia.interceptor;

import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.WmThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * 拦截请求，并获取请求中的用户信息 存储到线程副本
 */
@Slf4j
public class WmTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求中的header
        String userId = request.getHeader("userId");
        // 判断指定值是否为空值，为空返回一个空optional
        Optional<String> optional = Optional.ofNullable(userId);
        // 存在值则返回true，不存在返回false
        if (optional.isPresent()) {
            // 将用户信息存入线程池当中
            WmUser user = new WmUser();
            user.setId(Integer.valueOf(userId));
            WmThreadLocalUtil.setUser(user);
            log.info("wmTokenFilter设置用户信息到threadlocal中...");
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("清理threadlocal...");
        WmThreadLocalUtil.remove();
    }
}
