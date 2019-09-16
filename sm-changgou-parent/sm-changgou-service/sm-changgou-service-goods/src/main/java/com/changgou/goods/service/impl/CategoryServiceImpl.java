package com.changgou.goods.service.impl;

import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.service.CategoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author：Mr.ran &Date：2019/8/25 22:50
 * <p>
 * @Description：
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * Category条件+分页查询
     *
     * @param category 查询条件
     * @param page     页码
     * @param size     页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Category> findPage(Category category, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(category);
        List<Category> categories = categoryMapper.selectByExample(example);
        PageInfo<Category> pageInfo = new PageInfo<>(categories);
        return pageInfo;
    }
    /**
     * Category分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Category> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        List<Category> categories = categoryMapper.selectAll();
        PageInfo<Category> pageInfo = new PageInfo<>(categories);
        return pageInfo;
    }
    /**
     * Category条件查询
     * @param category
     * @return
     */
    @Override
    public List<Category> findList(Category category) {
        //构建查询对象
        Example example = createExample(category);

        //查询数据
        List<Category> categories = categoryMapper.selectByExample(example);
        return categories;
    }
    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Integer id) {
        //根据id 删除
        categoryMapper.deleteByPrimaryKey(id);
    }
    /**
     * 修改Category
     * @param category
     */
    @Override
    public void update(Category category) {
        //忽略空值
        categoryMapper.updateByPrimaryKeySelective(category);
    }
    /**
     * 增加Category
     * @param category
     */
    @Override
    public void add(Category category) {
        //忽略空值
        categoryMapper.insertSelective(category);
    }
    /**
     * 根据ID查询Category
     * @param id
     * @return
     */
    @Override
    public Category findById(Integer id) {
        //根据主键查询数据
        categoryMapper.selectByPrimaryKey(id);
        return null;
    }

    /**
     * 查询Category全部数据
     * @return
     */
    @Override
    public List<Category> findAll() {
        return categoryMapper.selectAll();
    }

    /****
     * 根据父节点 查询数据
     * @param pid
     * @return
     */
    @Override
    public List<Category> findByParentId(Integer pid) {
        Category category = new Category();
        category.setParentId(pid);
            //查询数据
        List<Category> categories = categoryMapper.select(category);
        return categories;
    }

    /**
     * Category构建查询对象
     *
     * @param category
     * @return
     */
    public Example createExample(Category category) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        if (category != null) {
            // 分类ID
            if (!StringUtils.isEmpty(category.getId())) {
                criteria.andEqualTo("id", category.getId());
            }
            // 分类名称
            if (!StringUtils.isEmpty(category.getName())) {
                criteria.andLike("name", "%" + category.getName() + "%");
            }
            // 商品数量
            if (!StringUtils.isEmpty(category.getGoodsNum())) {
                criteria.andEqualTo("goodsNum", category.getGoodsNum());
            }
            // 是否显示
            if (!StringUtils.isEmpty(category.getIsShow())) {
                criteria.andEqualTo("isShow", category.getIsShow());
            }
            // 是否导航
            if (!StringUtils.isEmpty(category.getIsMenu())) {
                criteria.andEqualTo("isMenu", category.getIsMenu());
            }
            // 排序
            if (!StringUtils.isEmpty(category.getSeq())) {
                criteria.andEqualTo("seq", category.getSeq());
            }
            // 上级ID
            if (!StringUtils.isEmpty(category.getParentId())) {
                criteria.andEqualTo("parentId", category.getParentId());
            }
            // 模板ID
            if (!StringUtils.isEmpty(category.getTemplateId())) {
                criteria.andEqualTo("templateId", category.getTemplateId());
            }
        }
        return example;
    }
}
