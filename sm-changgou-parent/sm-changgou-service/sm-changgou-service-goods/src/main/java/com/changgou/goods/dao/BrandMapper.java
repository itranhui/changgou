package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper  extends Mapper<Brand> {
    /*****
     * 根据分类信息查询 对应的品牌信息
     * @param categoryId
     * @return
     */
    @Select("SELECT tb.* FROM tb_category_brand tcb,tb_brand tb WHERE tcb.category_id=#{categoryId} AND tb.id=tcb.brand_id")
    List<Brand> findByCategory( Integer categoryId);
}
