package com.changgou.oauth.controller;

import com.changgou.oauth.service.UserLoginOauthService;
import com.changgou.oauth.util.AuthToken;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author：Mr.ran &Date：2019/9/6 19:05
 * <p>
 * @Description：
 */
//@RequestMapping("/user")
//@RestController
public class UserLoginOauthController {

    /**
     * 读取配置文件中的客户端id
     */
    @Value("${autth.clientId}")
    private String clientId;
    /**
     * 读取客户端密钥
     *Secret (秘密)
     */
    @Value("${auth.clientSecret}")
    private  String   clientSecret;

    /**
     * 注入service
     */
    @Autowired
    private UserLoginOauthService userLoginOauthService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/login")
    public Result login(String username,String password){
        //调用userLoginService实现登录
        String grant_type = "password";
        AuthToken authToken  =  userLoginOauthService.login(username,password,clientId,clientSecret,grant_type);

        if (authToken !=null){
            //登录成功
            return new Result(true, StatusCode.OK,"登录成功", authToken);
        }
        return new Result(false,StatusCode.LOGINERROR,"登录失败");
    }

}
