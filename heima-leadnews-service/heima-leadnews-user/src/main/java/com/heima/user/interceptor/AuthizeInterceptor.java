package com.heima.user.interceptor;

import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class AuthizeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取id
        String userId = request.getHeader("userId");
        Optional<String> optional = Optional.ofNullable(userId);
        if (optional.isPresent()) {
            ApUser apUser = new ApUser();
            apUser.setId(Integer.valueOf(userId));
            AppThreadLocalUtil.setUser(apUser);
            log.info("app user threadLocal 存储成功。。。");
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("threadLocal 清理。。。");
        AppThreadLocalUtil.remove();
    }
}
