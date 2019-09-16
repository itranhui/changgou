package com.changgou.order.service;

import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.github.pagehelper.PageInfo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/****
 * @Author:www.itheima.com
 * @Description:Order业务层接口
 * @Date www.itheima.com 0:16
 *****/
public interface OrderService {

    /***
     * Order多条件分页查询
     * @param order
     * @param page
     * @param size
     * @return
     */
    PageInfo<Order> findPage(Order order, int page, int size);

    /***
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Order> findPage(int page, int size);

    /***
     * Order多条件搜索方法
     * @param order
     * @return
     */
    List<Order> findList(Order order);

    /***
     * 删除Order
     * @param id
     */
    void delete(String id);

    /***
     * 修改Order数据
     * @param order
     */
    void update(Order order);

    /***
     * 新增Order
     * @param order
     */
    void add(Order order);

    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
     Order findById(String id);

    /***
     * 查询所有Order
     * @return
     */
    List<Order> findAll();

    /***
     * 修改订单状态
     * @param out_trade_no 商品订单号
     * @param time_end 支付完成时间
     * @param transaction_id 微信支付订单号
     */
    void updateStatus(String out_trade_no, String time_end, String transaction_id) throws Exception;

    /***
     * 根据订单号查询该订单号对应的全部商品数据 List<orderItem>(回滚仓库)
     * @param out_trade_no
     * @return
     */
    List<OrderItem> selectOrderItems(String out_trade_no);

    /***
     * 删除对应的订单数据 ，删除订单的同时也要删除对应订单的orderItem数据
     * @param orderId
     */
    void isDelete(String orderId);

    /***
     * 根据orderId查询出对应的 skuId(key) , num(value)
     * @param orderId
     * @return
     */
    Map<String, Object> skuIdsAndNumMap(String orderId);
}
