package com.huatian.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.huatian.gmall.bean.OrderDetail;
import com.huatian.gmall.bean.OrderInfo;
import com.huatian.gmall.bean.PaymentInfo;
import com.huatian.gmall.payment.config.AlipayConfig;
import com.huatian.gmall.service.OrderService;
import com.huatian.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;
    @Autowired
    PaymentService paymentService;
    @Reference
    OrderService orderService;

    @RequestMapping("/index")
    public String toIndex(String outTradeNo, String totalAmount, Model model){
        model.addAttribute("outTradeNo",outTradeNo);
        model.addAttribute("totalAmount",totalAmount);
        return "index";
    }

    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, Model model){
        model.addAttribute("outTradeNo",outTradeNo);
        model.addAttribute("totalAmount",totalAmount);
        OrderInfo orderInfo = orderService.getOrderByOutTradeNo(outTradeNo);
        //保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setSubject(orderInfo.getOrderDetailList().get(0).getSkuName());
        paymentInfo.setPaymentStatus("未支付");
        paymentInfo.setCreateTime(new Date());
        paymentService.savePayment(paymentInfo);

        //对接支付宝的pagepay接口，公共参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址

        //填充业务参数
        Map<String,String> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount","0.01");

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        map.put("subject",orderDetailList.get(0).getSkuName());
        alipayRequest.setBizContent(JSON.toJSONString(map));//填充业务参数

        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
            System.err.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //启动延迟检查支付状态的队列
        paymentService.sendDelayPaymentResult(paymentInfo,5);

        return form;
    }
    @RequestMapping("/alipay/callback/return")
    public String callbackReturn(HttpServletRequest request){
        // 付款成功 // 修改支付状态
        String notify_time = (String)request.getParameter("notify_time");
        String app_id = (String)request.getParameter("app_id");
        String sign = (String)request.getParameter("sign");
        System.err.println(sign);
        String trade_no = (String)request.getParameter("trade_no");
        String out_trade_no = (String)request.getParameter("out_trade_no");
        String trade_status = (String)request.getParameter("trade_status");
        String total_amount = (String)request.getParameter("total_amount");
        //等幂性检查
        boolean b = paymentService.checkPayStatus(out_trade_no);
        if (b){
            //调用SDK验证签名
            Map<String,String> map = new HashMap<>();
            try {
                boolean signVerified = AlipaySignature.rsaCheckV1(map, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            //更新支付信息
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(request.getQueryString());
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentService.updatePayment(paymentInfo);
            // 通知订单系统，修改订单状态
            paymentService.sendPaymentSuccessQueue(paymentInfo);
        }
        return "finish";
    }
}
