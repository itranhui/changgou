package com.changgou.seckill.task;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.IdWorker;
import com.changgou.entity.SeckillStatus;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author：Mr.ran &Date：2019/9/12 20:29
 * <p>
 * @Description：
 */
@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 给延时队列发送消息
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /***
     * 获取配置文件对象数据
     *
     */

    @Autowired
    private Environment env;


    /**
     * 在需要异步执行的方法上面加上这个注解 @Async
     */
    @Async
    public void createOrder() {
        try {


            System.out.println("准备睡一下在下单");
            Thread.sleep(10000);

            //从redis中取出 SeckillOrderQueue队列 消费 取出来的数据将会从redis中移出  因为消费了(刚刚是从左边存的，那么现在从右边取)
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
            //因为是多线程 不一定调用该 seckillStatus 就一定有值要判断是否为null
            if (seckillStatus == null) {
                return;
            }

            //下单之前先从redis中获取一下 seckillGoodsId数据如果存在说明有库存可以下单，不存在说明没有库存不能下单
            Object along = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();
            if (along == null) {
                //说明队列中已经没有对应的数据了 不能下单 那么就要删除该用户的排队信息
                clearUserQueue(seckillStatus.getUsername());
                return;
            }

            //说明SeckillGoodsCountList_id 中有数据 可以下单


            //现在该 SeckillStatus 中就有秒杀下单用户的相关消息
            String username = seckillStatus.getUsername();
            String time = seckillStatus.getTime();
            Long id = seckillStatus.getGoodsId();
            //查询出 该秒杀商品
            SeckillGoods seckillGood = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
            //判断该商品是否还有库存，如果没有直接返回一个异常
            if (seckillGood == null || seckillGood.getStockCount() <= 0) {
                throw new RuntimeException("已售罄");
            }
            //说明有库存创建秒杀商品订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            //秒杀商品id
            seckillOrder.setSeckillId(seckillGood.getId());
            //支付金额
            seckillOrder.setMoney(seckillGood.getCostPrice());
            //购买用户
            seckillOrder.setUserId(username);
            //订单创建时间
            seckillOrder.setCreateTime(new Date());
            //未支付 0
            seckillOrder.setStatus("0");
            //将秒杀订单存放到redis中 采用hash数据格式SeckillOrder作为namespace,username作为key
            redisTemplate.boundHashOps("SeckillOrder_").put(username, seckillOrder);
            //库存递减更新redis中的SeckillGoods数据
            seckillGood.setStockCount(seckillGood.getStockCount() - 1);

            //如果redis中的秒杀商品的库存小于等于0的话说明已经卖完了,那么就不应该留在秒杀商品的列表中所以要删除
            if (seckillGood.getStockCount() <= 0) {
                //并且也要将秒杀商品的数据告知数据库(因为我们设置的定时任务是每个30秒从数据库中加载秒杀商品到redis告知数据库该商品已经卖完不要加载到redis中)
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGood);
                redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);

            } else {
                //说明还有库存重置数据到redis中
                redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, seckillGood);
            }
            //更新下单状态
            seckillStatus.setStatus(2);//抢单成功
            seckillStatus.setOrderId(seckillOrder.getId());//订单id
            seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney()));
            //更新redis中的下单状态的数据
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            System.out.println("下单时间：" + simpleDateFormat.format(new Date()));
            //将消息先发送给MQ(延时队列，设置过期时间为10s)
            rabbitTemplate.convertAndSend(env.getProperty("mq.pay.routing.delayroutingkey"), (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setExpiration("10000");
                    return message;
                }
            });


            System.out.println("下单成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 清除用户的排队信息相关数据
     *
     */
    public void clearUserQueue(String username) {
        //排队标识
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //排队信息清理掉
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);

    }


}

