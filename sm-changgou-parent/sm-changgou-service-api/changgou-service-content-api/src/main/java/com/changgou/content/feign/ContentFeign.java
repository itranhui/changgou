package com.changgou.content.feign;

import com.changgou.content.pojo.Content;

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
@FeignClient(name="content")
@RequestMapping("/content")
public interface ContentFeign {

    /*********
     * 根据 分类id 查询所有的广告集合
     * @param id
     * @return
     */
    @GetMapping("/list/category/{id}")
    Result<List<Content>> findByCategory(@PathVariable(value = "id") Integer id);

    /***
     * Content分页条件搜索实现
     * @param content
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) Content content, @PathVariable int page, @PathVariable int size);

    /***
     * Content分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable int page, @PathVariable int size);

    /***
     * 多条件搜索品牌数据
     * @param content
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<Content>> findList(@RequestBody(required = false) Content content);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable Long id);

    /***
     * 修改Content数据
     * @param content
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody Content content, @PathVariable Long id);

    /***
     * 新增Content数据
     * @param content
     * @return
     */
    @PostMapping
    Result add(@RequestBody Content content);

    /***
     * 根据ID查询Content数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Content> findById(@PathVariable Long id);

    /***
     * 查询Content全部数据
     * @return
     */
    @GetMapping
    Result<List<Content>> findAll();
}