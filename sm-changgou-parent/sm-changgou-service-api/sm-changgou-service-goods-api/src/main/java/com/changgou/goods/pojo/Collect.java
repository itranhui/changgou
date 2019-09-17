package com.changgou.goods.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @Author：Mr.ran &Date：2019/9/17 11:18
 * <p>
 * @Description：
 */
@ApiModel(description = "collect",value = "collect")
@Table(name="tb_collect")
public class Collect {
    @Column(name = "username")
    private String username;//分类名称
    @ApiModelProperty(value = "skuId",required = false)
    @Column(name = "skuId")
    private Long  skuId;//商品数量

    public Collect() {
    }

    public Collect(String username, Long skuId) {
        this.username = username;
        this.skuId = skuId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
