package com.changgou.content.dao;
import com.changgou.content.pojo.Content;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:ranhui
 * @Description:Content的Dao
 * @Date
 *****/
@Component
public interface ContentMapper extends Mapper<Content> {
}
