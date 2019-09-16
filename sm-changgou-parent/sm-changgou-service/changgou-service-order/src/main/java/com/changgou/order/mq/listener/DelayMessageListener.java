package com.changgou.order.mq.listener;

import com.changgou.order.pto.ProcessingTimeoutOrders;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.spring.annotation.MapperScan;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author：Mr.ran &Date：2019/9/10 20:18
 * <p>
 * @Description：过期消息的监听
 */
@Component
@RabbitListener(queues = "orderListenerQueue")//说明是rabbitMQ的队列监听类 需要监听的队列
public class DelayMessageListener {

    /**
     * 注入ProcessingTimeoutOrders 来处理超时订单
     */
    @Autowired
    private ProcessingTimeoutOrders processingTimeoutOrders;

    /**
     * 监听队列消息(orderListenerQueue 队列)
     *
     * @param message 订单id
     */
    @RabbitHandler
    public void getDelayMessage(String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.print("监听消息的时间：" + simpleDateFormat.format(new Date()));
        System.out.println(",监听到的消息：" + message);
        //处理超时订单
        try {
            int count = processingTimeoutOrders.processingTimeoutOrders(message);
            System.out.println("延时订单处理成功！！！");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("失败！！！");
        }
    }
}
