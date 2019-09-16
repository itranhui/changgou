package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

public interface UserLoginOauthService {
    /***
     * 登录实现
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     * 参数传递：
     * 1.账号		 username=szitheima
     * 2.密码		 password=szitheima
     * 3.授权方式		grant_type=password
     * 请求头传递
     * MD5->加密字符[不可逆(只能加密不能解密)]：摘要加密[签名(RSA算法->私钥)->RSA(公钥)]
     * JWT令牌
     */
    AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type);
}
