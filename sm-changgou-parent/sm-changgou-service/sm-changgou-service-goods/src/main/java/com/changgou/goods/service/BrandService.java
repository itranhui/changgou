package com.changgou.goods.service;

import com.changgou.goods.pojo.Brand;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * 商品服务接口
 */
public interface BrandService  {
    /***
     * 查询所有的品牌
     * @return
     */
    List<Brand> findAll();

    /****
     * 根据ID查询品牌
     * @param id
     * @return
     */
    Brand findBrandById(Integer id);

    /***
     * 新增品牌
     * @param brand
     */
    void add(Brand brand);
    /***
     * 修改品牌数据
     * @param brand
     */
    void update(Brand brand);

    /****
     *  删除品牌
     * @param id
     */
    void deleteById(Integer id);

    /***
     * 多条件搜索品牌方法
     * @param brand
     * @return
     */
    List<Brand> findList(Brand brand);

    /***
     * 品牌分页查询
     * @param page 当前页
     * @param size 每页要显示的数据条数
     * @return
     */
    PageInfo<Brand> findPage(int page, int size);
    /***
     * 品牌列表条件+分页查询
     * @param brand
     * @param page
     * @param size
     * @return
     */
    PageInfo<Brand> findPage(Brand brand, int page, int size);


    /*****
     * 根据分类信息查询 对应的品牌信息
     * @param categoryId
     * @return
     */
    List<Brand> findBrandByCategory(Integer categoryId);

}
