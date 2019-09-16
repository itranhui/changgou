package com.changgou.search.service;

import java.util.Map;

public interface SkuEsService {

    /***
     * 导入SKU数据
     */
    public void importData() ;

        /***
         * 搜索
         * @param searchMap
         * @return
         */
    Map<String, Object>  search(Map<String, String> searchMap);
}
