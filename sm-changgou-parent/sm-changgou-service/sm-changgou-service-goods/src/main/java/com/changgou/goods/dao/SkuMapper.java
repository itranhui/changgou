package com.changgou.goods.dao;

import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;
@Component
public interface SkuMapper extends Mapper<Sku> {

    /**
     * 库存递减
     * @param skuId
     * @param num
     * @return
     */
    @Update("update tb_sku set num = num - #{num} where id = #{skuId} and num>=#{num}")
    int decrCount(@Param("skuId") Long skuId,@Param("num") Integer num);

    /***
     * 库存回滚
     * @param skuId
     * @param num
     * @return
     */
    @Update("update tb_sku SET num = num + #{num}  WHERE id  = #{skuId}")
    int addCount(@Param("skuId") Long skuId, @Param("num") Integer num);
}
