package com.huatian.gmall.test;

import com.huatian.gmall.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    public static void main(String[] args) {
        Map<String,String> map = new HashMap<>();
        map.put("userId","2");
        map.put("nickName","zhangsan");
        String salt = "127.0.0.1";
        String gmall = JwtUtil.encode("gmall", map, salt);
        //System.err.println(gmall);
        Map gmall1 = JwtUtil.decode("gmll", gmall, "127.0.0.2");
        System.out.println("---------------");
        System.out.println(map);

    }
}
