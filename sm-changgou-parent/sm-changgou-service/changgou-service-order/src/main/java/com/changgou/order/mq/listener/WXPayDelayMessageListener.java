package com.changgou.order.mq.listener;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import com.changgou.user.feign.UserFeign;
import com.changgou.user.pojo.User;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/16 17:50
 * <p>
 * @Description：监听支付失败的队列
 */
@Component
@RabbitListener(queues = "WXPayListenerQueue")
public class WXPayDelayMessageListener {


    /***
     * 注入OrderService  用于处理 支付失败的订单是否在一段时间后支付成功
     * 0 ：支付还未成功
     * 1 ：支付成功
     */

    @Autowired
    private OrderService orderService;

    /***
     * 注入OrderItemMapper 删除该订单对应的所有的商品数据
     */

    @Autowired
    private OrderItemMapper orderItemMapper;

    /***
     * 注入SkuFeign用于回滚商品库存
     *
     */

    @Autowired
    private SkuFeign skuFeign;

    /***
     *
     * 注入UserFeign 用户回滚用户积分
     *
     */

    @Autowired
    private UserFeign userFeign;

    /***
     * 监听 WXPayListenerQueue队列，判断用户是否支付成功 对于成功或者时失败做出不同的处理
     *
     * @param message 该订单的ID
     */
    @GlobalTransactional//控制分布式事务
    @RabbitHandler
    public void getMessage(String message) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.print("监听消息的时间：" + simpleDateFormat.format(new Date()));
            System.out.println(",监听到的消息：" + message);
            //返回对应的order
            Order order = orderService.findById(message);
            //将order传入OrderService中 0 ：支付还未成功 , 1 ：支付成功
            int count = orderService.checkWXPay(order);
            if (count == 1) {
                //说明支付成功，
                System.out.println("上次支付失败的订单，现在支付成功！！");
                return;
            }
            //说明支付失败(一定时间过去后还是没有支付该订单，删除订单，回滚库存，回滚用户积分)
            //查询出该订单的用户名
            String username = order.getUsername();
            //1.删除订单信息
            orderService.delete(order.getId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            // 根据orderId查询出全部的OrderItem 然后将OrderItem中的skuId 存放到数组中
            Map<String, Object> skuIdAndNumMap = skuIdsAndNumMap(order.getId());
            //2.删除OrderItem数据
            orderItemMapper.delete(orderItem);
            //3.回滚goods中的商品库存(调用goods微服务)
            skuFeign.addCount(skuIdAndNumMap);
            //4.回滚该用户对应的积分(调用user微服务)
            //获取该用户回滚之前的积分数，然后把 原有的积分数+ 1
            User user = userFeign.findById(username).getData();
            user.setPoints(user.getPoints() + 1);
            userFeign.update(user, username);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.err.println("执行失败");
        }
    }
    /***
     * 根据orderId查询出对应的 skuId(key) , num(value)
     * @param orderId
     * @return
     */

    public Map<String, Object> skuIdsAndNumMap(String orderId) {
        //创建map 封装返回数据
        Map<String, Object> map = new HashMap<String, Object>();
        //查询对应的OrderItem
        OrderItem orderItem = new OrderItem();
        //将orderId设置到OrderItem中(作为查询条件)
        orderItem.setOrderId(orderId);
        List<OrderItem> orderItems = orderItemMapper.select(orderItem);

        //遍历orderItems获取对应的sku和num 封装成map
        if (orderItems != null && orderItems.size() > 0) {
            for (OrderItem item : orderItems) {
                Long skuId = item.getSkuId();
                String skuidstr = skuId.toString();
                map.put(skuidstr, item.getNum());
            }

        }
        return map;
    }
}
