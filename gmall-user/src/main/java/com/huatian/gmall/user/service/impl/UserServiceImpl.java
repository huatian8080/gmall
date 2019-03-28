package com.huatian.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huatian.gmall.bean.UserAddress;
import com.huatian.gmall.bean.UserInfo;
import com.huatian.gmall.service.UserService;
import com.huatian.gmall.user.mapper.UserAddressMapper;
import com.huatian.gmall.user.mapper.UserInfoMapper;
import com.huatian.gmall.utils.ActiveMQUtil;
import com.huatian.gmall.utils.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.jms.*;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserAddressMapper userAddressMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Override
    public List<UserInfo> userInfoList() {
        List<UserInfo> list = userInfoMapper.selectAllUserAndAddress();
        return list;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        UserInfo userDB = userInfoMapper.selectOne(userInfo);
        if (userDB != null){
            Jedis jedis = redisUtil.getJedis();
            String userString = JSON.toJSONString(userDB);
            jedis.setex("user:"+userDB.getId()+":info",60*60*24,userString);
        }
        return userDB;
    }

    @Override
    public UserAddress getUserAddress(String userAddressId) {
        UserAddress userAddress = userAddressMapper.selectByPrimaryKey(userAddressId);
        return userAddress;
    }

    @Override
    public void sendCombineCartQueue(String id, String cartListCookie) {
        //发送合并购物车的小时队列
        //建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testqueue = session.createQueue("COMBINE_CART");

            MessageProducer producer = session.createProducer(testqueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("userId",id);
            mapMessage.setString("cartListCookie",cartListCookie);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);
            session.commit();
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
