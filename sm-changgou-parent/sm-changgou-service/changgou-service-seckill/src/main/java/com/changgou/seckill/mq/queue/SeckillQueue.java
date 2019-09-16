package com.changgou.seckill.mq.queue;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

/**
 * @Author：Mr.ran &Date：2019/9/14 21:04
 * <p>
 * @Description：利用延时队列的特性实现超时订单的处理(一个延时队列，一个正常队列监听延时队列)
 */
@Configuration
public class SeckillQueue {

    /***
     * 创建延时过期队列(延时队列)
     *
     *
     */
       @Bean
    public Queue orderSeckillDelayQueue(){
           return QueueBuilder
                   .durable("orderSeckillDelayQueue")//延时队列的名称
                   .withArgument("x-dead-letter-exchange","orderListenerSeckillExchange") //par1:消息超时进入死信队列，绑定死信队列交换机
                                                                                           //par2:死信队列的数据要要发送给哪个交换机？？
                   .withArgument("x-dead-letter-routing-key","orderListenerSeckillQueue")  //par1:交换机绑定路由key
                                                                                    //par2:路由key的值(需要接收死信队列的数据的队列名)
                   .build();
       }


    /***
     * 创建需要接收死信队列中的数据的队列
     */

    @Bean
    public Queue orderListenerSeckillQueue(){
        return new Queue("orderListenerSeckillQueue");
    }

    /***
     * 创建交换机
     *
     *
     */

    @Bean
    public Exchange orderListenerSeckillExchange(){
        return new DirectExchange("orderListenerSeckillExchange");
    }

    /***
     * 队列绑定交换机
     *
     *
     */

    @Bean
    public Binding orderListenerSeckillBinding(Queue  orderListenerSeckillQueue ,Exchange orderListenerSeckillExchange ){
                                                                                                    //这个说白了也就是队列的名称(路由key可以是队列的名称)
        return BindingBuilder.bind(orderListenerSeckillQueue).to(orderListenerSeckillExchange).with("orderListenerSeckillQueue").noargs();
    }


}
