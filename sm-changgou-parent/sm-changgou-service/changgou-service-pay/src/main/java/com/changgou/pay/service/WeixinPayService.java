package com.changgou.pay.service;

import java.util.Map;

/**
 * 微信支付等操作的接口
 */
public interface WeixinPayService {
    /*****
     * 创建二维码
     * @param //out_trade_no : 客户端自定义订单编号
     * @param //total_fee    : 交易金额,单位：分
     * @return
     */
     Map createnative(Map<String, String> parameterMap) ;

    /***
     * 微信支付状态的查询
     * @param outtradeno
     * @return
     */
    Map queryStatus(String outtradeno);
}
