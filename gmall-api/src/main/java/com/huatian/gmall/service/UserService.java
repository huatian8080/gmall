package com.huatian.gmall.service;

import com.huatian.gmall.bean.UserAddress;
import com.huatian.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> userInfoList();

    UserInfo login(UserInfo userInfo);

    UserAddress getUserAddress(String userAddressId);

    void sendCombineCartQueue(String id, String cartListCookie);
}
