package com.changgou.seckill.pto;

import com.changgou.entity.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/14 20:28
 * <p>
 * @Description：
 */
@Component
public class CloseWxPay {
    @Autowired
    private Environment env;

    /***
     * 关闭微信的订单支付
     */
    public Map<String, String> closeWxPay(String outtradeno) {
        try {
            //创建map封装请求参数数据
            Map<String, String> paramMap = new HashMap<String, String>();
            //封装订单id
            paramMap.put("out_trade_no", outtradeno);
            //公众号ID(也就是应用ID)
            paramMap.put("appid", env.getProperty("weixin.appid"));
            //商户ID 每个 商户的ID都是唯一的
            paramMap.put("mch_id", env.getProperty("weixin.partner"));
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名(密钥)
            //paramMap.put("sign",env.getProperty("weixin.partnerkey"));
            //转换成XML格式 也可以这样携带签名
            String paramXML = WXPayUtil.generateSignedXml(paramMap, env.getProperty("weixin.partnerkey"));

            //使用httpClient 发送请求
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";
            HttpClient httpClient = new HttpClient(url);
            //开启https请求方式
            httpClient.setHttps(true);
            //设置提交的参数
            httpClient.setXmlParam(paramXML);
            //使用POST执行请求
            httpClient.post();
            //获取请求结果
            String XMLcontent = httpClient.getContent();
            //转换成Map
            Map<String, String> map = WXPayUtil.xmlToMap(XMLcontent);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
