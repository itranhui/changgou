package com.changgou.goods.service.impl;

import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SpecMapper;
import com.changgou.goods.dao.TemplateMapper;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Spec;
import com.changgou.goods.pojo.Template;
import com.changgou.goods.service.SpecService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author：Mr.ran &Date：2019/8/26 10:41
 * <p>
 * @Description：
 */

@Service
@Transactional
public class SpecServiceImpl implements SpecService {

    @Autowired
    private SpecMapper specMapper;

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * Spec条件+分页查询
     *
     * @param spec 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spec> findPage(Spec spec, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(spec);
        //执行搜索
        return new PageInfo<Spec>(specMapper.selectByExample(example));
    }

    /**
     * Spec分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spec> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Spec>(specMapper.selectAll());
    }

    /**
     * Spec条件查询
     *
     * @param spec
     * @return
     */
    @Override
    public List<Spec> findList(Spec spec) {
        //构建查询条件
        Example example = createExample(spec);
        //根据构建的条件查询数据
        return specMapper.selectByExample(example);
    }


    /**
     * Spec构建查询对象
     *
     * @param spec
     * @return
     */
    public Example createExample(Spec spec) {
        Example example = new Example(Spec.class);
        Example.Criteria criteria = example.createCriteria();
        if (spec != null) {
            // ID
            if (!StringUtils.isEmpty(spec.getId())) {
                criteria.andEqualTo("id", spec.getId());
            }
            // 名称
            if (!StringUtils.isEmpty(spec.getName())) {
                criteria.andLike("name", "%" + spec.getName() + "%");
            }
            // 规格选项
            if (!StringUtils.isEmpty(spec.getOptions())) {
                criteria.andEqualTo("options", spec.getOptions());
            }
            // 排序
            if (!StringUtils.isEmpty(spec.getSeq())) {
                criteria.andEqualTo("seq", spec.getSeq());
            }
            // 模板ID
            if (!StringUtils.isEmpty(spec.getTemplateId())) {
                criteria.andEqualTo("templateId", spec.getTemplateId());
            }
        }
        return example;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Integer id) {
        //查询规格数据
        Spec spec = specMapper.selectByPrimaryKey(id);
        //如果templateId 为null的话 那就直接删除
        if (spec.getTemplateId() != null) {
            //变更模板数量
            updateSpecNum(spec, -1);
        }
        //删除指定规格
        specMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spec
     *
     * @param spec
     */
    //@Transactional(propagation = Propagation.REQUIRED)//事务注解
    @Override
    public void update(Spec spec) {
        //修改之前的template
        Integer id = spec.getId();
        Spec afterSpec = specMapper.selectByPrimaryKey(id);
        if (spec.getTemplateId() == null) {
            //原有对应的template 数据-1
            updateSpecNum(afterSpec, -1);
            specMapper.updateByPrimaryKey(spec);
        }
        //如果原有的数据是null的话
        else if (afterSpec.getTemplateId() == null) {
            //更新数据
            specMapper.updateByPrimaryKey(spec);
            //新template +1
            updateSpecNum(spec, 1);
        }
        else {
            //原来有对应的template 数据-1
            updateSpecNum(afterSpec, -1);
            specMapper.updateByPrimaryKey(spec);
            //新template +1
            updateSpecNum(spec, 1);
        }
        //都不满足 直接执行
        specMapper.updateByPrimaryKey(spec);
    }

    /**
     * 增加Spec
     *
     * @param spec
     */
    @Override
    public void add(Spec spec) {

        specMapper.insert(spec);
        if (spec.getTemplateId() != null)
            //变更模板数量
            updateSpecNum(spec, 1);
    }

    /**
     * 根据ID查询Spec
     *
     * @param id
     * @return
     */
    @Override
    public Spec findById(Integer id) {
        return specMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spec全部数据
     *
     * @return
     */
    @Override
    public List<Spec> findAll() {
        return specMapper.selectAll();
    }


    /**
     * 修改模板统计数据
     *
     * @param spec:操作的模板
     * @param count:变更的数量
     */
    public void updateSpecNum(Spec spec, int count) {
        //修改模板数量统计
        Template template = templateMapper.selectByPrimaryKey(spec.getTemplateId());
        //如果为null 就把当前数据设置给SpecNum
        if (template.getSpecNum() == null) {

            template.setSpecNum(count);
        } else {
            template.setSpecNum(template.getSpecNum() + count);
        }
        //更新tb_template表
        templateMapper.updateByPrimaryKeySelective(template);
    }


    /****
     * 根据分类id  查询出对应的规格 列表
     * @param categoryid
     * @return
     */
    @Override
    public List<Spec> findByCategoryId(Integer categoryid) {
        //先查询出分类消息
        Category category = categoryMapper.selectByPrimaryKey(categoryid);

        //取出模板id 设置到Spec 中
        Integer templateId = category.getTemplateId();
        Spec spec = new Spec();
        spec.setTemplateId(templateId);

        return specMapper.select(spec);
    }
}
