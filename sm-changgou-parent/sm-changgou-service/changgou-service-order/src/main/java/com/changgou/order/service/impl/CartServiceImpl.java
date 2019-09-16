package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.order.service.impl
 ****/
@Service
public class CartServiceImpl implements CartService {

    //数据存入到哪台机器的Redis
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    /***
     * 购物车集合查询
     * @param username 用户登录名
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {

        return redisTemplate.boundHashOps("cat_"+username).values();
    }

    /***
     * 加入购物车
     * @param num
     * @param id
     */
    @Override
    public void add(Integer num, Long id, String username) {
        //执行删除操作
        if (num<=0){
            redisTemplate.boundHashOps("cat_"+username).delete(id);
            return;
        }
        //查询出sku信息
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();
        //获取spu信息
        Long spuId = sku.getSpuId();
        Result<Spu> spuResult = spuFeign.findById(spuId);
        Spu spu = spuResult.getData();

        //将sku，spu转换成orderItem
        OrderItem orderItem = createOrderItem(num, id, sku, spu);
        //将orderItem采用 hash数据格式保存到redis中
        redisTemplate.boundHashOps("cat_"+username).put(id,orderItem);

    }

    /***
     * 创建一个OrderItem对象
     * @param num
     * @param id
     * @param sku
     * @param spu
     * @return
     */
    public OrderItem createOrderItem(Integer num, Long id, Sku sku, Spu spu) {
        //将加入购物车的商品信息封装成OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId().toString());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        orderItem.setImage(spu.getImage());
        return orderItem;
    }

}
