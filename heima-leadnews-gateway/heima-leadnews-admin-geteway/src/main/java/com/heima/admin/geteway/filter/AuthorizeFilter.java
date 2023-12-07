package com.heima.admin.geteway.filter;

import com.heima.admin.geteway.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取request 和 response 对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 判断是否是登录的请求
        if (request.getURI().getPath().contains("/login")) {
            return chain.filter(exchange); // 放行
        }

        // 非登录请求 获取token
        String token = request.getHeaders().getFirst("token");

        // 验证token是否存在
        if (StringUtils.isEmpty(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        try {
            // 验证token是否过期
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            int status = AppJwtUtil.verifyToken(claimsBody);
            if (status==1 || status==2) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            } else {
                // 将user信息存放至header中
                Object id = claimsBody.get("id");
                ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                    httpHeaders.add("userId", id + "");
                }).build();

                exchange.mutate().request(serverHttpRequest).build();
            }
        } catch (Exception e) {
            log.error("admin 过滤器 拦截异常");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // token未过期，存在
        return chain.filter(exchange);
    }

    /**
     * 拦截器优先级
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
