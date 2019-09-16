package com.itheima.jwt;

import com.github.wxpay.sdk.WXPayUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;
import org.junit.validator.PublicClassValidator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*****
 * @Author:
 * @Description:
 * 令牌的生成和解析
 ****/
public class JwtTest {

    /****
     * 创建令牌
     */
    @Test
    public void testCreateToken(){
        //构建Jwt令牌的对象
        JwtBuilder builder = Jwts.builder();
        builder.setIssuer("黑马训练营");    //颁发者
        builder.setIssuedAt(new Date());    //颁发时间
        builder.setExpiration(new Date(System.currentTimeMillis()+3600000));  //过期时间
        builder.setSubject("JWT令牌测试"); //主题信息

        //自定义载荷信息
        Map<String,Object> userInfo = new HashMap<String,Object>();
        userInfo.put("company","黑马训练营");
        userInfo.put("address","中南海");
        userInfo.put("money",3500);
        builder.addClaims(userInfo);        //添加载荷


        builder.signWith(SignatureAlgorithm.HS256,"itcast");  //1：签名算法   2：秘钥(盐)
        String token = builder.compact();
        System.out.println(token);
    }

    /***
     * 令牌解析
     */
    @Test
    public void parseToken(){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiLpu5Hpqazorq3nu4PokKUiLCJpYXQiOjE1Njc1Njk5ODIsImV4cCI6MTU2NzU3MzU4Miwic3ViIjoiSldU5Luk54mM5rWL6K-VIiwiYWRkcmVzcyI6IuS4reWNl-a1tyIsIm1vbmV5IjozNTAwLCJjb21wYW55Ijoi6buR6ams6K6t57uD6JClIn0.sSmvmB2canXMe-KmEmwKs_k_VWif0I8N9kNS_EsnOUQ";
        Claims claims = Jwts.parser()
                .setSigningKey("itcast")   //秘钥(盐)
                .parseClaimsJws(token)      //要解析的令牌对象
                .getBody();//获取解析后的数据
        System.out.println(claims.toString());
    }


    @Test
    public  void test() throws Exception {
       // System.out.println(WXPayUtil.generateNonceStr());//获取随机的一串字符串
        Map<String,String> map = new HashMap<String, String>();
        map.put("q001","11");
        map.put("q002","11");
        map.put("q003","11");
        //String mapToXml = WXPayUtil.mapToXml(map);
       // System.out.println(WXPayUtil.mapToXml(map));
       // System.out.println(WXPayUtil.xmlToMap(mapToXml));
        //String signature = WXPayUtil.generateSignature(map, "1");
        //System.out.println(signature);
        String s = WXPayUtil.generateSignedXml(map, "1");
        System.out.println(s);
        System.out.println(WXPayUtil.xmlToMap(s));
    }
}
