package com.huatian.gmall.service;

import com.huatian.gmall.bean.OrderInfo;
import com.huatian.gmall.bean.UserAddress;

import java.util.List;

public interface OrderService {
    List<UserAddress> getAddressListByUserId(String userId);

    void putTradeCodeForCache(String userId,String tradeCode);

    boolean checkTradeCode(String userId,String tradeCode);

    void saveOrder(OrderInfo orderInfoForDB);

    void delCheckedCart(List<String> delCartIdList);

    OrderInfo getOrderByOutTradeNo(String outTradeNo);

    void updateProcessStatus(String out_trade_no, String result, String trade_no);

    void sendOrderResult(String out_trade_no);

    void updateProcessStatusByOrderId(String orderId, String status);
}
