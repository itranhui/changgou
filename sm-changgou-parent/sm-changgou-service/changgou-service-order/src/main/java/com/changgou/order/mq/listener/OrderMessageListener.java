package com.changgou.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*****
 * @Author:
 * @Description: com.changgou.order.mq.listener
 ****/
@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class OrderMessageListener {

    @Autowired
    private OrderService orderService;

    /***
     * 注入SkuFeign
     */
    @Autowired
    private SkuFeign skuFeign;
    /****
     * 支付结果监听
     */
    @RabbitHandler
    public void getMessage(String message) throws Exception{
        //支付结果
        Map<String,String> resultMap = JSON.parseObject(message,Map.class);
        System.out.println("监听到的支付结果："+resultMap);

        //通信标识 return_code
        String return_code = resultMap.get("return_code");

        if(return_code.equals("SUCCESS")){
            //业务结果 result_code
            String result_code = resultMap.get("result_code");

            //订单号 out_trade_no
            String out_trade_no = resultMap.get("out_trade_no");

            //支付成功，修改订单状态
            if(result_code.equals("SUCCESS")){
                //修改订单状态  商品订单号 out_trade_no ; 支付完成时间	time_end; 微信支付订单号	transaction_id
                orderService.updateStatus(out_trade_no,resultMap.get("time_end"),resultMap.get("transaction_id"));
            }else{
                //关闭支付->
                //支付失败,关闭支付，取消订单，回滚库存
                orderService.delete(out_trade_no);
                //回滚库存对应订单(中的商品)的库存
                //根据订单号查询该订单号对应的全部商品数据 List<orderItem>(回滚仓库)
                List<OrderItem> orderItems  = orderService.selectOrderItems(out_trade_no);

                //遍历集合取出满足该订单条件的所有的key:skuId , value:num
                Map<String,Object> skuIdsAndNumMap = new HashMap<String, Object>();
                for (OrderItem orderItem : orderItems) {
                    String skuId = orderItem.getSkuId().toString();
                    Integer num = orderItem.getNum();
                    skuIdsAndNumMap.put(skuId,num);
                }
                //调用goods微服务 传递 skuIdsAndNumMap执行库存回滚操作
                skuFeign.addCount(skuIdsAndNumMap);
            }
        }
    }
}
