package com.changgou.seckill.pto;

import com.changgou.entity.SeckillStatus;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author：Mr.ran &Date：2019/9/14 21:33
 * <p>
 * @Description：超时订单未支付，回滚库存，删除该用户的排队消息，删除订单
 */
@Component
public class RollBackAndDelete {
    /***
     * 注入RedisTemplate 操作缓存数据
     */
    @Autowired
    private RedisTemplate redisTemplate;


    /***
     * 对SeckillGoods进行操作
     *
     */
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /***
     * 超时订单的处理方法(总)
     * @param seckillStatus
     * @return 0：标识处理失败; 1：标识处理成功
     */
    public int deleteAndRoleBack(SeckillStatus seckillStatus) {
        try {
            //取出所有有需要的的数据
            String username = seckillStatus.getUsername();
            //删除改用户的排队消息
            clearUserQueue(username);
            //删除订单数据，回滚库存，队列
            redisTemplate.boundHashOps("SeckillOrder_").delete(username);
            //回滚库存对redis中的数据进行操作，但是这时候不一定redis中就有该数据 如果没有的话那么就操作数据库
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
            //判断是否有库存如果没有 那么就对数据库进行+1
            if (seckillGoods==null) {
                //在数据库中查询对应的seckillGoods数据(根据goodsId查询)
                 seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
                 //这个时候 seckillGoods对应的 StockCount 肯定为0
                seckillGoods.setStockCount(1);
                //更新对应的SeckillGoods
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
            }else {
                //说明reids中还有该商品的库存 那么就直接修改redis中的库存容量
                seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
                //更新redis数据
                redisTemplate.boundHashOps("SeckillGoods_"+seckillStatus.getTime()).put(seckillGoods.getId(),seckillGoods);
            }
            //由于我们之前防止超卖现象的发生 (该商品有多少个库存就存了多少个该商品对应的id)也要回滚
            redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGoods.getId()).leftPush(seckillGoods.getId());
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
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
