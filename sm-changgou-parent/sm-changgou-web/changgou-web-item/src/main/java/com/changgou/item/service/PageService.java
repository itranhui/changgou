package com.changgou.item.service;

/**
 * @Author：Mr.ran &Date：2019/9/3 20:39
 * <p>
 * @Description：
 */

public interface  PageService {
    /**
     * 根据商品的ID 生成静态页
     * @param spuId
     */
     void createPageHtml(Long spuId) ;
}