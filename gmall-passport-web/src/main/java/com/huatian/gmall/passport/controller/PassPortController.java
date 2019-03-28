package com.huatian.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.huatian.gmall.bean.CartInfo;
import com.huatian.gmall.bean.UserInfo;
import com.huatian.gmall.service.CartService;
import com.huatian.gmall.service.UserService;
import com.huatian.gmall.utils.CookieUtil;
import com.huatian.gmall.utils.JwtUtil;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.jms.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassPortController {

    @Reference
    UserService userService;

    @RequestMapping("/loginPage")
    public String toLogin(String originUrl, Model model){
        model.addAttribute("originUrl",originUrl);
        return  "index";
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request,HttpServletResponse response){
        //验证用户名和密码
        UserInfo userDB = userService.login(userInfo);
        //验证成功,将用户名和密码生成token,然后将用户的信息提取到redis中,并设置过期时间
        if (userDB != null){
            // 通过负载均衡nginx
            String ip = request.getHeader("x-forwarded-for");
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
            Map<String,String> map = new HashMap<>();
            map.put("userId",userDB.getId());
            map.put("nickName",userDB.getNickName());
            String token = JwtUtil.encode("gmall", map, ip);
            //合并购物车,发送合并购物车的消息队列
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            CookieUtil.deleteCookie(request,response,"cartListCookie");
            userService.sendCombineCartQueue(userDB.getId(),cartListCookie);

            return token;
        }
        return "Failed";
    }

    //验证token
    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token,String currentIp){

        Map gmall = null;
        try {
            gmall = JwtUtil.decode("gmall", token, currentIp);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        if (gmall == null){
            return "Failed";
        }
        return  "SUCCESS";
    }


}
