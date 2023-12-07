package com.heima.admin.interceptor;

import com.heima.model.admin.pojos.AdUser;
import com.heima.utils.common.ADThreadLocalUtil;
import com.heima.utils.common.WmThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class AdminInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的信息
        String userId = request.getHeader("userId");
        Optional<String> optional = Optional.ofNullable(userId);
        if (optional.isPresent()) {
            // 将用户信息存入线程副本中
            AdUser adUser = new AdUser();
            adUser.setId(Integer.valueOf(userId));
            ADThreadLocalUtil.setUser(adUser);
            log.info("admin 设置用户信息到threadlocal中...");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("清理threadlocal...");
        ADThreadLocalUtil.remove();
    }
}
