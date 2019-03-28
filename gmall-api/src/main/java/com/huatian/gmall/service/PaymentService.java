package com.huatian.gmall.service;

import com.huatian.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePayment(PaymentInfo paymentInfo);

    boolean checkPayStatus(String out_trade_no);

    void updatePayment(PaymentInfo paymentInfo);

    void sendPaymentSuccessQueue(PaymentInfo paymentInfo);

    void sendDelayPaymentResult(PaymentInfo paymentInfo,int count);

    Map<String,String> checkAlipayPayment(String out_trade_no);

    PaymentInfo getPaymentByOutTradeNo(String out_trade_no);
}
