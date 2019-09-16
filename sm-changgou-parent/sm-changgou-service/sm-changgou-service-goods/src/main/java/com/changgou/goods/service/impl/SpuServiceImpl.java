package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.changgou.entity.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/8/28 15:54
 * <p>
 * @Description：
 */
@Service
@Transactional
public class SpuServiceImpl implements SpuService {


    //注入Idworker
    @Autowired
    private IdWorker idWorker;

    //注入SpuMapper
    @Autowired
    private SpuMapper spuMapper;

    //注入SkuMapper
    @Autowired
    private SkuMapper skuMapper;

    // 注入BrandMapper
    @Autowired
    private BrandMapper brandMapper;

    //注入CategoryMapper
    @Autowired
    private CategoryMapper categoryMapper;

    /***
     * 添加Goods / 更新
     * @param goods
     * @return
     */
    @Override
    public void saveGoods(Goods goods) {
        //取出Spu
        Spu spu = goods.getSpu();
        if (goods.getSpu().getId() == null) {
            //添加
            //设置ID
            spu.setId(idWorker.nextId());
            //保存spu
            spuMapper.insertSelective(spu);
        } else {
            //更新删除之前 的sku数据
            Long spuId = spu.getId();
            Sku sku = new Sku();
            sku.setSpuId(spuId);
            skuMapper.delete(sku);
        }
        //获取当前时间(是修改日期也是创建日期)
        Date nowTime = new Date();
        //获取商品的名称
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        String brandName = brand.getName();
        //分类名称
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        String categoryName = category.getName();
        //分类的三级ID
        Integer category3Id = spu.getCategory3Id();
        //SpuId
        Long spuId = spu.getId();
        //保存sku
        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {
            //skuId
            long skuId = idWorker.nextId();
            sku.setId(skuId);
            sku.setSpuId(spuId);
            sku.setBrandName(brandName);
            sku.setCategoryName(categoryName);
            sku.setCategoryId(category3Id);
            sku.setUpdateTime(nowTime);
            sku.setCreateTime(nowTime);
            //获取sku的名字
            String spec = sku.getSpec();
            if (StringUtils.isEmpty(spec)) {
                sku.setSpec("{}");
            }
            //获取sku的name
            String name = sku.getName();
            //将JSON格式转换成Map
            Map<String, String> map = JSON.parseObject(spec, Map.class);
            //遍历集合循环组装sku 的name
            for (Map.Entry<String, String> entry : map.entrySet()) {
                name += " " + entry.getValue();
            }
            sku.setName(name);
            //增加
            skuMapper.insertSelective(sku);
        }
    }

    /***
     * 根据ID查询Goods
     * @param id
     * @return
     */
    @Override
    public Goods findGoodsById(Long id) {
        Goods goods = new Goods();
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        goods.setSpu(spu);
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        goods.setSkuList(skus);
        return goods;
    }



    /****
     * 根据spuid 审核改该商品
     * @param id
     * @return
     */
    @Override
    public void audit(Long id) {
        //查询出spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断商品是否被删除
        if (spu.getIsDelete().equals("1")){
            throw new RuntimeException("此商品已经删除");
        }
        //实现上架和审核
        spu.setStatus("1"); //审核通过
        spu.setIsMarketable("1"); //上架
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /***
     * 商品下架
     * @param spuId
     */
    @Override
    public void pull(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsDelete().equals("1")){
            throw  new RuntimeException("此商品已经被删除");
        }
        //设置下架状态
        spu.setIsMarketable("0");
        //更新数据
        spuMapper.updateByPrimaryKeySelective(spu);
    }



    /******
     * 商品上架
     * @param id
     * @return
     */
    @Override
    public void put(Long id) {

        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu.getIsDelete().equals("1")){
            throw  new RuntimeException("删除的商品不能上架");
        }
        if (spu.getIsMarketable().equals("1")){
            throw new RuntimeException("不能上架同一款商品");
        }
        if (spu.getStatus().equals("0")){
            throw new RuntimeException("未通过审核的商品不能上架");
        }
        //改变上架的状态
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
        }


    /*****
     * 批量上架商品
     * @param ids
     * @return
     */
    @Override
    public int putMany(Long[] ids) {
        //构建条件对象
        Example example = new Example(Spu.class);
        //criteria  标准
        Example.Criteria criteria = example.createCriteria();
        //要上架的id
        criteria.andIn("id", Arrays.asList(ids));
        //对没有上架的商品进行上架
        criteria.andEqualTo("isMarketable","0");
        //对通过审核的商品进行上架
        criteria.andEqualTo("status","1");
        //对没有被删除的上=商品进行上架
        criteria.andEqualTo("isDelete","0");
        //设置上架状态为 1
        Spu spu = new Spu();
        spu.setIsMarketable("1");
        return spuMapper.updateByExampleSelective(spu,example);

    }

    /****
     * 批量下架商品
     * @param ids
     * @return
     */
    @Override
    public int pullMany(Long[] ids) {
       Spu spu = new Spu();
       spu.setIsMarketable("0");
       //反射的三种方式
       Example example = new Example(spu.getClass());

        Example.Criteria criteria = example.createCriteria();

        criteria.andIn("id", Arrays.asList(ids));
        //对上架的商品下架(既然是上架的商品的话，那么一定是审核通过的，一定是没有删除杜)
        criteria.andEqualTo("isMarketable","1");

        //执行更新 数据操作
        return  spuMapper.updateByExample(spu,example);
    }

    /***
     * 逻辑删除
     * @param spuId
     */
    @Override
    public void logicDelete(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //必须是下架的商品才能被删除
        if (spu.getIsMarketable().equals("1")) {
            throw new RuntimeException("必须是下架的商品才能被删除!");
        }
        //逻辑删除，设置状态为 未审核
        spu.setIsDelete("1");
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);

    }



    /**
     * 恢复数据
     * @param spuId
     */
    @Override
    public void restore(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //检查该 商品是否被删除
        if (spu.getIsDelete().equals("0")) {
            throw new RuntimeException("此商品没有被删除");
        }
        //恢复数据，设置状态
        spu.setStatus("0");
        spu.setIsDelete("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //检查是否被逻辑删除  ,必须先逻辑删除后才能物理删除
        if(!spu.getIsDelete().equals("1")){
            throw new RuntimeException("此商品不能删除！");
        }
        if (spu.getIsMarketable().equals("1")){
            throw new RuntimeException("上架的商品不能逻辑删除，先下架");
        }
        spuMapper.deleteByPrimaryKey(id);
        //删除 Spu 那么对应的sku 的商品信息 也要删除
        Sku sku = new Sku();
        sku.setSpuId(id);
        skuMapper.delete(sku);
    }
    /***
     * Spu分页条件搜索实现
     * @param spu
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size) {
        //构建查询条件
        PageHelper.startPage(page, size);
        Example example = createExample(spu);
        List<Spu> spus = spuMapper.selectByExample(example);
        //封装数据
        PageInfo<Spu> pageInfo = new PageInfo<Spu>(spus);
        return pageInfo;
    }

    /****
     * 根据id 查询spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id) {

        return spuMapper.selectByPrimaryKey(id);
    }

    /****
     * 构建条件查询
     * @param spu
     * @return
     */
    public Example createExample(Spu spu) {
        Example example = new Example(Spu.class);
        //获取构建条件对象
        Example.Criteria criteria = example.createCriteria();

        if (spu != null) {
            //id
            if (!StringUtils.isEmpty(String.valueOf(spu.getId()))) {
                criteria.andEqualTo("id", spu.getId());
            }
            //货号
            if (!StringUtils.isEmpty(spu.getSn())) {
                criteria.andEqualTo("sn", spu.getSn());
            }
            //SPU名
            if (!StringUtils.isEmpty(spu.getName())) {
                criteria.andLike("name", "%" + spu.getName() + "%");
            }
            //副标题
            if (!StringUtils.isEmpty(spu.getCaption())) {
                criteria.andLike("caption", "%" + spu.getCaption() + "%");
            }
            //品牌ID
            if (!StringUtils.isEmpty(String.valueOf(spu.getBrandId()))) {
                criteria.andEqualTo("brandId", spu.getBrandId());
            }
            //一级分类
            if (!StringUtils.isEmpty(String.valueOf(spu.getCategory1Id()))) {
                criteria.andEqualTo("category1Id", spu.getCategory1Id());
            }
            //二级分类
            if (!StringUtils.isEmpty(String.valueOf(spu.getCategory2Id()))) {
                criteria.andEqualTo("category2Id", spu.getCategory2Id());
            }
            //三级分类
            if (!StringUtils.isEmpty(String.valueOf(spu.getCategory3Id()))) {
                criteria.andEqualTo("category3Id", spu.getCategory3Id());
            }
            //模板ID
            if (!StringUtils.isEmpty(String.valueOf(spu.getTemplateId()))) {
                criteria.andEqualTo("templateId", spu.getTemplateId());
            }
            //运费模板
            if (!StringUtils.isEmpty(String.valueOf(spu.getFreightId()))) {
                criteria.andEqualTo("freightId", spu.getFreightId());
            }
            //图片
            if (!StringUtils.isEmpty(spu.getImage())) {
                criteria.andEqualTo("image", spu.getImage());
            }
            //售后服务
            if (!StringUtils.isEmpty(spu.getSaleService())) {
                criteria.andLike("saleService", "%" + spu.getSaleService() + "%");
            }
            //介绍
            if (!StringUtils.isEmpty(spu.getIntroduction())) {
                criteria.andLike("introduction", "%" + spu.getIntroduction() + "%");
            }
            //规格列表
            if (!StringUtils.isEmpty(spu.getSpecItems())) {
                criteria.andLike("specItems", "%" + spu.getSpecItems() + "%");
            }
            //参数列表
            if (!StringUtils.isEmpty(spu.getParaItems())) {
                criteria.andLike("paraItems", "%" + spu.getParaItems() + "%");
            }
            //销量
            if (!StringUtils.isEmpty(String.valueOf(spu.getSaleNum()))) {
                //没有说明 销量的范围 默认从0开始
                criteria.andBetween("saleNum", 0, spu.getSaleNum());
            }
            //评论数
            if (!StringUtils.isEmpty(String.valueOf(spu.getCommentNum()))) {
                //没有说明评论数的范围 默认从0开始
                criteria.andBetween("saleNum", 0, spu.getCommentNum());
            }
            //上架
            if (!StringUtils.isEmpty(spu.getIsMarketable())) {
                criteria.andEqualTo("isMarketable", spu.getIsMarketable());
            }
        }
        return example;
    }
}
