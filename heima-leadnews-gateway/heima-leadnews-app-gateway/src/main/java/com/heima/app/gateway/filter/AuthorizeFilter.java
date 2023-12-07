package com.heima.app.gateway.filter;

import com.heima.app.gateway.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {

    /**
     * 拦截逻辑
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取request 和response对象 方便获取链接以及token
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 获取请求拦截
        if (request.getURI().getPath().contains("/login")) {
            // 登录连接，进行放行
            return chain.filter(exchange);
        }

        // 获取token参数
        String token = request.getHeaders().getFirst("token");

        // 判断token是否存在
        if (StringUtils.isBlank(token)) {
            // 不存在则返回错误
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        try {
            // token 存在 判断token是否有效
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            int status = AppJwtUtil.verifyToken(claimsBody);
            if (status==1 || status == 2) {
                // token无效
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            } else {
                // 获得token解析后的用户信息
                Object userId = claimsBody.get("id");
                // 在header中添加新信息
                ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                    httpHeaders.add("userId", userId + "");
                }).build();

                // 重置header信息
                exchange.mutate().request(serverHttpRequest).build();
            }
        } catch (Exception e) {
            log.info("异常:{}", e.toString());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // token有且有效

        return chain.filter(exchange);
    }

    /**
     * 优先级设置 值越小优先级越高
     * @return int值
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
