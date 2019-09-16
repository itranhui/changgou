package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.oauth.service
 ****/
public interface UserLoginService {

    /***
     * 密码模式登录操作
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     */
    AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) throws Exception;
}
