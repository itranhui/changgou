package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;

/**
 * @Author：Mr.ran &Date：2019/8/28 15:54
 * <p>
 * @Description：
 */

public interface SpuService {

    /***
     * 添加Goods
     * @param goods
     * @return
     */
    void saveGoods(Goods goods);
    /***
     * 根据ID查询Goods
     * @param id
     * @return
     */
    Goods findGoodsById(Long id);
    /****
     * 根据spuid 审核改该商品
     * @param id
     * @return
     */
    void audit(Long id);


    /***
     * 商品下架
     * @param spuId
     */
    void pull(Long spuId);

    /******
     * 商品上架
     * @param id
     * @return
     */
    void put(Long id);

    /*****
     * 批量上架商品
     * @param ids
     * @return
     */
    int putMany(Long[] ids);

    /****
     * 下架商品
     * @param ids
     * @return
     */
    int pullMany(Long[] ids);

    /***
     * 逻辑删除
     * @param spuId
     */
    void logicDelete(Long spuId);

    /***
     * 还原被删除商品
     * @param spuId
     */
    void restore(Long spuId);

    /*****
     * 物理删除
     * @param id
     */
     void delete(Long id);
    /***
     * Spu分页条件搜索实现
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /****
     * 根据id 查询spu
     * @param id
     * @return
     */
    Spu findById(Long id);
}
