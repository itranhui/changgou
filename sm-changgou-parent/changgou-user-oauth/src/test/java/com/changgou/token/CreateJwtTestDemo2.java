package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import sun.misc.IOUtils;

import java.io.*;
import java.net.URL;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.token
 * 令牌的创建和解析
 ****/
public class CreateJwtTestDemo2 {

    /***
     * 创建令牌
     */
    @Test
    public void testCreateToken() {
        //加载证书
        ClassPathResource classPathResource = new ClassPathResource("changgou68.jks");
        /**
         * 读取密钥对
         * par1:证书路径
         * par2: (在创建证书的时候指定的)
         */
        KeyStoreKeyFactory keyFactory = new KeyStoreKeyFactory(classPathResource, "changgou68".toCharArray());

        /**
         * 获取证书中的一对密钥  Pair 一对
         * par1:证书别名(在创建证书的时候指定的)
         * par2:密钥密码
         */
        KeyPair keyPair = keyFactory.getKeyPair("changgou68", "changgou68".toCharArray());

        //获取私钥->RSA算法
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //创建令牌 需要私钥加盐[RSA算法] 创建载荷
        Map<String, String> payload = new HashMap<>();
        payload.put("nikename", "tomcat");
        payload.put("address", "sz");
        payload.put("role", "admin,user");
        //将私钥加盐
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        //获取token
        String token = jwt.getEncoded();
        System.out.println(token);
    }

    /***
     * 解析令牌
     */
    @Test
    public void testParseToken() {

        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoic3oiLCJyb2xlIjoiYWRtaW4sdXNlciIsIm5pa2VuYW1lIjoidG9tY2F0In0.dgTmkVkXLht-YeYBo-2s93kqW-CDX1s2dKUBlzCaC4DS5XlHacVH2ENgrc5IVRROU0DOTbsdcpz5b311QTPXuetpJlfwTTtXppxl-mmQKu_kP_iWwLbMM_RkUJwjmcvilwraVJ4EBTvk_ju7f1EOiOsEDpxMKz5BDparLnJuNtdcU151a01fhRSUxjhr_h0cGi6E0P8RKaLZSABz-Qyccb38R_1wF1yPn4wlNih27G2PYBudGXcO3JRoBugUiuHfKeMpfxPbi6obLBVZlA1rBeMHH5DX1r4zHBq-6DnQ9d2My3IBsVQBcuEqzjNeRSYT_idfIxMusrk5Ue-lZQH3qg";


        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqTlEpq/y1xfOgrJpSIxfo3clEO2gcn2ABw8iHvJ7y6Wx0g9+yuoxT7YTZv3Cam9ITacWtZhy8UlEeG/Y2onSthTcKuWP+fg6zIV2vsoiwd4tMXp7ka8Qk9rpNYpb1h6LcMFJIkhKr6RmqTbveMQ3kW9+CO6Q9Gb2XerdXvWEhsJi80XyuS23AObN63DsNwfIgXUia5qzX6+ao6L6LHpk9fGjcBWk6Rg+uq2V/v5gEdORa2vDW6+QyP6wxnym7RfJjvcC4NlPzgdkqwsqBQkPz/QvLfShhhrcHJL39XItTxOujiDOK6JRLMJIgj986JXlxemvysAoHQzVxmq9waKMVQIDAQAB-----END PUBLIC KEY-----";


        /*
         *jwt解析token
         * par1:token
         * par2:公钥 (公钥不能解密  只能校验)
         */
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
        //获取解析后的数据
        String claims = jwt.getClaims();

        System.out.println(claims);

    }


    /***
     * 解析令牌
     */
    @Test
    public void testParseTokenDemo() {

        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoic3oiLCJyb2xlIjoiYWRtaW4sdXNlciIsIm5pa2VuYW1lIjoidG9tY2F0In0.dgTmkVkXLht-YeYBo-2s93kqW-CDX1s2dKUBlzCaC4DS5XlHacVH2ENgrc5IVRROU0DOTbsdcpz5b311QTPXuetpJlfwTTtXppxl-mmQKu_kP_iWwLbMM_RkUJwjmcvilwraVJ4EBTvk_ju7f1EOiOsEDpxMKz5BDparLnJuNtdcU151a01fhRSUxjhr_h0cGi6E0P8RKaLZSABz-Qyccb38R_1wF1yPn4wlNih27G2PYBudGXcO3JRoBugUiuHfKeMpfxPbi6obLBVZlA1rBeMHH5DX1r4zHBq-6DnQ9d2My3IBsVQBcuEqzjNeRSYT_idfIxMusrk5Ue-lZQH3qg";

        URL url = Resource.class.getClassLoader().getResource("public.key");
        String path = url.getPath();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String publickey = null;
        try {
            byte[] bytes = IOUtils.readNBytes(inputStream, 1);

            publickey = new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(publickey);

        //公钥
        /* String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqTlEpq/y1xfOgrJpSIxfo3clEO2gcn2ABw8iHvJ7y6Wx0g9+yuoxT7YTZv3Cam9ITacWtZhy8UlEeG/Y2onSthTcKuWP+fg6zIV2vsoiwd4tMXp7ka8Qk9rpNYpb1h6LcMFJIkhKr6RmqTbveMQ3kW9+CO6Q9Gb2XerdXvWEhsJi80XyuS23AObN63DsNwfIgXUia5qzX6+ao6L6LHpk9fGjcBWk6Rg+uq2V/v5gEdORa2vDW6+QyP6wxnym7RfJjvcC4NlPzgdkqwsqBQkPz/QvLfShhhrcHJL39XItTxOujiDOK6JRLMJIgj986JXlxemvysAoHQzVxmq9waKMVQIDAQAB-----END PUBLIC KEY-----";*/


        /*
         *jwt解析token
         * par1:token
         * par2:公钥 (公钥不能解密  只能校验)
         */
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
        //获取解析后的数据
        String claims = jwt.getClaims();

        System.out.println(claims);

    }

    @Test
    public void test() {

        Integer a = 200;
        Integer b = 200;
        System.out.println(System.identityHashCode(a));
        System.out.println(System.identityHashCode(b));
        System.out.println(a==b);

        boolean i = false;
        int c = 200;
        int d = 200;
        System.out.println(System.identityHashCode(c));
        System.out.println(System.identityHashCode(d));
        System.out.println(c==d);

    }


}
