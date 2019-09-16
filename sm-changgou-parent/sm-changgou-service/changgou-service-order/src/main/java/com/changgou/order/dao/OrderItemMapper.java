package com.changgou.order.dao;
import com.changgou.order.pojo.OrderItem;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:www.itheima.com
 * @Description:OrderItem的Dao
 * @Date
 *****/
@Component
public interface OrderItemMapper extends Mapper<OrderItem> {
}
