package com.changgou.order.controller;

import com.changgou.entity.TokenDecode;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import com.github.pagehelper.PageInfo;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/****
 * @Author:www.itheima.com
 * @Description:
 * @Date
 *****/

@RestController
@RequestMapping("/order")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    /*****
     * 用户确认收货 用户点击确认收货后修改订单的是否收货确认信息，修改订单的为已完成状态
     * @param id
     * @return
     */
    @GetMapping("/eceiving/{id}")
    public Result receivingOk(@PathVariable (value = "id") String id){
        //获取用户登录的登录名
        String username = TokenDecode.getUserInfo().get("username");
        try {
            orderService.receivingOk(username,id);
            return new Result(true,StatusCode.OK,"确认收货成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,StatusCode.ERROR,"确认收货失败",e.getMessage());
        }
    }


    /***
     *1.用户点击提醒发货，那么前端要传递一个时间对象到后台(时间格式是 yyyyy-MM-dd HH:mm:ss)，并且和该订单的id
     *2.后端可用获取登录用户的用户信息
     *3.后端返回 0：提醒发货失败,并把失败的原因返回给前端
     *          1：表示提醒发货成功 修改提醒发货的时间
     */
    @PostMapping("/remind")
    public Result remindGoods(@RequestBody Map<String,String> map){
        //获取当前用户登录信息 并保存到map集合中一并传递给OrderService
        String username = TokenDecode.getUserInfo().get("username");
        map.put("username",username);
        try {
            orderService.remindGoods(map);
            return new Result(true,StatusCode.OK,"提醒发货成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,StatusCode.ERROR,"提醒发货失败",e.getMessage());
        }
    }

    /****
     *
     * 查询出该用户所对应的所有数据(OrderItem)
     *
     *
     */
@GetMapping("/getitem/{id}")
public Result<List<OrderItem>> getOrderToOrderItem(@PathVariable(value = "id") String id){
    List<OrderItem> orderItems = orderService.selectOrderItems(id);
    return new Result<>(true,StatusCode.OK,"订单商品数据查询成功",orderItems);
}




    /****
     * 用户在我的订单 页面点击取消订单(前端传递该订单的id到后台)
     * @param id
     * @return
     */
    @PostMapping("/cancelorder/{id}")
    public Result cancelOrder ( @PathVariable(value = "id") String id ){
        orderService.cancelOrder(id);
        return new Result(true,StatusCode.OK,"订单取消成功");
    }




    /****
     * 查询当前登录用户的订单信息、
     *
     * @return
     */
    @GetMapping("selectorder")
    public Result selectOrderUsername(){

        //获取到当前用户登录信息，从中获取用户名
        String username = TokenDecode.getUserInfo().get("username");
        //查询该用户的订单信息
        List<Order> orders =  orderService.selectOrderUsername(username);
        return new Result<List<Order>>(true,StatusCode.OK,"用户订单信息查询成功",orders);
    }



    /***
     * 新增Order数据
     * @param order
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Order order){
        //获取当前用户登录的用户名信息
        String username = TokenDecode.getUserInfo().get("username");
        //设置到order中
        order.setUsername(username);
        //调用orderService 完成订单的下单操作
        orderService.add(order);
        return new Result(true,StatusCode.OK,"订单下单成功");
    }






    /***
     * Order分页条件搜索实现
     * @param order
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false)  Order order, @PathVariable  int page, @PathVariable  int size){
        //调用OrderService实现分页条件查询Order
        PageInfo<Order> pageInfo = orderService.findPage(order, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * Order分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用OrderService实现分页查询Order
        PageInfo<Order> pageInfo = orderService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param order
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Order>> findList(@RequestBody(required = false)  Order order){
        //调用OrderService实现条件查询Order
        List<Order> list = orderService.findList(order);
        return new Result<List<Order>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        //调用OrderService实现根据主键删除
        orderService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改Order数据
     * @param order
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody  Order order,@PathVariable String id){
        //设置主键值
        order.setId(id);
        //调用OrderService实现修改Order
        orderService.update(order);
        return new Result(true,StatusCode.OK,"修改成功");
    }



    /***
     * 根据ID查询Order数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Order> findById(@PathVariable String id){
        //调用OrderService实现根据主键查询Order
        Order order = orderService.findById(id);
        return new Result<Order>(true,StatusCode.OK,"查询成功",order);
    }

    /***
     * 查询Order全部数据
     * @return
     */
    @GetMapping
    public Result<List<Order>> findAll(){
        //调用OrderService实现查询所有Order
        List<Order> list = orderService.findAll();
        return new Result<List<Order>>(true, StatusCode.OK,"查询成功",list) ;
    }
}
