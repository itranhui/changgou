package com.changgou.goods.service.impl;

import com.changgou.goods.dao.AlbumMapper;
import com.changgou.goods.pojo.Album;
import com.changgou.goods.service.AlbumService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author：Mr.ran &Date：2019/8/25 20:45
 * <p>
 * @Description：
 */
@Service
public class AlbumServiceImpl implements AlbumService {



    //注入dao
    @Autowired
    private AlbumMapper albumMapper;


    /***
     * 多条件分页查询
     * @param album
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Album> findPage(Album album, int page, int size) {
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(album);
        //执行搜索
        return new PageInfo<Album>(albumMapper.selectByExample(example));

    }

    /****
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Album> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        List<Album> albums = albumMapper.selectAll();
        PageInfo<Album> pageInfo = new PageInfo<>(albums);
        return pageInfo;
    }

    /***
     * 多条件的搜索
     * @param album
     * @return
     */
    @Override
    public List<Album> findList(Album album) {
        //构建搜索条件
        Example example = createExample(album);
        //传入查询条件对象
        List<Album> albums = albumMapper.selectByExample(example);
        return albums;
    }

    /*****
     * 根据id 删除 Album
     * @param id
     */
    @Override
    public void delete(Long id) {
        // PrimaryKey 主键
    albumMapper.deleteByPrimaryKey(id);
    }
    /****
     * 修改Album数据
     * @param album
     */
    @Override
    public void update(Album album) {
        //根据主键修改数据
        albumMapper.updateByPrimaryKey(album);
        //根据主键修改数据  忽略空值
        //albumMapper.updateByPrimaryKeySelective(album);
    }

    /*****
     * 新增Album数据
     * @param album
     */
    @Override
    public void add(Album album) {
        //保存 忽略空值 (Selective)
        albumMapper.insertSelective(album);
    }

    /****
     * 根据id 查询 Album数据
     * @param id
     * @return
     */
    @Override
    public Album findById(Long id) {
        //根据主键查询数据
        return  albumMapper.selectByPrimaryKey(id);
    }

    /****
     * 查询所有的 Album
     * @return
     */
    @Override
    public List<Album> findAll() {

        return albumMapper.selectAll();
    }

    /***
     * 构建查询对象
     *
     */


    /**
     * Album构建查询对象
     * @param album
     * @return
     */
    public Example createExample(Album album){
        Example example=new Example(Album.class);
        Example.Criteria criteria = example.createCriteria();
        if(album!=null){
            // 编号
            if(!StringUtils.isEmpty(String.valueOf(album.getId()))){
                criteria.andEqualTo("id",album.getId());
            }
            // 相册名称
            if(!StringUtils.isEmpty(album.getTitle())){
                criteria.andLike("title","%"+album.getTitle()+"%");
            }
            // 相册封面
            if(!StringUtils.isEmpty(album.getImage())){
                criteria.andEqualTo("image",album.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(album.getImageItems())){
                criteria.andEqualTo("imageItems",album.getImageItems());
            }
        }
        return example;
    }

}
