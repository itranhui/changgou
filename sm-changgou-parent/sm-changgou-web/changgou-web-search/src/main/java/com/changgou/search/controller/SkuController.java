package com.changgou.search.controller;

import com.changgou.entity.Page;
import com.changgou.search.feign.SkuSearchFeign;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/3 15:20
 * <p>
 * @Description：
 */

@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuSearchFeign skuSearchFeign;

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map searchMap, Model model){

        //替换特殊字符
        handerSearchMap(searchMap);


        //调用changgou-service-search微服务
        Map<String,Object> resultMap = skuSearchFeign.search(searchMap);
        model.addAttribute("result",resultMap);

        //将条件存储用于页面回显数据
        model.addAttribute("searchMap",searchMap);


        //获取拼接条件 回显页面
        String  [] urls = url(searchMap);
        model.addAttribute("url",urls[0]);
        model.addAttribute("sorturl",urls[1]);

        //计算分页
        Page<SkuInfo> pageInfo = new Page<SkuInfo>(
                Long.parseLong(resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("pageNumber").toString())+1,
                Integer.parseInt(resultMap.get("pageSize").toString())
        );
        model.addAttribute("pageInfo",pageInfo);
        return "search";
    }

    /*****
     * 替换特殊字符
     * @param searchMap
     */
    public void handerSearchMap (Map<String,String> searchMap){
        if (searchMap!=null){
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {

                if (entry.getKey().startsWith("spec_")){
                    entry.setValue(entry.getValue().replace("+","%2B"));
                }
            }
        }
    }

    /****
     * 拼接url 路径
     * @param searchMap
     * @return
     */
    private String [] url(Map<String,Object> searchMap) {
        //初始化地址
        String url = "/search/list";
        //排序地址
        String sorturl = "/search/list";
        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            sorturl += "?";
            for (Map.Entry<String, Object> entry : searchMap.entrySet()) {
                //key是搜索的条件对象
                String key = entry.getKey();
                //跳过分页参数
                if (key.equalsIgnoreCase("pageNum")) {
                    continue;
                }
                //value搜索的值
                String value = (String) entry.getValue();
                url += key + "=" + value + "&";

                //排序参数，跳过
                if (key.equalsIgnoreCase("sortField") || key.equalsIgnoreCase("sortRule")) {
                    continue;
                }
                sorturl += key + "=" + value + "&";
            }

            //去掉最后一个&
            url = url.substring(0, url.length() - 1);
            sorturl = sorturl.substring(0, sorturl.length() - 1);
        }
        return new String[]{url, sorturl};
    }


}