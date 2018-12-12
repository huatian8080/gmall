package com.huatian.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huatian.gmall.service.UserInfoService;
import com.huatian.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> userInfoList() {
        List<UserInfo> list = userInfoMapper.selectAllUserAndAddress();
        return list;
    }
}
