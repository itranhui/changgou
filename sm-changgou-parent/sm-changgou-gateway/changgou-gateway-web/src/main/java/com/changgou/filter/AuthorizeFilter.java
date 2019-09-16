package com.changgou.filter;

import com.changgou.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
/**
 * @Author：Mr.ran &Date：2019/9/4 19:06
 * <p>
 * @Description：
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {


    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    /****
     * 全局过滤器
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取 response
        ServerHttpResponse response = exchange.getResponse();
        //获取 request
        ServerHttpRequest request = exchange.getRequest();

        //获取url 路径
        String url  = request.getURI().getPath();

        String login = "/api/user/login";
        String search = "api/brand/search";
        //如果是登录等常规请求就放行
        if (url.startsWith(login)|| url.startsWith(search)){
            //放行
            Mono<Void> filter = chain.filter(exchange);
            return   filter;
        }


        String uri = request.getURI().toString();
        if (!URLFilter.hasAuthorize(uri)){
            return chain.filter(exchange);
        }


        //从请求头中取出token
        String token =  request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        //true  说明头中有token  false 说明没有
        boolean hasToken = true;

        //如果头中没有token 就从请求参数中获取token
        if (StringUtils.isEmpty(token)){
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            hasToken = false;
        }
        //如果请求参数中没有的话那么就从 cookie中获取
        if (StringUtils.isEmpty(token)){
            HttpCookie first = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (first!=null){
                    token =first.getValue();
                }
        }
        //如果token为mull的话 就输出错误信息
        if (StringUtils.isEmpty(token)){
            response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            return response.setComplete();
        }

        //解析令牌
         /*     try {
            Claims claims = JwtUtil.parseJWT(token);
            //将token添加到请求头中
            request.mutate().header(AUTHORIZE_TOKEN,token);
        } catch (Exception e) {
            e.printStackTrace();
            //解析失败响应 401
             response.setStatusCode(HttpStatus.UNAUTHORIZED);
             return response.setComplete();
        }*/
        //如果令牌为空则设置状态码为401
        if (org.springframework.util.StringUtils.isEmpty(token)){
             //设置401 返回
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空数据
            return response.setComplete();
        }else {
            //不为空的话，那么检验令牌是否是以bearer 开头 如果不是就添加
            if (!token.startsWith("bearer ") && !token.startsWith("Bearer ")) {

                //拼接token
                token = "bearer " + token;

            }
            //将 令牌封装到头文件中
                request.mutate().header(AUTHORIZE_TOKEN, token);
        }
        //放行
        return  chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
