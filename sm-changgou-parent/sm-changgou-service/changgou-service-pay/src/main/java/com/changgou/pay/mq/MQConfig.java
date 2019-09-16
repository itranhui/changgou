package com.changgou.pay.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.EventListener;

/*****
 * @Author:
 * @Description:
 ****/
@Configuration
public class MQConfig {

    /***
     * 读取配置文件中的信息的对象
     */
    @Autowired
    private Environment env;

    /****
     * 创建队列
     */
    @Bean
    public Queue orderQueue() {
        //String queue = env.getProperty("mq.pay.queue.order");
        //return new Queue(queue);
        return new Queue("queue.order");
    }


    /***
     * 创建交换机
     */
    @Bean
    public Exchange orderExchange() {
        //DirectExchange 路由交换机 (routing_key完全匹配)
        // TopicExchange 主题交换机(也是通配符交换机 # ：多个 * ：一个)
        return new DirectExchange("exchange.order", true, false);
    }

    /***
     * 队列绑定交换机
     */
    @Bean
    public Binding orderQueueExchange( Queue orderQueue,  Exchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with("queue.order").noargs();
    }





        /*================================================秒杀队列的创建==============================*/


        /****
         * 创建队列
         */
        @Bean
        public Queue orderSeckillQueue () {
            //String queue = env.getProperty("mq.pay.queue.order");
            //return new Queue(queue);
            return new Queue(env.getProperty("mq.pay.queue.seckillorder"));
        }


        /***
         * 创建交换机
         */
        @Bean
        public Exchange orderSeckillExchange () {
            //DirectExchange 路由交换机 (routing_key完全匹配)
            // TopicExchange 主题交换机(也是通配符交换机 # ：多个 * ：一个)
            return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"), true, false);
        }

        /***
         * 队列绑定交换机
         */
        @Bean
        public Binding orderSeckillQueueExchange (Queue orderSeckillQueue, Exchange orderSeckillExchange){
            return BindingBuilder.bind(orderSeckillQueue).to(orderSeckillExchange).with(env.getProperty("mq.pay.routing.seckillkey")).noargs();
        }


    }


