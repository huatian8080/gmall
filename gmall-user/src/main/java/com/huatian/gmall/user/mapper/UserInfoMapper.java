package com.huatian.gmall.user.mapper;

import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserInfoMapper extends Mapper<UserInfo>{
    List<UserInfo> selectAllUserAndAddress();
}
