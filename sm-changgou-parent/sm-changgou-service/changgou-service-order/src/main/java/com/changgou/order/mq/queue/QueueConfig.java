package com.changgou.order.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author：Mr.ran &Date：2019/9/10 19:54
 * <p>
 * @Description：利用延时队列的特性实现超时订单的处理(一个延时队列，一个正常队列监听延时队列)
 */
@Configuration
public class QueueConfig {
    /***
     * 创建延时队列(消息会在指定的时间过期)
     *  //orderDelayQueue队列信息会过期，过期之后，进入到死信队列，死信队列数据绑定到其他交换机中
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        return  QueueBuilder
                //队列名(队列实例)
                .durable("orderDelayQueue")
                //par1:消息超时进入死信队列，绑定死信队列交换机
                //par2:死信队列的数据要要发送给哪个交换机？？
                .withArgument("x-dead-letter-exchange","orderListenerExchange")
                //par1:交换机绑定路由key
                //par2:路由key的值(需要接收死信队列的数据的队列名)
                .withArgument("x-dead-letter-routing-key","orderListenerQueue")
                //构建队列
                .build();
    }
    /**
     * 创建需要接收死信队列中的数据的对列
     */
    @Bean
    public Queue orderListenerQueue(){
        return new Queue("orderListenerQueue");
    }

    /***
     * 创建交换机
     *
     */
    @Bean
    public Exchange orderListenerExchange(){
        return new DirectExchange("orderListenerExchange");
    }

    /***
     * 队列绑定交换机
     *
     *
     */
    @Bean
    public Binding orderListenerBinding(Queue orderListenerQueue ,Exchange  orderListenerExchange){
        /***
         *这句话的意思是:把队列 orderListenerQueue 通过 orderListenerQueue(routing_key)的规则绑定到 orderListenerExchange (交换机上)
         */
        return BindingBuilder.bind(orderListenerQueue).to(orderListenerExchange).with("orderListenerQueue").noargs();
    }
}
