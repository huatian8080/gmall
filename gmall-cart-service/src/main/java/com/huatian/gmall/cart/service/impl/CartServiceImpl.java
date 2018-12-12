package com.huatian.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huatian.gmall.bean.CartInfo;
import com.huatian.gmall.cart.mapper.CartInfoMapper;
import com.huatian.gmall.service.CartService;
import com.huatian.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo getCartExist(CartInfo cartInfo) {
        CartInfo cartInfo1 = new CartInfo();
        cartInfo1.setUserId(cartInfo.getUserId());
        cartInfo1.setSkuId(cartInfo.getSkuId());
        return cartInfoMapper.selectOne(cartInfo1);
    }

    @Override
    public void saveCart(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void updateCart(CartInfo cartInfo) {
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",cartInfo.getUserId())
                .andEqualTo("skuId",cartInfo.getSkuId());
        cartInfoMapper.updateByExampleSelective(cartInfo,example);
    }

    @Override
    public void flushCache(String userId) {
        Jedis jedis = redisUtil.getJedis();
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfoList = cartInfoMapper.select(cartInfo);
        jedis.del("user:"+userId+":cart");
        Map<String,String> map = new HashMap<>();
        for (CartInfo info : cartInfoList) {
            map.put(info.getSkuId(), JSON.toJSONString(info));
        }
        jedis.hmset("user:"+userId+":cart",map);
        jedis.close();

    }

    @Override
    public List<CartInfo> getCartListFromCache(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals("user:" + userId + ":cart");
        if (hvals != null && hvals.size()>0){
            for (String hval : hvals) {
                CartInfo cartInfo = JSON.parseObject(hval,CartInfo.class);
                cartInfoList.add(cartInfo);
            }
        }
        jedis.close();
        return cartInfoList;
    }
}
