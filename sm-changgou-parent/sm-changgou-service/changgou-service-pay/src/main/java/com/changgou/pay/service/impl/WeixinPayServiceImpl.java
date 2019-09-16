package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.HttpClient;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.spring.annotation.MapperScan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/10 15:14
 * <p>
 * @Description：
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    /**
     * 应用ID
     */
    @Value("${weixin.appid}")
    private String appid;
    /**
     * 商户ID
     */
    @Value("${weixin.partner}")
    private String partner;
    /**
     * 秘钥（签名）
     */
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    /**
     * 支付回调地址
     */
    @Value("${weixin.notifyurl}")
    private String notifyurl;

    /***
     * 微信支付状态的查询
     * @param outtradeno
     * @return
     */
    @Override
    public Map queryStatus(String outtradeno) {
        try {
            //创建map集合封装所需参数
            Map<String, String> paramMap = new HashMap<String, String>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //商品订单号(orderId)
            paramMap.put("out_trade_no", outtradeno);
            //Map转换成XML格式可以携带签名
            String xmlparameters = WXPayUtil.generateSignedXml(paramMap, partnerkey);

            //Url地址微信支付订单查询地址
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient httpClient = new HttpClient(url);
            //使用https提交方式
            httpClient.setHttps(true);
            //提交的参数
            httpClient.setXmlParam(xmlparameters);
            //微信支付 必须使用POST提交 微信支付规定
            //执行请求
            httpClient.post();
            //获取返回结果(XML格式)
            String xmlContent = httpClient.getContent();
            //将XML格式转换成 Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlContent);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*****
     * 创建二维码
     * @param //out_trade_no : 客户端自定义订单编号
     * @param //total_fee    : 交易金额,单位：分
     * @return
     */
    @Override
    public Map createnative(Map<String, String> parameterMap) {

        try {
            //创建map集合封装所需参数
            Map<String, String> paramMap = new HashMap<String, String>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //商品描述
            paramMap.put("body", "畅物购商品结算");
            //商品订单号(orderId)
            paramMap.put("out_trade_no", parameterMap.get("outtradeno"));
            //交易金额，单位：分
            paramMap.put("total_fee", parameterMap.get("totalfee"));
            //终端IP
            paramMap.put("spbill_create_ip", "127.0.0.1");
            //通知地址
            paramMap.put("notify_url", notifyurl);
            //交易类型
            paramMap.put("trade_type", "NATIVE");
            //设置二维码的有效时期
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            Date date = new Date();
            String time_start = simpleDateFormat.format(date);
            paramMap.put("time_start",time_start);

            //将 time_start转换成 Long 然后加上100  那么就是有效期1分钟
            Long time_expire = Long.valueOf(time_start);
            time_expire =  time_expire+ 100l;
            String time_ecpire_str = time_expire.toString();
            //设置二维码到期时间
            paramMap.put("time_expire",time_ecpire_str);


            //创建自定义的数据  存放交换机名称，和队列名称传递过来
            //获取交换机名称
            String exchange = parameterMap.get("exchange");
            String routingkey = parameterMap.get("routingkey");
            Map<String,String> attachMap = new HashMap<String, String>();
            attachMap.put("exchange",exchange);
            attachMap.put("routingkey",routingkey);

            //todo  如果是秒杀商品需要传递username
            String username = parameterMap.get("username");
            if (!StringUtils.isEmpty(username)){
                //自定义数据 增加username
                attachMap.put("username",username);
            }
            String attach = JSON.toJSONString(attachMap);

            paramMap.put("attach",attach);


            //Map转换成XML格式可以携带签名
            String xmlparameters = WXPayUtil.generateSignedXml(paramMap, partnerkey);



            //Url地址微信支付地址
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            HttpClient httpClient = new HttpClient(url);
            //使用https提交方式
            httpClient.setHttps(true);
            //提交的参数
            httpClient.setXmlParam(xmlparameters);
            //微信支付 必须使用POST提交 微信支付规定
            //执行请求
            httpClient.post();
            //获取返回结果(XML格式)
            String xmlContent = httpClient.getContent();
            //将XML格式转换成 Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlContent);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
