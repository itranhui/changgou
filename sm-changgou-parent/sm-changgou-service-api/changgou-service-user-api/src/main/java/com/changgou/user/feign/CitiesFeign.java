package com.changgou.user.feign;

import com.changgou.entity.Result;
import com.changgou.user.pojo.Cities;
import com.github.pagehelper.PageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:ranhui
 * @Description:
 * @Date
 *****/
@FeignClient(name="user")
@RequestMapping("/cities")
public interface CitiesFeign {

    /***
     * Cities分页条件搜索实现
     * @param cities
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) Cities cities, @PathVariable int page, @PathVariable int size);

    /***
     * Cities分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable int page, @PathVariable int size);

    /***
     * 多条件搜索品牌数据
     * @param cities
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<Cities>> findList(@RequestBody(required = false) Cities cities);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable String id);

    /***
     * 修改Cities数据
     * @param cities
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody Cities cities, @PathVariable String id);

    /***
     * 新增Cities数据
     * @param cities
     * @return
     */
    @PostMapping
    Result add(@RequestBody Cities cities);

    /***
     * 根据ID查询Cities数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Cities> findById(@PathVariable String id);

    /***
     * 查询Cities全部数据
     * @return
     */
    @GetMapping
    Result<List<Cities>> findAll();
}