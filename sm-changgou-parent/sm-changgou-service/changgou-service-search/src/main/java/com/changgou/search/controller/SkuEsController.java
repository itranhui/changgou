package com.changgou.search.controller;

import java.util.Map;

 import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.SkuEsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：Mr.ran &Date：2019/8/31 15:32
 * <p>
 * @Description：
 */
@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuEsController {
    @Autowired
    private SkuEsService skuEsService;

    /**
     * 导入数据
     *
     * @return
     */
    @GetMapping("/import")
    public Result importData() {
        skuEsService.importData();
        return new Result(true, StatusCode.OK, "导入数据到索引库中成功！");
    }

    /*******
     * 关键字搜索
     * @return
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam(required = false) Map<String, String> searchMap) {
        Map<String, Object> stringObjectMap = skuEsService.search(searchMap);
        return stringObjectMap;
    }

}
