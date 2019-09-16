package com.changgou.seckill.mq.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.HttpClient;
import com.changgou.seckill.pto.CloseWxPay;
import com.changgou.seckill.service.SeckillOrderService;
import com.github.wxpay.sdk.WXPayUtil;
import javafx.scene.shape.ClosePath;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.persistence.PostLoad;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/14 19:04
 * <p>
 * @Description：
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillMessageListener {

    /**
     * 获取配置文件的对象数据
     */
    @Autowired
    private Environment env;

    /**
     * 支付成功修改订单数据
     */
    @Autowired
    private SeckillOrderService seckillOrderService;
    /****
     * 注入关闭微信支付类
     */
    @Autowired
    private CloseWxPay closeWxPay;



    /***
     * 监听消息
     * @param message
     */
    @RabbitHandler
    public void listenerSeckillMessage( String message){
        System.out.println("监听到的信息是："+message);

        //将消息转化成Map
        Map<String,String> map = JSON.parseObject(message, Map.class);
        //获取return_code(通信标识)
        String returnCode = map.get("return_code");
        //获取订单号
        String outTradeNo = map.get("out_trade_no");
        //获取自定义数据 然后转换成Map
        String attach = map.get("attach");
        Map<String,String> attachMap = JSON.parseObject(attach, Map.class);
        //获取用户名
        String username = attachMap.get("username");
        if (returnCode.equals("SUCCESS")){
            //result_code->业务结果-SUCCESS->改订单状态
            String result_code = map.get("result_code");
            if (result_code.equals("SUCCESS")){
                //说明已经支付成功，那么就要修改订单状态(删除用户排队消息)
                //用户名，transaction_id(微信支付订单号)，支付完成时间
                seckillOrderService.updatePayStatus(username,map.get("transaction_id"),map.get("time_end"));
            }else {
                //没有支付的话那就要取消订单回滚库存
                //关闭微信支付订单传递一个参数订单号(订单id)
                Map<String, String> mapNoPay = closeWxPay.closeWxPay(outTradeNo);
                if (mapNoPay.get("return_code").equals("SUCCESS")){
                    if (mapNoPay.get("result_code").equals("SUCCESS")){
                       //删除订单回滚库存
                        seckillOrderService.deleteOrder(username);
                    }
                }
            }
        }
    }
}
