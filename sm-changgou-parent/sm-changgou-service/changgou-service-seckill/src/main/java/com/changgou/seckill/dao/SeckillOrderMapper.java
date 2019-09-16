package com.changgou.seckill.dao;
import com.changgou.seckill.pojo.SeckillOrder;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author
 * @Description:SeckillOrder的Dao
 * @Date 2019/6/14 0:12
 *****/
@Component
public interface SeckillOrderMapper extends Mapper<SeckillOrder> {
}
