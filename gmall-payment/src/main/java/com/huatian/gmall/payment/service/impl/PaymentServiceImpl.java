package com.huatian.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.huatian.gmall.bean.PaymentInfo;
import com.huatian.gmall.payment.mapper.PaymentInfoMapper;
import com.huatian.gmall.service.PaymentService;
import com.huatian.gmall.utils.ActiveMQUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePayment(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public boolean checkPayStatus(String out_trade_no) {
        boolean b = false;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfoDB = paymentInfoMapper.selectOne(paymentInfo);
        if (paymentInfoDB != null && paymentInfoDB.getPaymentStatus().equals("未支付")){
            b = true;
        }
        return b;
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",paymentInfo.getOutTradeNo());
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public void sendPaymentSuccessQueue(PaymentInfo paymentInfo) {
        //发送支付成功的消息队列
        //建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOutTradeNo());
            mapMessage.setString("trade_no",paymentInfo.getAlipayTradeNo());
            mapMessage.setString("result",paymentInfo.getPaymentStatus());
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendDelayPaymentResult(PaymentInfo paymentInfo,int count) {
        //发送延迟检查支付的消息队列
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("PAYMENT_CHECK_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("out_trade_no",paymentInfo.getOutTradeNo());
            mapMessage.setInt("count",count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> checkAlipayPayment(String out_trade_no) {
        //支付宝线下查询支付详情
        Map<String,String> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",out_trade_no);
        //调用支付宝支付接口
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        Map<String,String> resultMap = new HashMap<>();
        if(response.isSuccess()){
            System.out.println("调用成功");
            System.err.println(response);
            String tradeStatus = response.getTradeStatus();
            String tradeNo = response.getTradeNo();
            String queryString = response.toString();
            System.err.println(queryString);
            resultMap.put("trade_no",tradeNo);
            resultMap.put("trade_status",tradeStatus);
            resultMap.put("queryString",queryString);
        } else {
            System.out.println("调用失败");
        }
        return resultMap;
    }

    @Override
    public PaymentInfo getPaymentByOutTradeNo(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfoDB = paymentInfoMapper.selectOne(paymentInfo);
        return paymentInfoDB;
    }
}
