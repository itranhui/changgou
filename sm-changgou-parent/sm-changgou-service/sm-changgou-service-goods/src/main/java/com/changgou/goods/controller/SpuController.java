package com.changgou.goods.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：Mr.ran &Date：2019/8/28 15:52
 * <p>
 * @Description：
 */
@RestController
@RequestMapping("/spu")
public class SpuController {


    @Autowired
    private SpuService spuService;

    /***
     * 添加Goods
     * @param goods
     * @return
     */
    @PostMapping("/save")
    public Result saveGoods(@RequestBody Goods goods) {
        spuService.saveGoods(goods);
        return new Result(true, StatusCode.OK, "保存成功");
    }

    /***
     * 根据ID查询Goods
     * @param id
     * @return
     */
    @GetMapping("/goods/{id}")
    public Result<Goods> findGoodsById(@PathVariable Long id) {
        //根据ID查询Goods(SPU+SKU)信息
        Goods goods = spuService.findGoodsById(id);
        return new Result<Goods>(true, StatusCode.OK, "查询成功", goods);
    }

    /****
     * 根据spuid 审核改该商品
     * @param id
     * @return
     */
    @PutMapping("/audit{id}")
    public Result audit(@PathVariable("id") Long id) {
        spuService.audit(id);
        return new Result(true, StatusCode.OK, "审核成功");
    }

    /*****
     * 商品下架
     * @param id
     * @return
     */
    @PutMapping("/pull/{id}")
    public Result pull(@PathVariable(value = "id") Long id){
    spuService.pull(id);
        return new Result(true,StatusCode.OK,"下架成功");
    }

    /******
     * 商品上架
     * @param id
     * @return
     */
    @PutMapping("/put/{id}")
    public Result put(@PathVariable("id")Long  id){
        spuService.put(id);
        return new Result(true,StatusCode.OK,"上架成功");
    }

    /*****
     * 批量上架商品
     * @param ids
     * @return
     */
    @PutMapping("/put/many")
    public Result putMany(@RequestBody Long[] ids){
        int count = spuService.putMany(ids);
        return new Result(true,StatusCode.OK,"上架"+count+"个商品");
    }
    /*****
     * 批量下架商品
     * @param ids
     * @return
     */
    @PutMapping("/pull/many")
    public Result pullMany(@RequestBody Long[] ids){
        int count = spuService.pullMany(ids);
        return new Result(true,StatusCode.OK,"下架"+count+"个商品");
    }

    /*****
     * 逻辑删除数据
     * @param id
     * @return
     */
    @DeleteMapping("/logic/delete/{id}")
    public Result logicDelete(@PathVariable(value = "id") Long id){
        spuService.logicDelete(id);
        return new Result(true,StatusCode.OK,"逻辑删除成功！");
    }
    /**
     * 恢复数据
     * @param id
     * @return
     */
    @PutMapping("/restore/{id}")
    public Result restore(@PathVariable Long id){
        spuService.restore(id);
        return new Result(true,StatusCode.OK,"数据恢复成功！");
    }

    /**
     * 恢复数据
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        spuService.delete(id);
        return new Result(true,StatusCode.OK,"数据物理删除成功！");
    }

    /***
     * Spu分页条件搜索实现
     * @param spu
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false) Spu spu, @PathVariable  int page, @PathVariable  int size){
        //执行搜索
        PageInfo<Spu> pageInfo = spuService.findPage(spu, page, size);
        return new Result<>(true,StatusCode.OK,"查询成功",pageInfo);
    }
    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable Long id){
        Spu spu =  spuService.findById(id);
        return new Result<>(true,StatusCode.OK,"查询成功",spu);
    }
}
