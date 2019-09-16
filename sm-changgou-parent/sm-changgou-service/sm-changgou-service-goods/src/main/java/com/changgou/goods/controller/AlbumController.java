package com.changgou.goods.controller;


import com.changgou.goods.pojo.Album;
import com.changgou.goods.service.AlbumService;
import com.github.pagehelper.PageInfo;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author：Mr.ran &Date：2019/8/25 21:29
 * <p>
 * @Description：
 */
@RestController
@RequestMapping("/album")
public class AlbumController {

    //注入AlbumService
    @Autowired
    public AlbumService albumService;

    /***
     * Album分页条件搜索实现
     * @param album
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo<Album>> findPage(@PathVariable(value = "page") Integer page, @PathVariable(value = "size") Integer size, @RequestBody(required = false) Album album) {
        PageInfo<Album> pageInfo = albumService.findPage(album, page, size);
        return new Result<>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /***
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result<Album> findAll() {
        return new Result<>(true, StatusCode.OK, "查询成功", albumService.findAll());
    }


    /***
     * 根据ID查询相册数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Album> findById(@PathVariable(value = "id") Integer id) {
        return new Result<>(true, StatusCode.OK, "查询成功", albumService.findById(Long.valueOf(id)));
    }



    /***
     * 新增相册数据
     * @param album
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Album album) {
        albumService.add(album);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    /***
     * 修改相册数据
     * @param album
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Album album, @PathVariable(value = "id") Long id){
        //设置ID
        album.setId(id);
        //修改数据
        albumService.update(album);
        return new Result(true, StatusCode.OK,"修改成功");
    }
    /***
     * 根据ID删除相册数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Long id){
        albumService.delete(id);
        return new Result(true, StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索相册
     *
     */
    @PostMapping(value = "/search")
    public Result<List<Album>> findList(@RequestBody(required = false) Album album) {
        List<Album> list = albumService.findList(album);
        return new Result<>(true, StatusCode.OK,"查询成功",list);

    }



    /****
     * 分页搜索实现
     * @param page  当前页
     * @param size 每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<Album> findPage(@PathVariable int page, @PathVariable int size){
        PageInfo<Album> pageInfo = albumService.findPage(page, size);
        return new Result<Album>(true, StatusCode.OK,"查询成功",pageInfo);
    }


}



