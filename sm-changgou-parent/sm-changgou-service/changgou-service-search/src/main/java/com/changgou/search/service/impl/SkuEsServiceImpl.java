package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuEsService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author：Mr.ran &Date：2019/8/31 15:31
 * <p>
 * @Description：
 */
@Service
public class SkuEsServiceImpl implements SkuEsService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /****
     * ElasticsearchTempalte  ：  可以实现索引库的增删改查[高级搜索]
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /****
     * 多条件搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        /***
         * 搜索条件封装
         */
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(searchMap);

        //集合搜索
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);

        //当用户选择了分类，将分类作为搜索条件，则不需要对分类进行分组搜索，因为分组搜索的数据是用于显示分类搜索条件的
        //分类->searchMap->category
        //if(searchMap==null || StringUtils.isEmpty(searchMap.get("category"))){
        //分类分组查询实现
        //List<String> categoryList = searchCategoryList(nativeSearchQueryBuilder);
        //resultMap.put("categoryList",categoryList);
        //}


        //当用户选择了品牌，将品牌作为搜索条件，则不需要对品牌进行分组搜索，因为分组搜索的数据是用于显示品牌搜索条件的
        //品牌->searchMap->brand
        //if(searchMap==null || StringUtils.isEmpty(searchMap.get("brand"))){
        //查询品牌集合[搜索条件]
        //List<String> brandList = searchBrandList(nativeSearchQueryBuilder);
        //resultMap.put("brandList",brandList);
        //}

        //规格查询
        //Map<String, Set<String>> specList = searchSpecList(nativeSearchQueryBuilder);
        //resultMap.put("specList",specList);

        //分组搜索实现
        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);
        resultMap.putAll(groupMap);
        return resultMap;
    }


    /****
     * 分组查询->分类分组、品牌分组、规格分组
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String,Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder,Map<String,String> searchMap) {
        /***
         * 分组查询分类集合
         * .addAggregation():添加一个聚合操作
         * 1)取别名
         * 2)表示根据哪个域进行分组查询
         */
        if(searchMap==null || StringUtils.isEmpty(searchMap.get("category"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if(searchMap==null || StringUtils.isEmpty(searchMap.get("brand"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合,可以根据多个域进行分组
         * .get("skuCategory"):获取指定域的集合数  [手机,家用电器,手机配件]
         */
        //定义一个Map，存储所有分组结果
        Map<String,Object> groupMapResult = new HashMap<String,Object>();

        if(searchMap==null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = aggregatedPage.getAggregations().get("skuCategory");
            //获取分类分组集合数据
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList",categoryList);
        }
        if(searchMap==null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = aggregatedPage.getAggregations().get("skuBrand");
            //获取品牌分组集合数据
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList",brandList);
        }
        StringTerms specTerms = aggregatedPage.getAggregations().get("skuSpec");

        //获取规格分组集合数据->实现合并操作
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> specMap = putAllSpec(specList);
        groupMapResult.put("specList",specMap);
        return groupMapResult;
    }

    /***
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    public List<String> getGroupList(StringTerms stringTerms){
        List<String> groupList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String feildName = bucket.getKeyAsString();//其中的一个分类名字
            groupList.add(feildName);
        }
        return groupList;
    }

    /***
     * 搜索条件封装
     * @param searchMap
     * @return
     */
    public NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //NativeSearchQueryBuilder:搜索条件构建对象,用于封装各种搜索条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //BoolQuery   must,must_not,should
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if(searchMap!=null && searchMap.size()>0){
            //根据关键词搜索
            String keywords = searchMap.get("keywords");
            //如果关键词不为空，则搜索关键词数据
            if(!StringUtils.isEmpty(keywords)){
                //nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            //输入了分类->category
            if(!StringUtils.isEmpty(searchMap.get("category"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",searchMap.get("category")));
            }

            //输入了品牌->brand
            if(!StringUtils.isEmpty(searchMap.get("brand"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }

            //规格过滤实现:spec_网络=联通3G&spec_颜色=红
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                //如果key以spec_开始，则表示规格筛选查询
                if(key.startsWith("spec_")){
                    //规格条件的值
                    String value = searchMap.get(key).replace("\\","");
                    //spec_网络  spec_前5个要去掉
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }

            //price  0-500元 500-1000元 1000-1500元 1500-2000元 2000-3000元 3000元以上
            String price = searchMap.get("price");
            if(!StringUtils.isEmpty(price)){
                //去掉中文元和以上  0-500 500-1000 1000-1500 1500-2000 2000-3000 3000
                price=price.replace("元","").replace("以上","");
                //prices[]根据-分割        [0,500][500,1000]............................[3000]
                String[] prices = price.split("-");
                //x一定不为空,y有可能为null
                if(prices!=null && prices.length>0){
                    //price>prices[0]
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    //prices[1]!=null   price<=prices[1]
                    if(prices.length==2){
                        // price<=prices[1]
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }

            //排序实现
            String sortField = searchMap.get("sortField");  //指定排序的域
            String sortRule = searchMap.get("sortRule");    //指定排序的规则
            if(!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)){
                nativeSearchQueryBuilder.withSort(
                        new FieldSortBuilder(sortField) //指定排序域
                                .order(SortOrder.valueOf(sortRule))); //指定排序规则
            }
        }

        //分页,用户如果不传分页参数，则默认第1页
        Integer pageNum = coverterPage(searchMap); //默认第1页
        Integer size =30;    //默认查询的数据条数
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum-1,size));

        //将boolQueryBuilder填充给nativeSearchQueryBuilder
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    /****
     * 接收前端传入的分页参数
     * @param searchMap
     * @return
     */
    public Integer coverterPage(Map<String,String> searchMap){
        if(searchMap!=null){
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {
            }
        }
        return 1;
    }


    /***
     * 结果集搜索
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //高亮配置  ***:HighlightBuilder(X)
        HighlightBuilder.Field field = new HighlightBuilder.Field("name"); //指定高亮域
        //前缀  <em style="color:red;">
        field.preTags("<em style=\"color:red;\">");
        //后缀 </em>
        field.postTags("</em>");
        //碎片长度  关键词数据的长度
        field.fragmentSize(100);
        //添加高亮
        nativeSearchQueryBuilder.withHighlightFields(field);

        /*****
         * 执行搜索,响应结果给我
         * 1)搜索条件封装对象
         * 2)搜索的结果集(集合数据)需要转换的类型
         * 3)AggregatedPage<SkuInfo>:搜索结果集的封装
         */
        //AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate
                .queryForPage(
                        nativeSearchQueryBuilder.build(),       //搜索条件封装
                        SkuInfo.class,                         //数据集合要转换的类型的字节码
                        //SearchResultMapper);                 //执行搜索后，将数据结果集封装到该对象中
                        new SearchResultMapper() {
                            @Override
                            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                                //存储所有转换后的高亮数据对象
                                List<T> list = new ArrayList<T>();

                                //执行查询，获取所有数据->结果集[非高亮数据|高亮数据]
                                for (SearchHit hit : response.getHits()) {
                                    //分析结果集数据，获取[非高亮]数据
                                    SkuInfo skuInfo =JSON.parseObject(hit.getSourceAsString(),SkuInfo.class);

                                    //分析结果集数据，获取高亮数据->只有某个域的高亮数据
                                    HighlightField highlightField = hit.getHighlightFields().get("name");

                                    if(highlightField!=null && highlightField.getFragments()!=null){
                                        //高亮数据读取出来
                                        Text[] fragments = highlightField.getFragments();
                                        //Text[]->String->String[]->
                                        StringBuffer buffer = new StringBuffer();
                                        for (Text fragment : fragments) {
                                            buffer.append(fragment.toString());
                                        }
                                        //非高亮数据中指定的域替换成高亮数据
                                        skuInfo.setName(buffer.toString());
                                    }
                                    //将高亮数据添加到集合中
                                    list.add((T) skuInfo);
                                }
                                //将数据返回
                                /***
                                 * 1)搜索的集合数据：(携带高亮)List<T> content
                                 * 2)分页对象信息：Pageable pageable
                                 * 3)搜索记录的总条数：long total
                                 */
                                return new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits());
                            }
                        });

        //分页参数-总记录数
        long totalElements = page.getTotalElements();
        //总页数
        int totalPages = page.getTotalPages();
        //获取数据结果集
        List<SkuInfo> contents = page.getContent();
        //封装一个Map存储所有数据，并返回
        Map<String,Object> resultMap = new HashMap<String,Object>();

        resultMap.put("rows",contents);
        resultMap.put("total",totalElements);
        resultMap.put("totalPages",totalPages);
         //webSearch 分页
        NativeSearchQuery build = nativeSearchQueryBuilder.build();
        Pageable pageable = build.getPageable();
        resultMap.put("pageNumber",pageable.getPageNumber());
        resultMap.put("pageSize",pageable.getPageSize());
        return resultMap;
    }



    /****
     * 规格汇总合并
     * @param specList
     * @return
     */
    public Map<String, Set<String>> putAllSpec(List<String> specList) {
        //合并后的Map对象:将每个Map对象合成成一个Map<String,Set<String>>
        Map<String,Set<String>> allSpec = new HashMap<String,Set<String>>();

        //1.循环specList   spec={"手机屏幕尺寸":"5.5寸","网络":"联通2G","颜色":"紫","测试":"实施","机身内存":"128G","存储":"16G","像素":"800万像素"}
        for (String spec : specList) {
            //2.将每个JSON字符串转成Map
            Map<String,String> specMap = JSON.parseObject(spec,Map.class);

            //4.合并流程,循环所有Map
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                //4.1取出当前Map，并且获取对应的Key 以及对应 value
                String key = entry.getKey();    //规格名字
                String value = entry.getValue();//规格值

                //4.2将当前循环的数据合并到一个Map<String,Set<String>>中
                //从allSpec中获取当前规格对应的Set集合数据
                Set<String> specSet = allSpec.get(key);
                if(specSet==null){
                    //之前allSpec中没有该规格
                    specSet = new HashSet<String>();
                }
                specSet.add(value);
                allSpec.put(key,specSet);
            }
        }
        return allSpec;
    }



    /****
     * 导入索引库
     */
    @Override
    public void importData() {
      /*  //Feign调用，查询List<Sku>
        Result<List<Sku>> skuResult = skuFeign.importData();

        *//****
         * 将List<Sku>转成List<SkuInfo>
         * List<Sku>->[{skuJSON}]->List<SkuInfo>
         *//*
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()),SkuInfo.class);

        //循环当前SkuInfoList
        for (SkuInfo skuInfo : skuInfoList) {
            //获取spec -> Map(String)->Map类型   {"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"}
            Map<String,Object> specMap = JSON.parseObject(skuInfo.getSpec(),Map.class);
            //如果需要生成动态的域，只需要将该域存入到一个Map<String,Object>对象中即可，该Map<String,Object>的key会生成一个域，域的名字为该Map的key
            //当前Map<String,Object>后面Object的值会作为当前Sku对象该域(key)对应的值
            skuInfo.setSpecMap(specMap);
        }

        //调用Dao实现数据批量导入
        skuEsMapper.saveAll(skuInfoList);*/
    }


















    /*===========================================优化前的代码===================================================*/
    /****
     * 规格分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /***
         * 分组查询规格集合
         * .addAggregation():添加一个聚合操作
         * 1)取别名
         * 2)表示根据哪个域进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合,可以根据多个域进行分组
         * .get("skuSpec"):获取指定域的集合数  [{"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"},
         *                                      {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}
         *                                    ]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuSpec");

        List<String> specList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String specName = bucket.getKeyAsString();//其中的一个规格名字
            specList.add(specName);
        }

        //规格汇总合并
        Map<String, Set<String>> allSpec = putAllSpec(specList);
        return allSpec;
    }

    /****
     * 品牌分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /***
         * 分组查询品牌集合
         * .addAggregation():添加一个聚合操作
         * 1)取别名
         * 2)表示根据哪个域进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合,可以根据多个域进行分组
         * .get("skuBrand"):获取指定域的集合数  [华为,小米,中兴]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuBrand");

        List<String> brandList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();//其中的一个品牌名字
            brandList.add(brandName);
        }
        return brandList;
    }

    /****
     * 分类分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /***
         * 分组查询分类集合
         * .addAggregation():添加一个聚合操作
         * 1)取别名
         * 2)表示根据哪个域进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /***
         * 获取分组数据
         * aggregatedPage.getAggregations():获取的是集合,可以根据多个域进行分组
         * .get("skuCategory"):获取指定域的集合数  [手机,家用电器,手机配件]
         */
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");

        List<String> categoryList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String categoryName = bucket.getKeyAsString();//其中的一个分类名字
            categoryList.add(categoryName);
        }
        return categoryList;
    }
}
