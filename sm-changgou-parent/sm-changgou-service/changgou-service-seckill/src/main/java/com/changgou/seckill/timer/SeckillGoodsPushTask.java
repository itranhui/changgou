package com.changgou.seckill.timer;

import com.changgou.entity.DateUtil;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.security.KeyPair;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Author：Mr.ran &Date：2019/9/12 15:04
 * <p>
 * @Description：
 */
@Component
public class SeckillGoodsPushTask {


    /**
     * 注入redisTemplate
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询满足条件的所有的seckillGoods
     */
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /****
     * 每30秒执行一次
     * 将数据库中需要参与秒杀的商品存放到redis中
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis() {

        //存放到redis中 采用hash数据格式 ，date为namespace
        //遍历时间菜单 得到每个时间段信息
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date startTime : dateMenus) {
            //hash的namespace
            String namespace = "SeckillGoods_" + DateUtil.data2str(startTime, DateUtil.PATTERN_YYYYMMDDHH);

            //构建查询条件
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();

            //必须是已经审核通过的
            criteria.andEqualTo("status", "1");
            //秒杀商品的库存必须大于0
            criteria.andGreaterThan("stockCount", 0);
            //秒杀商品的开始时间必须大于或者等于活动的开始时间

            criteria.andGreaterThanOrEqualTo("startTime", startTime);
            //秒杀商品的结束时间必须小于开始时间+2
            criteria.andLessThan("endTime", DateUtil.addDateHour(startTime, 2));
            //之前已经添加到redis中的数据是不能再添加的，所有要防止再次添加
            Set keys = redisTemplate.boundHashOps( namespace).keys();
            if (keys != null && keys.size() > 0) {
                //组装条件
                criteria.andNotIn("id", keys);
            }
            //查询数据库 ，将满足 条件大的数据存放到redis中
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

            //秒杀商品存放redis
            for (SeckillGoods seckillGood : seckillGoods) {
                redisTemplate.boundHashOps(namespace).put(seckillGood.getId(), seckillGood);
                System.out.println("aaaaaaaaaa");
                /***
                 * //todo 解决超卖问题：利用redis的队列实现，给每件商品创建一个独立的商品个数队列（list->list特性从该种取出数据那么就会真正的取出来 ）(也就是把该商品的库存个数存放到redis中下单直接从redis中获取)，每次用户下单的时候先从redis中获取对应的商品信息，如果能取出数据那么就说明有库存，取不到说明就没有库存
                 * //todo：也可以采用自增建来完成(increment)自增-1 如果小于0说明没有库存(有弊端需要控制，可能一直负增长)
                 */
                //将秒杀商品的id存放到redis中采用list队列来储存

                //获取对应商品的ids(库存)
                Long[] ids = pushIds(seckillGood.getId(), seckillGood.getStockCount());
                //采用list队列寸存放到redis中(那么下单就从队列中取出数据)
                redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGood.getId()).leftPushAll(ids);//可变参数(也就是数组)
            }
        }
    }


    /***
     * 获取对应商品的id(用于存入redis中的队列防止超卖问题的产生)
     *
     * par1:对应秒杀商品的id
     * par2:对应秒杀商品的库存
     * 该方法储存了 len个id(该一个商品的对应的id)
     */
    public Long[] pushIds(Long id, int len) {
        Long[] ids = new Long[len];
        //遍历ids
        for (int i = 0; i < ids.length; i++) {
            ids[i] = id;
        }
        return ids;
    }

}
