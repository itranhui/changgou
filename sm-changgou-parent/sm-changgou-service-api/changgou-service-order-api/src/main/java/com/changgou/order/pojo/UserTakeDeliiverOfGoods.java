package com.changgou.order.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/****
 * @Author:shenkunlin
 * @Description:UserTakeDeliiverOfGoods构建
 * @Date 2019/6/14 19:13
 *****/
@ApiModel(description = "UserTakeDeliiverOfGoods",value = "UserTakeDeliiverOfGoods")
@Table(name="tb_user_take_deliiver_of_goods")
public class UserTakeDeliiverOfGoods implements Serializable{

	@ApiModelProperty(value = "表id",required = false)
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
	private Integer id;//表id

	@ApiModelProperty(value = "用户id",required = false)
    @Column(name = "username")
	private String username;//用户id

	@ApiModelProperty(value = "订单id",required = false)
    @Column(name = "orderId")
	private String orderId;//订单id

	@ApiModelProperty(value = "订单支付时间",required = false)
    @Column(name = "order_pay_time")
	private Date orderPayTime;//订单支付时间

	@ApiModelProperty(value = "提醒发货时间",required = false)
    @Column(name = "remind_goods_time")
	private Date remindGoodsTime;//提醒发货时间

	@ApiModelProperty(value = "收货时间",required = false)
    @Column(name = "take_delivery_of_goods")
	private Date takeDeliveryOfGoods;//收货时间

	@ApiModelProperty(value = "是否收货",required = false)
    @Column(name = "is_toke")
	private String isToke;//是否收货



	//get方法
	public Integer getId() {
		return id;
	}

	//set方法
	public void setId(Integer id) {
		this.id = id;
	}
	//get方法
	public String getUsername() {
		return username;
	}

	//set方法
	public void setUsername(String username) {
		this.username = username;
	}
	//get方法
	public String getOrderId() {
		return orderId;
	}

	//set方法
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	//get方法
	public Date getOrderPayTime() {
		return orderPayTime;
	}

	//set方法
	public void setOrderPayTime(Date orderPayTime) {
		this.orderPayTime = orderPayTime;
	}
	//get方法
	public Date getRemindGoodsTime() {
		return remindGoodsTime;
	}

	//set方法
	public void setRemindGoodsTime(Date remindGoodsTime) {
		this.remindGoodsTime = remindGoodsTime;
	}
	//get方法
	public Date getTakeDeliveryOfGoods() {
		return takeDeliveryOfGoods;
	}

	//set方法
	public void setTakeDeliveryOfGoods(Date takeDeliveryOfGoods) {
		this.takeDeliveryOfGoods = takeDeliveryOfGoods;
	}
	//get方法
	public String getIsToke() {
		return isToke;
	}

	//set方法
	public void setIsToke(String isToke) {
		this.isToke = isToke;
	}


}
