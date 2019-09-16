package com.changgou.goods.service;

import com.changgou.goods.pojo.Album;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface AlbumService {
    /***
     * 多条件分页查询
     * @param album
     * @param page
     * @param size
     * @return
     */
    PageInfo<Album> findPage(Album album, int page, int size);

    /****
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Album> findPage(int page, int size);

    /***
     * 多条件的搜索
     * @param album
     * @return
     */
    List<Album> findList(Album album);


    /*****
     * 根据id 删除 Album
     * @param id
     */
    void delete(Long id);

    /****
     * 修改Album数据
     * @param album
     */
    void update(Album album);

    /*****
     * 新增Album数据
     * @param album
     */
    void  add(Album album);

    /****
     * 根据id 查询 Album数据
     * @param id
     * @return
     */
    Album findById(Long id);

    /****
     * 查询所有的 Album
     * @return
     */
    List<Album> findAll();

}
