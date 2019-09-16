package com.changgou.order.dao;
import com.changgou.order.pojo.Order;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:www.itheima.com
 * @Description:Order的Dao
 * @Date www.itheima.com 0:12
 *****/
@Component
public interface OrderMapper extends Mapper<Order> {
}
