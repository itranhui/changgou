package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.UserLoginOauthService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/6 19:11
 * <p>
 * @Description：
 */
@Service
public class UserLoginOauthServiceImpl implements UserLoginOauthService {


    /**
     * changgou-user-oauth
     */
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;
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

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) {
        //获取指定服务的注册信息
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        if (serviceInstance == null) {
            throw new RuntimeException("找不到对应的服务");
        }
        //拼接请求地址
        String url = serviceInstance.getUri()+"/oauth/token";

        //封装请求提交的参数
        MultiValueMap<String,String>  parameterMap = new LinkedMultiValueMap<>();
        parameterMap.add("username",username);
        parameterMap.add("password",password);
        parameterMap.add("grant_type",grant_type);

        //请求头封装 base64加密
        String authorization= null;
        try {
             authorization = "Basic "+new String(Base64.getDecoder().decode(clientId+":"+clientSecret.getBytes()),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        MultiValueMap hearderMap = new LinkedMultiValueMap();
        hearderMap.add("authorization",authorization);

        //HttpEntity->创建该对象 封装了请求头和请求体
        HttpEntity  httpEntity = new HttpEntity(parameterMap,hearderMap);

        AuthToken authToken = getAuthToken(url, httpEntity);

        return authToken;
    }

    /***
     *  AuthToken  用户令牌封装
     * @param url
     * @param httpEntity
     * @return
     */
    private AuthToken getAuthToken(String url, HttpEntity httpEntity) {
        /****
         * 1:请求地址
         * 2:提交方式
         * 3:requestEntity:请求提交的数据信息封装 请求体|请求头
         * 4:responseType:返回数据需要转换的类型
         */
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);

        //将令牌信息抓换成 authToken对象
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken((String) response.getBody().get("access_token"));
        authToken.setRefreshToken((String) response.getBody().get("refresh_token"));
        authToken.setJti((String) response.getBody().get("jti"));
        return authToken;
    }
}
