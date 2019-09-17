package com.changgou.goods.service.impl;

import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.service.SkuService;
import com.changgou.goods.timer.CollectGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/8/31 15:21
 * <p>
 * @Description：
 */
@Service
public class SkuServiceImpl implements SkuService {


    //注入Dao
    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    /***
     * 新增我的收藏考虑并发问题，将用户对应的添加收藏先写入到redis中 然后再定时的将redis中的数据写入
     * @param map
     */
    @Override
    public void collect(Map<String, Object> map) {
        //先将用户的收藏添加到redis中采用hash数据格式   // todo 使用list
        String username = (String) map.get("username");
        String skuId =  map.get("skuId").toString();
      redisTemplate.boundListOps(username).leftPush(skuId);
    }

    /***
     * 库存回滚
     * @param skuIdsAndNumMap
     */
    @Override
    public void addCount(Map<String, Object> skuIdsAndNumMap) {

        // 数据库中每条记录都拥有行级锁,此时只能允许一个事务修改该记录，只有等该事务结束后，其他事务才能操作该记录
        for (Map.Entry<String, Object> entry : skuIdsAndNumMap.entrySet()) {
            Long skuId = Long.valueOf(entry.getKey());
            String value = (String)entry.getValue();
            Integer num = Integer.parseInt(value);
            int count =  skuMapper.addCount(skuId,num);
        }
    }

    /***
     * 库存递减
     */
    @Override
    public void decrCount(Map<String, Object> decrmap) {
        //遍历该集合执行递减操作
        //采用行级锁控制超卖  update tb_sku set num=num-#{num} where id=#{id} and num>=#{num}
        // 数据库中每条记录都拥有行级锁,此时只能允许一个事务修改该记录，只有等该事务结束后，其他事务才能操作该记录
        for (Map.Entry<String, Object> entry : decrmap.entrySet()) {
            Long skuId = Long.valueOf(entry.getKey());
            String value = (String)entry.getValue();
            Integer num = Integer.parseInt(value);
            int count =  skuMapper.decrCount(skuId,num);
            //说明库存不足递减失败
            if (count<=0){
                throw  new RuntimeException("库存不足，递减失败");
            }
        }
    }

    /*********
     * 查询全部sku
     * @return
     */
    @Override
    public List<Sku> importData() {
        List<Sku> skuList = skuMapper.selectAll();
        return skuList;
    }
    /***
     * 多条件搜索品牌数据
     * @param sku
     * @return
     */
    @Override
    public List<Sku> findList(Sku sku) {
        return skuMapper.select(sku);
    }

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @Override
    public Sku findById(Long id) {
        return skuMapper.selectByPrimaryKey(id);
    }
}
