package com.changgou.goods.service;

import com.changgou.goods.pojo.Sku;

import java.util.List;
import java.util.Map;

public interface SkuService {
    /*********
     * 查询全部sku
     * @return
     */
    List<Sku> importData();
    /***
     * 多条件搜索品牌数据
     * @param sku
     * @return
     */
    List<Sku> findList(Sku sku);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    Sku findById(Long id);

    /**
     * 库存递减
     */
    void decrCount(Map<String, Object> decrmap);

    /***
     * 库存回滚
     * @param skuIdsAndNumMap
     */
    void addCount(Map<String, Object> skuIdsAndNumMap);

    /****
     * 新增我的收藏
     *
     * @param map
     */
    void collect(Map<String, Object> map);
}
