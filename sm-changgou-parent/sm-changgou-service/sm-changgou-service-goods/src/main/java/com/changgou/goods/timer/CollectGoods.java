package com.changgou.goods.timer;

import com.changgou.entity.TokenDecode;
import com.changgou.goods.dao.CollectMapper;
import com.changgou.goods.pojo.Collect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/17 11:28
 * <p>
 * @Description：定时将redis中的数据同步到数据库中(也可以不同步，用户的收藏就储存到redis中，)
 */
@Component
public class CollectGoods {
    /***
     * 注入redisTemplate
     *
     */
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 添加收藏记录
     */
    @Autowired
    private CollectMapper collectMapper;

    /**
     * 每20秒钟将redis中的数据执行到mysql中
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void CollectToGoods() {
   String username = "zhangsan";
        String skuId = (String) redisTemplate.boundListOps(username).rightPop();
        if (skuId == null) {
            return;
        }
        //更新数据库对应信息
        Collect collect = new Collect();
        collect.setSkuId(Long.valueOf(skuId));
        collect.setUsername(username);
        collectMapper.insert(collect);
    }
}
