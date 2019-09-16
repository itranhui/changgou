package com.changgou.seckill.mq.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.SeckillStatus;
import com.changgou.seckill.pto.RollBackAndDelete;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author：Mr.ran &Date：2019/9/14 21:26
 * <p>
 * @Description：
 */
@Component
@RabbitListener(queues = "orderListenerSeckillQueue")
public class DelaySeckillMessageListener {


    /**
     * 注入超时订单处理类
     *
     */
@Autowired
private RollBackAndDelete rollBackAndDelete;


    /****
     * 监听的是接收死信队列  的  信息的队列
     *
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        System.out.println("接收消息的时间是: " + simpleDateFormat.format(new Date()));
        System.out.println("订单超时未支付的信息是："+message);
        //message转换成pojo->SeckillStatus
        SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);
        //回滚库存，删除订单，删除该用户的排队消息
        //0:表示处理失败，1：表示处理成功
        int roleBack = rollBackAndDelete.deleteAndRoleBack(seckillStatus);
        if (roleBack!=0){
            //处理成功  打印一句话
            System.out.println("超时订单处理成功");
        }else {
            throw  new RuntimeException("超时订单处理失败");
        }
    }
}
