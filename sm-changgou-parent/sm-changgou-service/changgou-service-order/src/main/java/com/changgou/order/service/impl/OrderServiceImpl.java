package com.changgou.order.service.impl;

import com.changgou.entity.IdWorker;
import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Para;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import com.changgou.user.feign.UserFeign;
import com.changgou.user.pojo.User;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/****
 * @Author:www.itheima.com
 * @Description:Order业务层接口实现类
 * @Date www.itheima.com 0:16
 *****/
@Service
//@Transactional事务控制
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IdWorker idWorker;
    /**
     * 为了查询当前用户的购物车商品数据
     */
    @Autowired
    private RedisTemplate redisTemplate;
    /***
     * 添加订单明细(主要 作用)
     */
    @Autowired
    private OrderItemMapper orderItemMapper;

    /***
     * 注入SkuFeign(对应商品的库存数量 递减)
     */
    @Autowired
    private SkuFeign skuFeign;
    /***
     * 注入UserFeign 对对应的下单 用户增加10个积分
     */
    @Autowired
    private UserFeign userFeign;


    /***
     * 延时队列处理超时订单 注入RabbitTemplate
     *
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /***
     * 用户点击取消订单
     * @param id 订单id
     */
    @GlobalTransactional//开启分布式事务
    @Override
    public void cancelOrder(String id) {
        //1.根据订单id查询出所有的 OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(id);
        Map<String, Object> skuIdsAndNumMap = skuIdsAndNumMap(id);
        //2.回滚该商品对应的库存数据
        skuFeign.addCount(skuIdsAndNumMap);
        //3.回滚用户对应的积分数据 //查询出该订单用户
        Order order = orderMapper.selectByPrimaryKey(id);
        String username = order.getUsername();
        Result<User> result =userFeign.findById(username);
        User user = result.getData();
        user.setPoints(user.getPoints()+1);
        //更新用户信息
        userFeign.update(user,username);
        //4.删除订单
        orderMapper.deleteByPrimaryKey(id);
    }

    /***
     * 判断支付失败的订单在一定时间后是否支付成功
     * @param order
     * @return
     */
    @Override
    public int checkWXPay(Order order) {
        String payStatus = order.getPayStatus();
        if ("1".equals(payStatus)){
            //说明支付成功
            return 1;
        }
        return 0;
    }

    /***
     * 查询用户的订单信息
     * @param username
     * @return
     */
    @Override
    public List<Order> selectOrderUsername(String username) {
        Order order = new Order();
        order.setUsername(username);
        return orderMapper.select(order);
    }

    /***
     * 根据orderId查询出对应的 skuId(key) , num(value)
     * @param orderId
     * @return
     */
    @Override
    public Map<String, Object> skuIdsAndNumMap(String orderId) {
        //创建map 封装返回数据
        Map<String, Object> map = new HashMap<String, Object>();
        //查询对应的OrderItem
        OrderItem orderItem = new OrderItem();
        //将orderId设置到OrderItem中(作为查询条件)
        orderItem.setOrderId(orderId);
        List<OrderItem> orderItems = orderItemMapper.select(orderItem);

        //遍历orderItems获取对应的sku和num 封装成map
        if (orderItems != null && orderItems.size() > 0) {
            for (OrderItem item : orderItems) {
                Long skuId = item.getSkuId();
                String skuidstr = skuId.toString();
                map.put(skuidstr, item.getNum());
            }

        }
        return map;
    }

    /***
     * 删除对应的订单数据 ，删除订单的同时也要删除对应订单的orderItem数据
     * @param orderId
     */
    @Override
    public void isDelete(String orderId) {

        //1.删除对应的order数据(逻辑删除设置is_delete为 1 )
        //Order order = orderMapper.selectByPrimaryKey(orderId);
        //order.setIsDelete("1");
        //更新数据
        //orderMapper.updateByPrimaryKeySelective(order);

        //物理删除 todo OrderItem 没有设置逻辑删除的 判断列(需要设置为逻辑删除的列)
        orderMapper.deleteByPrimaryKey(orderId);
        //2.根据orderId 删除对应的orderItem数据(逻辑删除)
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        //删除orderItem
        orderItemMapper.delete(orderItem);

    }

    /***
     * 根据订单号查询该订单号对应的全部商品数据 List<orderItem>(回滚仓库)
     * @param out_trade_no
     * @return
     */
    @Override
    public List<OrderItem> selectOrderItems(String out_trade_no) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(out_trade_no);
        return orderItemMapper.select(orderItem);
    }

    /***
     * 修改订单状态
     * @param out_trade_no 商品订单号
     * @param time_end 支付完成时间
     * @param transaction_id 微信支付订单号
     */
    @Override
    public void updateStatus(String out_trade_no, String time_end, String transaction_id) throws Exception {

        //根据outtradeon (订单号)查询出订单
        Order order = orderMapper.selectByPrimaryKey(out_trade_no);
        //设置订单完成时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = simpleDateFormat.parse(time_end);
        order.setEndTime(date);
        //设置支付订单号
        order.setTransactionId(transaction_id);
        //更新支付状态
        order.setPayType("1");
        //更新tb_order表
        orderMapper.updateByPrimaryKeySelective(order);

    }

    /**
     * 增加Order  当前order中有用户名 订单表和商品详情是一对多的关系
     *
     * @param order
     */
    @GlobalTransactional
    @Override
    public void add(Order order) {
        //查询相关数据(redis)
        //获取到客户勾选的所有的商品id，然后根据id去redis中取出对应的数据(需要下单的商品)
        Long[] skuIds = order.getSkuIds();
        //定义集合储存每个对应的orderItem
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        for (Long skuId : skuIds) {
            //从redis中查询数据
            //添加到集合(orderItems)
            orderItems.add((OrderItem) redisTemplate.boundHashOps("cat_" + order.getUsername()).get(skuId));

            //因为下单成功后需要删除redis中的对应商品(orderItem->京东商城的做法)
            //  redisTemplate.boundHashOps("cat_"+order.getUsername()).delete(skuId);
        }

        //统计计算
        //总金额
        int totalMoney = 0;
        //总商品数量
        int num = 0;

        //订单下单成功后，那么对应的商品的库存 容量也必须要递减 (递减数量= 原有数量（num）-该商品的购买个数（num）) ,要通过Feign调用goods服务
        //需要的参数是：skuId，购买数量定义一个map集合key为商品的skuId value为 购买数量
        Map<String, Object> decrmap = new HashMap<String, Object>();
        //遍历orderItems集合完善order数据
        for (OrderItem orderItem : orderItems) {
            num += orderItem.getNum();
            totalMoney += orderItem.getMoney();
            //订单下单成功后，那么对应的商品的库存 容量也必须要递减 (递减数量= 原有数量（num）-该商品的购买个数（num）)
            decrmap.put(orderItem.getSkuId().toString(), orderItem.getNum());
        }//总商品数量
        order.setTotalNum(num);
        //总金额
        order.setTotalMoney(totalMoney);
        //完善相关信息
        //订单添加1次
        order.setCreateTime(new Date());    //创建时间
        order.setUpdateTime(order.getCreateTime()); //修改时间
        order.setSourceType("1");       //订单来源  1:web
        order.setOrderStatus("0");     //0未支付
        order.setPayStatus("0");      //0未支付
        order.setIsDelete("0");      //0 未删除
        order.setId(String.valueOf(idWorker.nextId()));
        //添加订单明细
        for (OrderItem orderItem : orderItems) {
            orderItem.setId(String.valueOf(idWorker.nextId()));
            orderItem.setOrderId(order.getId());
            orderItemMapper.insertSelective(orderItem);
        }
        //返回的是下单成功的订单数量
        int count = orderMapper.insertSelective(order);

        //调用 SkuFeign执行库存递减
        skuFeign.decrCount(decrmap);
        //用户下单后那么要对对应的用户增加10个积分(userFeign)
        userFeign.addUserPoints(order.getUsername(), 10);

        /***
         * 注解：todo   (一个参数必须也要加注解)
         *          @RequestParam("name") String username 将参数中的name值赋值给username (从传递的参数中获取对应的值)
         *          @GetMapping(value = "/search/{page}/{size}" ) @PathVariable(value="page") int page 从路径上将page的值赋值给 int page
         *          @requestBody 接收pojo 或者json数据
         */

        //todo 延时队列处理超时订单
        /***
         * par1:需要延时的队列
         * par2:消息
         * par3：延时队列的设置；设置延时读取
         */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("创建订单时间：" + simpleDateFormat.format(new Date()));
        rabbitTemplate.convertAndSend("orderDelayQueue", (Object) order.getId(), new MessagePostProcessor() {
            //延时(超时队列)的设置
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                // setExpiration :设置需要延时的时间 Expiration：截至时间 单位是 毫秒
                message.getMessageProperties().setExpiration("300000000");
                return message;
            }
        });

    }

    /**
     * Order条件+分页查询
     *
     * @param order 查询条件
     * @param page  页码
     * @param size  页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> findPage(Order order, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(order);
        //执行搜索
        return new PageInfo<Order>(orderMapper.selectByExample(example));
    }

    /**
     * Order分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Order> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Order>(orderMapper.selectAll());
    }

    /**
     * Order条件查询
     *
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order) {
        //构建查询条件
        Example example = createExample(order);
        //根据构建的条件查询数据
        return orderMapper.selectByExample(example);
    }


    /**
     * Order构建查询对象
     *
     * @param order
     * @return
     */
    public Example createExample(Order order) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (order != null) {
            // 订单id
            if (!StringUtils.isEmpty(order.getId())) {
                criteria.andEqualTo("id", order.getId());
            }
            // 数量合计
            if (!StringUtils.isEmpty(order.getTotalNum())) {
                criteria.andEqualTo("totalNum", order.getTotalNum());
            }
            // 金额合计
            if (!StringUtils.isEmpty(order.getTotalMoney())) {
                criteria.andEqualTo("totalMoney", order.getTotalMoney());
            }
            // 优惠金额
            if (!StringUtils.isEmpty(order.getPreMoney())) {
                criteria.andEqualTo("preMoney", order.getPreMoney());
            }
            // 邮费
            if (!StringUtils.isEmpty(order.getPostFee())) {
                criteria.andEqualTo("postFee", order.getPostFee());
            }
            // 实付金额
            if (!StringUtils.isEmpty(order.getPayMoney())) {
                criteria.andEqualTo("payMoney", order.getPayMoney());
            }
            // 支付类型，1、在线支付、0 货到付款
            if (!StringUtils.isEmpty(order.getPayType())) {
                criteria.andEqualTo("payType", order.getPayType());
            }
            // 订单创建时间
            if (!StringUtils.isEmpty(order.getCreateTime())) {
                criteria.andEqualTo("createTime", order.getCreateTime());
            }
            // 订单更新时间
            if (!StringUtils.isEmpty(order.getUpdateTime())) {
                criteria.andEqualTo("updateTime", order.getUpdateTime());
            }
            // 付款时间
            if (!StringUtils.isEmpty(order.getPayTime())) {
                criteria.andEqualTo("payTime", order.getPayTime());
            }
            // 发货时间
            if (!StringUtils.isEmpty(order.getConsignTime())) {
                criteria.andEqualTo("consignTime", order.getConsignTime());
            }
            // 交易完成时间
            if (!StringUtils.isEmpty(order.getEndTime())) {
                criteria.andEqualTo("endTime", order.getEndTime());
            }
            // 交易关闭时间
            if (!StringUtils.isEmpty(order.getCloseTime())) {
                criteria.andEqualTo("closeTime", order.getCloseTime());
            }
            // 物流名称
            if (!StringUtils.isEmpty(order.getShippingName())) {
                criteria.andEqualTo("shippingName", order.getShippingName());
            }
            // 物流单号
            if (!StringUtils.isEmpty(order.getShippingCode())) {
                criteria.andEqualTo("shippingCode", order.getShippingCode());
            }
            // 用户名称
            if (!StringUtils.isEmpty(order.getUsername())) {
                criteria.andLike("username", "%" + order.getUsername() + "%");
            }
            // 买家留言
            if (!StringUtils.isEmpty(order.getBuyerMessage())) {
                criteria.andEqualTo("buyerMessage", order.getBuyerMessage());
            }
            // 是否评价
            if (!StringUtils.isEmpty(order.getBuyerRate())) {
                criteria.andEqualTo("buyerRate", order.getBuyerRate());
            }
            // 收货人
            if (!StringUtils.isEmpty(order.getReceiverContact())) {
                criteria.andEqualTo("receiverContact", order.getReceiverContact());
            }
            // 收货人手机
            if (!StringUtils.isEmpty(order.getReceiverMobile())) {
                criteria.andEqualTo("receiverMobile", order.getReceiverMobile());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(order.getReceiverAddress())) {
                criteria.andEqualTo("receiverAddress", order.getReceiverAddress());
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (!StringUtils.isEmpty(order.getSourceType())) {
                criteria.andEqualTo("sourceType", order.getSourceType());
            }
            // 交易流水号
            if (!StringUtils.isEmpty(order.getTransactionId())) {
                criteria.andEqualTo("transactionId", order.getTransactionId());
            }
            // 订单状态,0:未完成,1:已完成，2：已退货
            if (!StringUtils.isEmpty(order.getOrderStatus())) {
                criteria.andEqualTo("orderStatus", order.getOrderStatus());
            }
            // 支付状态,0:未支付，1：已支付，2：支付失败
            if (!StringUtils.isEmpty(order.getPayStatus())) {
                criteria.andEqualTo("payStatus", order.getPayStatus());
            }
            // 发货状态,0:未发货，1：已发货，2：已收货
            if (!StringUtils.isEmpty(order.getConsignStatus())) {
                criteria.andEqualTo("consignStatus", order.getConsignStatus());
            }
            // 是否删除
            if (!StringUtils.isEmpty(order.getIsDelete())) {
                criteria.andEqualTo("isDelete", order.getIsDelete());
            }
        }
        return example;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Order
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID查询Order
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Order全部数据
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }
}
