package com.huatian.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huatian.gmall.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Reference
    UserInfoService userService;

    @RequestMapping("/userList")
    public List<UserInfo> userList(){
        List<UserInfo> list = userService.userInfoList();

        return list;
    }
}
