package com.huatian.gmall.interceptor;

import com.huatian.gmall.annotations.LoginRequired;
import com.huatian.gmall.utils.CookieUtil;
import com.huatian.gmall.utils.HttpClientUtil;
import com.huatian.gmall.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);
        //不需要验证是否登录
        if (methodAnnotation == null) {
            return true;
        }
        String token = "";
        //从cookie中获取token,说明用户登录过一次
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
        System.err.println(oldToken);
        //从浏览器地址栏中获取token,说明该用户第一次登录
        String newToken = request.getParameter("newToken");
        //四种情况
        //oldToken为null,newtoken为null，从为登录过
        //oldToken空，newToken不空，第一次登陆

        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        boolean needSuccess = methodAnnotation.isNeedSuccess();

        if (StringUtils.isNotBlank(token)) {
            //验证token的工具
            // 通过负载均衡nginx
            String ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";
                }
            }
            String doGet =
                    HttpClientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token + "&currentIp=" + ip);

            //token合法通过
            if (doGet.equals("SUCCESS")) {
                //刷新cookie中的token
                CookieUtil.setCookie(request,response,"oldToken",token,60*60*24,true);
                Map gmall = JwtUtil.decode("gmall", token, ip);
                request.setAttribute("userId",gmall.get("userId"));
                request.setAttribute("nickName",gmall.get("nickName"));
                return true;
            }
        }
        //token为null或者token验证不通过
        if (needSuccess == true) {
            response.sendRedirect("http://passport.gmall.com:8085/loginPage?originUrl=" + request.getRequestURL());
            return false;
        }

        //token为null或验证失败,但不需要登陆通过
        return true;


    }
}
