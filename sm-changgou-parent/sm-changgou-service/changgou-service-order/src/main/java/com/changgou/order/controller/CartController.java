package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.order.controller
 * 购物车操作
 ****/
@RestController
@RequestMapping(value = "/cart")
public class CartController {

    @Autowired
    private CartService cartService;


    /**
     * 注入TokenDecode
     */

    /***
     * 购物车列表
     */
    @GetMapping(value = "/list")
    public Result<List<OrderItem>> list(){
         //用户名
       // String username="szitheima";
        /**
         * 获取当前登录用户的用户名
         */
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        System.out.println(userInfo);
        return new Result<>(true,StatusCode.OK,"查询成功",cartService.list(username));
    }

    /****
     * 加入购物车
     * 1:加入购物车数量
     * 2:商品ID
     */
    @GetMapping(value = "/add")
    public Result add( Integer num,  Long id){
        //获取用户登录的用户名
        //将商品加入购物车
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        cartService.add(num,id,username);
        return new Result(true, StatusCode.OK,"加入购物车成功！");

    }
}
