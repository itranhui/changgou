package com.changgou.goods.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.entity.TokenDecode;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/8/31 15:20
 * <p>
 * @Description：
 */
@RestController
@RequestMapping("/sku")
public class SkuController {

    //注入service
    @Autowired
    private SkuService skuService;

    /****
     * 新增我的收藏功能
     *
     * @param
     * @return
     */
    @PostMapping("/collect/{id}")
    public Result collect(@PathVariable ("id") Long id) {
        Map<String,Object> map = new HashMap<>();
        String username = TokenDecode.getUserInfo().get("username");
        map.put("username",username);
        map.put("skuId",id);
        skuService.collect(map);
        return new Result(true,StatusCode.OK,"添加我的收藏成功");
    }

    /***
     * 库存回滚
     * @param skuIdsAndNumMap
     */
    @PostMapping("/add/sku/num")
    public Result addCount(@RequestParam  Map<String, Object> skuIdsAndNumMap){
        skuService.addCount(skuIdsAndNumMap);
        return new Result(true,StatusCode.OK,"库存回滚成功");
    }

    /***
     * 库存递减
     * @param decrmap
     */
    @PostMapping("/decr/sku")
   public Result decrCount(@RequestParam Map<String, Object> decrmap){
        skuService.decrCount(decrmap);
        return new Result(true,StatusCode.OK,"库存递减成功");
    }

    /*******
     * 查询出全部的数据
     * @return
     */
    @GetMapping("/import/All")
    public Result importData(){
        List<Sku> skuList = skuService.importData();
        return new Result<List<Sku>>(true, StatusCode.OK,"查询成功",skuList);
    }

    /***
     * 多条件搜索品牌数据
     * @param sku
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<Sku>> findList(@RequestBody(required = false) Sku sku){
        List<Sku> skus =  skuService.findList(sku);
        return new Result<>(true,StatusCode.OK,"查询成功",skus);
    }

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable Long id){
         Sku sku = skuService.findById(id);
        return new Result<>(true,StatusCode.OK,"查询成功",sku);
    }
}
