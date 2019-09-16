package com.changgou.seckill.dao;
import com.changgou.seckill.pojo.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:
 * @Description:SeckillGoods的Dao
 * @Date 2019/6/14 0:12
 *****/
@Component
public interface SeckillGoodsMapper extends Mapper<SeckillGoods> {
}
