package com.changgou.goods.feign;

import com.changgou.goods.pojo.Para;
import com.github.pagehelper.PageInfo;
import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:ranhui
 * @Description:
 * @Date
 *****/
@FeignClient(name="goods")
@RequestMapping("/para")
public interface ParaFeign {

    /***
     * Para分页条件搜索实现
     * @param para
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) Para para, @PathVariable int page, @PathVariable int size);

    /***
     * Para分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable int page, @PathVariable int size);

    /***
     * 多条件搜索品牌数据
     * @param para
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<Para>> findList(@RequestBody(required = false) Para para);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable Integer id);

    /***
     * 修改Para数据
     * @param para
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody Para para, @PathVariable Integer id);

    /***
     * 新增Para数据
     * @param para
     * @return
     */
    @PostMapping
    Result add(@RequestBody Para para);

    /***
     * 根据ID查询Para数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Para> findById(@PathVariable Integer id);

    /***
     * 查询Para全部数据
     * @return
     */
    @GetMapping
    Result<List<Para>> findAll();
}