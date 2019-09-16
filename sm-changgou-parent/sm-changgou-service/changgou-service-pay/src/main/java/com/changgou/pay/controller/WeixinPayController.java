package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @Author：Mr.ran &Date：2019/9/10 15:37
 * <p>
 * @Description：
 */

@RestController
@RequestMapping(value = "/weixin/pay")
@CrossOrigin
public class WeixinPayController {

    /***
     * 注入WeixinPayService
     *
     ***/
    @Autowired
    private WeixinPayService weixinPayService;

    /**
     * 注入 RabbitTemplate
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;


    /***
     * 读取配置文件中的信息的对象
     */
    @Autowired
    private Environment env;
    /****
     * 支付结果通知回调方法
     *   #支付回调地址
     *   参考application.yml : notifyurl: http://266g98330k.qicp.vip:25171/weixin/pay//notify/url
     */
    @RequestMapping(value = "/notify/url")
    public String notifyurl(HttpServletRequest request) throws Exception{
        //获取网络输入流
        ServletInputStream is = request.getInputStream();

        //创建一个OutputStream->输入文件中
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len =0;
        while ((len=is.read(buffer))!=-1){
            baos.write(buffer,0,len);
        }

        //微信支付结果的字节数据
        byte[] bytes = baos.toByteArray();
        String xmlresult = new String(bytes,"UTF-8");
        System.out.println(xmlresult);

        //XML字符串->Map
        Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlresult);
        System.out.println(resultMap);
        //发送支付结果给MQ

        //获取自定义的数据
        String attach = resultMap.get("attach");
        Map<String,String> attachMap = JSON.parseObject(attach, Map.class);

        //获取交换机名称
        String exchange = attachMap.get("exchange");
        //获取routingkey名称
        String routingkey = attachMap.get("routingkey");
        //现在队列的名称和交换机就不写死，那么就可以 秒杀对应的交换机和 秒杀对应的订单

        rabbitTemplate.convertAndSend(exchange,routingkey, JSON.toJSONString(resultMap));
        //只有下列这段代码返回之后,微信支付端才能确保把用户微信支付结果返回了给(畅购商城)如果微信支付端没有收到这端代码会默认发送三次支付结果给(畅购商城)
        //现在是返回不了这句代码的因为我们用的是内网穿透 花生壳APP
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }


    /***
     * 微信支付状态查询
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String outtradeno){
        //查询支付状态
        Map map = weixinPayService.queryStatus(outtradeno);
        return new Result(true,StatusCode.OK,"查询支付状态成功！",map);
    }


    /***
     * 创建二维码
     * 普通订单：
     *      exchange:exchange.order
     *      routingkey:queue.order
     *
     * 秒杀订单：
     *     exchange:seckillexchange.seckillorder
     *     routingkey:seckillqueue.seckillorder
     *
     *  exchange+routingkey->JSON->attach
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String, String> paramMap) {
        Map resultMap = weixinPayService.createnative(paramMap);
        return new Result(true, StatusCode.OK, "创建二维码预付订单成功！", resultMap);
    }
}
