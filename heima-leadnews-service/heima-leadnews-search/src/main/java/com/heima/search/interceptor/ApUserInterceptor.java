package com.heima.search.interceptor;

import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class ApUserInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 拦截请求中的header
        String userId = request.getHeader("userId");
//        System.out.println("header中存储的内容："+userId);
        // 判断是否为空值
        Optional<String> id = Optional.ofNullable(userId);
        if (id.isPresent()) {
            // 将用户信息存入线程池中
            ApUser apUser = new ApUser();
            apUser.setId(Integer.valueOf(userId));
            AppThreadLocalUtil.setUser(apUser);
            log.info("app将用户信息存入到threadLocal中");
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("清理 threadLocal。。。");
        AppThreadLocalUtil.remove();
    }
}
