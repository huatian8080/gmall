package com.huatian.gmall.manage.Test;

import redis.clients.jedis.Jedis;

public class Test {
    public static void main(String[] args) {
        /*Jedis jedis = new Jedis("120.78.197.227", 6379);
        jedis.auth("123456");
        String ping = jedis.get("name");
        System.out.println(ping);
        jedis.close();*/
        int n =5;

        int num = fun(5);

    }
    public static int fun(int  n){
        if(n>1){
            return n*(n-1);
        }else{
            return n;
        }

    }
}
