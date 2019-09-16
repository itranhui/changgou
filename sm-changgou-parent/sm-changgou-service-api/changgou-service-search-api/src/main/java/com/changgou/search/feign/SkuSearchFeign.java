package com.changgou.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/3 15:22
 * <p>
 * @Description：
 */
@FeignClient("search")
@RequestMapping(value = "/search")

public interface SkuSearchFeign {
    /*******
     * 关键字搜索
     * @return
     */
    @GetMapping
     Map<String, Object> search(@RequestParam(required = false) Map<String, String> searchMap) ;
}
