package com.changgou.goods.service.impl;

import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author：Mr.ran &Date：2019/8/25 15:44
 * <p>
 * @Description：
 */
@Service
public class BrandServiceImpl implements BrandService {


    //注入BrandMapper
    @Autowired
    private BrandMapper brandMapper;



    /***
     * 新增品牌
     * @param brand
     */
    @Override
    public void add(Brand brand) {
      //  brandMapper.insert(brand); //如果不填值的话为 null
        //insertSelective(T t) 说明只要新增的数据中有空值都会被忽略 不填值的话为 空 不是null
       brandMapper.insertSelective(brand);
    }


    /****
     * 根据ID查询品牌
     * @param id
     * @return
     */
    @Override
    public Brand findBrandById(Integer id) {
        //Primary 主要的意思
        return brandMapper.selectByPrimaryKey(id);
    }


    /****
     * 查询所有的商品
     * @return
     */
    @Override
    public List<Brand> findAll() {
        //使用
        return brandMapper.selectAll();
    }

    /***
     * 修改品牌数据
     * @param brand
     */
    @Override
    public void update(Brand brand) {
        //更新数据
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    /****
     *  删除品牌
     * @param id
     */
    @Override
    public void deleteById(Integer id) {
        //  brandMapper.delete(brand);
       brandMapper.deleteByPrimaryKey(id);// 根据主键 删除
    }
    /***
     * 多条件搜索品牌方法
     * @param brand
     * @return
     */
    @Override
    public List<Brand> findList(Brand brand) {
        Example example = createExample(brand);
        //调用方法 brandMapper.selectByExample()
        List<Brand> brands = brandMapper.selectByExample(example);
        return brands;
    }


    /***
     * 品牌分页查询
     * @param page 当前页
     * @param size 每页要显示的数据条数
     * @return
     */
    @Override
    public PageInfo<Brand> findPage(int page, int size) {
        //拼接查询条件
        PageHelper.startPage(page,size);
        //分页查询
        List<Brand> brands = brandMapper.selectAll();
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        return pageInfo;
    }

    /***
     * 品牌列表条件+分页查询
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Brand> findPage(Brand brand, int page, int size) {
        //构建分页查询条件
        PageHelper.startPage(page,size);
        //构建 条件 查询对象(分页也是一种条件，也会传入进行查询)
        Example example = createExample(brand);
        //查询数据
        List<Brand> brands = brandMapper.selectByExample(example);

        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //返回pageInfo
        return pageInfo;

    }


    /**
     * 构建查询对象 Example
     * @param brand
     * @return
     */
    public Example createExample(Brand brand){
        //动态的构建条件：Example(例子)，criteria(criteria)：动态组装条件
        Example example=new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        //组装 条件
        if(brand!=null){
            //1)输入name-根据name查询[模糊查询]   select * from tb_brand where name like '%brand.getName%'
            if(!StringUtils.isEmpty(brand.getName())){
                criteria.andLike("name","%"+brand.getName()+"%");
            }
            // 品牌图片地址
            if(!StringUtils.isEmpty(brand.getImage())){
                criteria.andLike("image","%"+brand.getImage()+"%");
            }
            // 品牌的首字母
            if(!StringUtils.isEmpty(brand.getLetter())){
                criteria.andLike("letter","%"+brand.getLetter()+"%");
            }
            // 品牌id
            if(!StringUtils.isEmpty(brand.getLetter())){
                criteria.andEqualTo("id",brand.getId());
            }
            // 排序
            if(!StringUtils.isEmpty(brand.getSeq())){
                criteria.andEqualTo("seq",brand.getSeq());
            }
        }
        return example;
    }


    /*****
     * 根据分类信息查询 对应的品牌信息
     * @param categoryId
     * @return
     */
    @Override
    public List<Brand> findBrandByCategory(Integer categoryId) {
        List<Brand> brands = brandMapper.findByCategory(categoryId);
        return brands;
    }
}
