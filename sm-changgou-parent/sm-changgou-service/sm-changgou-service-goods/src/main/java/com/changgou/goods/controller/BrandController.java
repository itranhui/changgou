package com.changgou.goods.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author：Mr.ran &Date：2019/8/25 15:33
 * <p>
 * @Description：商品Controller
 */
@RestController
@RequestMapping("/brand")
public class BrandController {
    /**
     * 注入BrandService
     */
    @Autowired
    private BrandService brandService;


    /****
     * 查询所有的商品 ，请求方式：GET
     * @return
     */
    @GetMapping("/findAll")
    public Result<Brand> findAll() {
        List<Brand> brands = brandService.findAll();

        return new Result<>(true, StatusCode.OK, "查询全部品牌成功", brands);
    }


    /***
     * 根据ID查询品牌数据 请求方式：POST
     * @param id
     * @return
     */
    @PostMapping("/{id}")
    public Result<Brand> findById(@PathVariable(value = "id") Integer id) {
        Brand brand = brandService.findBrandById(id);
        return new Result<>(true, StatusCode.OK, "查询成功", brand);
    }

    /*****
     * 新增品牌  请求方法：POST
     * @param brand
     * @return
     */
    @PostMapping
    public Result insert(@RequestBody(required = false) Brand brand) {
        brandService.add(brand);
        return new Result(true, StatusCode.OK, "新增品牌成功！");
    }

    /****
     * 修改数据 请求方法：PUT
     * @param brand
     * @return
     */
    @PutMapping("/update")
    public Result update(@RequestBody Brand brand) {
        brandService.update(brand);
        return new Result(true, StatusCode.OK, "修改数据成功");
    }

    /****
     * 根据id 删除品牌数据 请求方法：DELETE
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable(value = "id") Integer id) {
        brandService.deleteById(id);
        return new Result(true, StatusCode.OK, "删除数据成功");
    }

    /*****
     *  根据多条件搜索品牌数据 请求方法：POST
     * @param brand
     * @return
     */
    @PostMapping(value = "/search")
    public Result deleteById(@RequestBody Brand brand) {
        List<Brand> brands = brandService.findList(brand);
        return new Result(true, StatusCode.OK, "查询数据成功", brands);
    }

    /***
     * 分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@PathVariable(value = "page") Integer page, @PathVariable(value = "size") Integer size) {
        PageInfo<Brand> pageInfo = brandService.findPage(page, size);

        return new Result<>(true, StatusCode.OK, "分页查询成功", pageInfo);
    }

    /***
     * 分页搜索实现
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@RequestBody Brand brand, @PathVariable(value = "size") Integer size, @PathVariable(value = "page") Integer page) {
        PageInfo<Brand> pageInfo = brandService.findPage(brand, page, size);
        return new Result<>(true,StatusCode.OK,"分页条件查询成功",pageInfo);
    }

    /*****
     * 根据分类信息查询 对应的品牌信息
     * @param categoryId
     * @return
     */
    @GetMapping("/category/{id}")
    public Result<List<Brand>> findBrandByCategory(@PathVariable(value = "id") Integer categoryId){

      List<Brand> brands =   brandService.findBrandByCategory(categoryId);
      return new Result<>(true,StatusCode.OK,"品牌查询成功",brands);
    }
}
