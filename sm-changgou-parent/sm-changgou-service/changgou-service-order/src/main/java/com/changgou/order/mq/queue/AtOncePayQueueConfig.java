package com.changgou.order.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author：Mr.ran &Date：2019/9/16 17:27
 * <p>
 * @Description：支付失败的发送延时消息队列
 */
@Configuration
public class AtOncePayQueueConfig {

    /**
     * 创建延时队列消息
     *
     */

    @Bean
    public Queue WXPayDelayQueue(){
        return QueueBuilder
                .durable("WXPayDelayQueue")
                .withArgument("x-dead-letter-exchange","WXPayListenerExchange")
                .withArgument("x-dead-letter-routing-key","WXPayListenerQueue")
                .build();
    }

    /**
     * 创建需要接收死信队列中的数据队列
     *
     */
    @Bean
    public Queue WXPayListenerQueue(){
        return new Queue("WXPayListenerQueue");
    }

    /***
     * 创建交换机
     *
     */
    @Bean
    public Exchange WXPayListenerExchange(){
        return new DirectExchange("WXPayListenerExchange");
    }

    /***
     *
     *
     * 队列绑定交换机
     *
     */

    @Bean
    public Binding WXPayListenerBinding (Queue WXPayListenerQueue ,Exchange WXPayListenerExchange){
        return BindingBuilder.bind(WXPayListenerQueue).to(WXPayListenerExchange).with("WXPayListenerQueue").noargs();
    }

}
