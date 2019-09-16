package com.changgou.goods.dao;

import com.changgou.goods.pojo.Category;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

@Component
public interface CategoryMapper extends Mapper<Category> {
}
