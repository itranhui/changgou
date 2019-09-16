package com.changgou.oauth.interceptor;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Author：Mr.ran &Date：2019/9/7 19:27
 * <p>
 * @Description：
 */
@Configuration
public class AdminInterceptor implements RequestInterceptor {

    /***
     * 在feign执行前执行
     *  在feign执行生成令牌
     * 将令牌(adminToken)存放到header中
     *
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        //生成管理员令牌
        String token = AdminToken.getAdminToken();
        //将token存放到header中
        template.header("Authorization","bearer "+token);
    }
}
