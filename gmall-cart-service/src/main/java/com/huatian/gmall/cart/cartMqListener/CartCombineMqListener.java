package com.huatian.gmall.cart.cartMqListener;

import com.alibaba.fastjson.JSON;
import com.huatian.gmall.bean.CartInfo;
import com.huatian.gmall.bean.PaymentInfo;
import com.huatian.gmall.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class CartCombineMqListener {

    @Autowired
    CartService cartService;

    @JmsListener(destination = "COMBINE_CART",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {
        String userId = mapMessage.getString("userId");
        String cartListCookie = mapMessage.getString("cartListCookie");
        if (StringUtils.isNotBlank(cartListCookie)){
            // 通过消息服务，发送异步的消息通知，并行处理购物车合并业务
            cartService.conbineCart(userId, JSON.parseArray(cartListCookie, CartInfo.class));
        }
    }
}
