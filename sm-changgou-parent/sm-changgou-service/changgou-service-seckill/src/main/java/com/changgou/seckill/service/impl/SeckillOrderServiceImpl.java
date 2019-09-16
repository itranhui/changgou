package com.changgou.seckill.service.impl;

import com.changgou.entity.DateUtil;
import com.changgou.entity.IdWorker;
import com.changgou.entity.SeckillStatus;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.task.MultiThreadingCreateOrder;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {


    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;


    /****
     * 支付失败，回滚库存,删除订单
     * @param username
     */
    @Override
    public void deleteOrder(String username) {
        //删除该用户在redis中所对应的订单
        redisTemplate.boundHashOps("SeckillOrder_").delete(username);
        //查询出用户的排队消息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get("username");
        //删除用户的排队消息
        clearUserQueue(username);
        //回滚库存->redis中的，但是redis中不一定有该商品了(可能被其他用户下单了)
        String namespace = "SeckillGoods_"+seckillStatus.getTime();
        SeckillGoods seckillGoods  = (SeckillGoods) redisTemplate.boundHashOps(namespace).get(seckillStatus.getGoodsId());
        //判断该seckillGoods是否还有商品
        if (seckillGoods==null){
            //数据库中查询该商品
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            //回滚库存更新数据库中的库存
            seckillGoods.setStockCount(1);
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        }else {
            //说明redis中还有该商品，那么就直接在原有的商品的基础上加1
            seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
            //更新redis中的缓存秒杀商品数据
            redisTemplate.boundHashOps(namespace).put(seckillGoods.getId(),seckillGoods);
        }
        //队列中的数据也要增加1(因为队列中的数据个数是根据该秒杀商品的库存来的 )
        redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGoods.getId()).leftPush(seckillGoods.getId());
    }

    /****
     * 用户支付成功后修改订单状态
     * @param username
     * @param transaction_id
     * @param time_end
     */
    @Override
    public void updatePayStatus(String username, String transaction_id, String time_end) {
        //1.从redis中把订单数据查询出来
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder_").get(username);
        //修改订单状态
        seckillOrder.setStatus("1");//已经支付
        //增加支付的 transaction_id 微信支付订单号
        seckillOrder.setTransactionId(transaction_id);
        //增加支付时间
        seckillOrder.setPayTime(getPayTime(time_end));
        //存入数据库
        seckillOrderMapper.insertSelective(seckillOrder);
        //清除redis中对应该订单的数据 username对应的订单数据
        redisTemplate.boundHashOps("SeckillOrder_").delete(username);
        //还要清除redis中的关于该用户的排队消息(如果不清除用户永远都买不了东西)
        clearUserQueue(username);

    }

    public Date getPayTime(String strTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date parse = null;
        try {
            parse = simpleDateFormat.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parse;
    }

    /**
     * 下单状态的查询
     *
     * @param username
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }

    /***
     * 添加秒杀订单
     * @param id:商品ID
     * @param time:商品秒杀开始时间
     * @param username:用户登录名
     * @return
     */
    @Override
    public Boolean add(Long id, String time, String username) {
        //防止用户重复排队(也就是防止用户重复抢购商品)，只要用户点击抢购那就会进入到排队中，我们可以设置一个自增值，让该值的初始值为1，每次自增+1 如果该用户对应的自增值大于1的话那么就表示是重复排队(重复抢购)抛出一个异常  100 表示重复排队
        //返回的是自增后的值
        Long aLong = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if (aLong > 1) {
            //说明是重复排队，抛出一个异常100表示
            throw new RuntimeException("100");
        }


        //创建redis队列采用list
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);
        //将该队列存入到redis中 采用list队列先存先取(先抢单的用户就先有机会去抢单 )
        //par1: list的名称
        //par2：list队列的值
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        //设置用户抢单状态用户查询
        redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
        //将抢单状态存到redis中

        //多线程下单
        multiThreadingCreateOrder.createOrder();

        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     *
     * @param seckillOrder 查询条件
     * @param page         页码
     * @param size         页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     *
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder) {
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     *
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder) {
        Example example = new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if (seckillOrder != null) {
            // 主键
            if (!StringUtils.isEmpty(seckillOrder.getId())) {
                criteria.andEqualTo("id", seckillOrder.getId());
            }
            // 秒杀商品ID
            if (!StringUtils.isEmpty(seckillOrder.getSeckillId())) {
                criteria.andEqualTo("seckillId", seckillOrder.getSeckillId());
            }
            // 支付金额
            if (!StringUtils.isEmpty(seckillOrder.getMoney())) {
                criteria.andEqualTo("money", seckillOrder.getMoney());
            }
            // 用户
            if (!StringUtils.isEmpty(seckillOrder.getUserId())) {
                criteria.andEqualTo("userId", seckillOrder.getUserId());
            }
            // 创建时间
            if (!StringUtils.isEmpty(seckillOrder.getCreateTime())) {
                criteria.andEqualTo("createTime", seckillOrder.getCreateTime());
            }
            // 支付时间
            if (!StringUtils.isEmpty(seckillOrder.getPayTime())) {
                criteria.andEqualTo("payTime", seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if (!StringUtils.isEmpty(seckillOrder.getStatus())) {
                criteria.andEqualTo("status", seckillOrder.getStatus());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(seckillOrder.getReceiverAddress())) {
                criteria.andEqualTo("receiverAddress", seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if (!StringUtils.isEmpty(seckillOrder.getReceiverMobile())) {
                criteria.andEqualTo("receiverMobile", seckillOrder.getReceiverMobile());
            }
            // 收货人
            if (!StringUtils.isEmpty(seckillOrder.getReceiver())) {
                criteria.andEqualTo("receiver", seckillOrder.getReceiver());
            }
            // 交易流水
            if (!StringUtils.isEmpty(seckillOrder.getTransactionId())) {
                criteria.andEqualTo("transactionId", seckillOrder.getTransactionId());
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
    public void delete(Long id) {
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     *
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     *
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }

    /***
     * 清理用户排队抢单信息
     */
    public void clearUserQueue(String username) {
        //排队标识
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //排队信息清理掉
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }
}
