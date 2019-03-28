package com.huatian.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huatian.gmall.bean.OrderDetail;
import com.huatian.gmall.bean.OrderInfo;
import com.huatian.gmall.bean.UserAddress;
import com.huatian.gmall.order.mapper.OrderDetailMapper;
import com.huatian.gmall.order.mapper.OrderInfoMapper;
import com.huatian.gmall.order.mapper.UserAddressMapper;
import com.huatian.gmall.service.OrderService;
import com.huatian.gmall.utils.ActiveMQUtil;
import com.huatian.gmall.utils.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Override
    public List<UserAddress> getAddressListByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);
        return userAddressList;
    }

    @Override
    public void putTradeCodeForCache(String userId,String tradeCode) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+userId+":tradeCode",60*30,tradeCode);
        jedis.close();
    }

    @Override
    public boolean checkTradeCode(String userId,String tradeCode) {
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeCache = jedis.get("user:" + userId + ":tradeCode");
        if (StringUtils.isNotBlank(tradeCodeCache)){
            if (tradeCodeCache.equals(tradeCode)){
                jedis.del("user:" + userId + ":tradeCode");
                return true;
            }
        }
        return false;
    }

    @Override
    public void saveOrder(OrderInfo orderInfoForDB) {
        orderInfoMapper.insertSelective(orderInfoForDB);
        String orderId = orderInfoForDB.getId();
        List<OrderDetail> orderDetailList = orderInfoForDB.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderId);
            orderDetailMapper.insertSelective(orderDetail);
        }
    }

    @Override
    public void delCheckedCart(List<String> delCartIdList) {
        String delCartStr = StringUtils.join(delCartIdList, ",");
        orderInfoMapper.delCheckedCart(delCartStr);
    }

    @Override
    public OrderInfo getOrderByOutTradeNo(String outTradeNo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        OrderInfo orderInfoDB = orderInfoMapper.selectOne(orderInfo);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfoDB.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfoDB.setOrderDetailList(orderDetailList);
        return orderInfoDB;
    }

    @Override
    public void updateProcessStatus(String out_trade_no, String result, String trade_no) {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(out_trade_no);
        orderInfo.setOrderStatus(result);
        orderInfo.setProcessStatus(result);
        orderInfo.setTrackingNo(trade_no);
        orderInfoMapper.updateByExampleSelective(orderInfo,example);
    }

    @Override
    public void sendOrderResult(String out_trade_no) {
        // 发送订单结果的消息通知给系统
        // 发送订单支付成功的消息队列
        // 建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("ORDER_SUCCESS_QUEUE");

            MessageProducer producer = session.createProducer(testqueue);
            TextMessage textMessage=new ActiveMQTextMessage();

            OrderInfo order = getOrderByOutTradeNo(out_trade_no);

            textMessage.setText(JSON.toJSONString(order));
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(textMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProcessStatusByOrderId(String orderId, String status) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(status);
        orderInfo.setOrderStatus(status);
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }
}
