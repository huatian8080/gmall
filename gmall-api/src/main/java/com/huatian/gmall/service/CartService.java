package com.huatian.gmall.service;

import com.huatian.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    CartInfo getCartExist(CartInfo cartInfo);

    void saveCart(CartInfo cartInfo);

    void updateCart(CartInfo cartInfo);

    void flushCache(String userId);

    List<CartInfo> getCartListFromCache(String userId);

    List<CartInfo> getCartListFromUserId(String userId);


    void updateAllCart(String userId,String isAllCheckFlag);

    void conbineCart(String id, List<CartInfo> cartInfos);
}
