package com.huatian.gmall.payment.paymentListener;

import com.huatian.gmall.bean.OrderInfo;
import com.huatian.gmall.bean.PaymentInfo;
import com.huatian.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PayStatusCheckListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        int count = mapMessage.getInt("count");
        System.err.println("第"+count+"次");
        count--;
        //检查支付状态
        Map<String,String> result = paymentService.checkAlipayPayment(out_trade_no);
        String trade_status = result.get("trade_status");
        String trade_no = result.get("trade_no");
        String queryString = result.get("queryString");
        if (trade_status != null && (trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED"))){
            boolean b = paymentService.checkPayStatus(out_trade_no);
            if (b){
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                paymentInfo.setCreateTime(new Date());
                paymentInfo.setAlipayTradeNo(trade_no);
                paymentInfo.setCallbackContent(queryString);
                paymentInfo.setPaymentStatus("已支付");
                paymentService.updatePayment(paymentInfo);
                //发送支付成功的消息队列
                paymentService.sendPaymentSuccessQueue(paymentInfo);
            }else {
                System.out.println("支付状态已更新,不需要重复更新");
            }
        }else {
            if (count > 0){
                System.err.println("剩余"+count);
                PaymentInfo paymentInfo = paymentService.getPaymentByOutTradeNo(out_trade_no);
                paymentService.sendDelayPaymentResult(paymentInfo,count);
            }else {
                System.err.println("次数用尽,停止检查");
            }
        }
    }
}
