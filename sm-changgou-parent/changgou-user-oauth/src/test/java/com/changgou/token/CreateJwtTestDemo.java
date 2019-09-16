package com.changgou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.token
 * 令牌的创建和解析
 ****/
public class CreateJwtTestDemo {

    /***
     * 创建令牌
     */
    @Test
    public void testCreateToken(){
        //加载证书  读取类路径中的文件
          ClassPathResource resource = new ClassPathResource("changgou68.jks");

        //读取证书数据,加载读取证书数据
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,"changgou68".toCharArray());

        //获取证书中的一对秘钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou68","changgou68".toCharArray());

        //获取私钥->RSA算法
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //创建令牌，需要私钥加盐[RSA算法]
        Map<String,Object> payload = new HashMap<String,Object>();
        payload.put("nikename","tomcat");
        payload.put("address","sz");
        payload.put("authorities",new String [] {"admin","auth"});

        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateKey));

        //获取令牌数据
        String token = jwt.getEncoded();
        System.out.println(token);
    }

    /***
     * 解析令牌
     */
    @Test
    public void testParseToken(){
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoic3oiLCJuaWtlbmFtZSI6InRvbWNhdCIsImF1dGhvcml0aWVzIjpbImFkbWluIiwiYXV0aCJdfQ.MwMwjiUmMq6nl50I2jWdxjaN-Ibq7EhYKlvOSZ24XPojaT4Rot72kMl2XLopLQ5zZbTpUq3oMRkfMtkfoVyLVynE3zYtD0eO5hdBvgb-hRu2T4vLU-cduWR7a1DQr8zH24Yb6P9EgUqDIbGT-B6_0LCcktbBk_uJxxL5Vb-m6RltomSUi0gmZ72xlq1y72AS-dhj7h3-X_hPHHZD7-FzGOv8cDSwKx7czGY9bzC3EyMqLUebCPGiXNYKJJKojkuHp2SEBiP9fljkfyxd_DRbZGVeY9oImSzOvL00Nrq68rzifuY3Q0WyGbNrdOwYQkejIJfxtFDL24vL6orD9ZXmDw";

        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqTlEpq/y1xfOgrJpSIxfo3clEO2gcn2ABw8iHvJ7y6Wx0g9+yuoxT7YTZv3Cam9ITacWtZhy8UlEeG/Y2onSthTcKuWP+fg6zIV2vsoiwd4tMXp7ka8Qk9rpNYpb1h6LcMFJIkhKr6RmqTbveMQ3kW9+CO6Q9Gb2XerdXvWEhsJi80XyuS23AObN63DsNwfIgXUia5qzX6+ao6L6LHpk9fGjcBWk6Rg+uq2V/v5gEdORa2vDW6+QyP6wxnym7RfJjvcC4NlPzgdkqwsqBQkPz/QvLfShhhrcHJL39XItTxOujiDOK6JRLMJIgj986JXlxemvysAoHQzVxmq9waKMVQIDAQAB-----END PUBLIC KEY-----";
        Jwt jwt = JwtHelper.decodeAndVerify(token,
                new RsaVerifier(publickey)
        );
        String claims = jwt.getClaims();
        System.out.println(claims);

    }
}
