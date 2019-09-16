package com.changgou.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.HttpClient;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.service.OrderService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/16 17:12
 * <p>
 * @Description：立即支付的消息监听
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.atonceorderqueue}")
public class AtOnceMessageListener {

    /**
     *获取配置文件对象信息 Environment
     *
     */
    @Autowired
    private Environment env;
    /**
     * 注入OrderService当用户支付成功后修改订单相关数据
     * 支付失败后将该订单消息发送到MQ中，如果三小时之内还没有支付的话就删除该订单采用 RabbitMQ的过期队列来实现
     */
    @Autowired
    private OrderService  orderService;

    /***
     * 注入RabbitMQ用于发送消息(给延时队列发送消息)
     *
     */

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /***
     * 立即支付的消息监听
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message) throws Exception {
        //支付结果 将支付微信支付返回的数据转换成Map格式
        Map<String,String> resultMap = JSON.parseObject(message,Map.class);
        System.out.println("监听到的支付结果："+resultMap);

        //判断是否成功成功
        //获取通信标识：return_code
        String return_code = resultMap.get("return_code");
        if (return_code.equals("SUCCESS")){
            //获取支付结果 result_code
            String result_code = resultMap.get("result_code");
            //获取订单号  out_trade_no
            String  out_trade_no = resultMap.get("out_trade_no");
            if (result_code.equals("SUCCESS")){
                //获取微信支付完成时间 time_end
                String time_end = resultMap.get("time_end");
                //获取微信支付完成订单号 transaction_id
                String transaction_id = resultMap.get("transaction_id");
                //说明支付成功,那么调用 updateStatus方法更新支付结果
                orderService.updateStatus(out_trade_no,time_end,transaction_id);
            }else {
                //要先关闭微信支付
                //关闭微信支付订单传递一个参数订单号(订单id)
                Map<String, String> map = closeWxPay(out_trade_no);
                if (map.get("return_code").equals("SUCCESS")){
                    if (map.get("result_code").equals("SUCCESS")){
                        //支付失败，那么发送一个支付失败延时队列，如果该订单三小时后还没有被支付的话那么就删除该订单，回滚库存，回滚用户积分
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        System.out.println("支付失败时间：" + simpleDateFormat.format(new Date()));
                        rabbitTemplate.convertAndSend("WXPayDelayQueue", (Object) out_trade_no, new MessagePostProcessor() {
                            @Override
                            public Message postProcessMessage(Message message) throws AmqpException {
                                //测试设置5秒
                                message.getMessageProperties().setExpiration("5000");
                                return message;
                            }
                        });
                    }
                }
            }
        }
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
