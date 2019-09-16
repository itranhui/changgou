package com.changgou.order.pto;

import com.changgou.entity.HttpClient;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.service.OrderService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/10 20:34
 * <p>
 * @Description： Processing timeout orders(包名) 处理超时订单
 */
@RestController
public class ProcessingTimeoutOrders {
    /***
     * 删除订单数据
     */
    @Autowired
    private OrderService orderService;
    /**
     * 回滚库存
     */
    @Autowired
    private SkuFeign skuFeign;
    /**
     * 获取配置文件中的对象消息
     */
    @Autowired
    private Environment env;

    /****
     * 处理超时订单的方法
     * @param orderId  订单id
     * @return int  0 :处理失败 ; 1 处理成功
     */
    public   int processingTimeoutOrders(String orderId) {

            //关闭微信支付订单传递一个参数订单号(订单id)
            Map<String, String> map = closeWxPay(orderId);
                if (map.get("return_code").equals("SUCCESS")){
                    if (map.get("result_code").equals("SUCCESS")){
                        //根据orderId查询出对应的 skuId(key) , num(value)
                        Map<String, Object> skuIdsAndNumMap = orderService.skuIdsAndNumMap(orderId);
                        //删除订单库存回滚
                        //par1:要删除的skuId
                        //par2:要删除的orderItem个数
                        skuFeign.addCount(skuIdsAndNumMap);
                        //根据orderId 删除对应的订单数据 ，删除订单的同时也要删除对应订单的orderItem数据
                        orderService.isDelete(orderId);
                        return 1;
                    }
                }
            return 0;
    }

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
