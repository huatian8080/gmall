package com.huatian.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.huatian.gmall.annotations.LoginRequired;
import com.huatian.gmall.bean.CartInfo;
import com.huatian.gmall.bean.SkuInfo;
import com.huatian.gmall.service.CartService;
import com.huatian.gmall.service.SkuService;
import com.huatian.gmall.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;


    @LoginRequired(isNeedSuccess = false)
    @RequestMapping("/allCheckCart")
    public String allCheckCart(HttpServletRequest request,HttpServletResponse response,
                               String isAllCheckFlag,Model model){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = new ArrayList<>();


        if (StringUtils.isBlank(userId)){
            //用户未登录,取cookie中的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                cartInfoList = JSON.parseArray(cartListCookie,CartInfo.class);
            }
        }else {
            //用户登录查询数据库
            cartInfoList = cartService.getCartListFromUserId(userId);
        }
        if (cartInfoList !=null && cartInfoList.size() > 0){
            for (CartInfo cartInfo : cartInfoList) {
                cartInfo.setIsChecked(isAllCheckFlag);
            }
        }
        if (StringUtils.isNotBlank(userId)){
            //更新数据库
            cartService.updateAllCart(userId,isAllCheckFlag);
        }
        if (StringUtils.isBlank(userId)){
            //更新cookie
            CookieUtil.setCookie(request,response,"cartListCookie",
                    JSON.toJSONString(cartInfoList),60*60*24,true);
        }else {
            //刷新缓存
            cartService.flushCache(userId);
        }
        model.addAttribute("userId",userId);
        model.addAttribute("cartList",cartInfoList);
        model.addAttribute("totalPrice",getTotalPrice(cartInfoList));
        return "cartListInner";
    }

    @LoginRequired(isNeedSuccess = false)
    @RequestMapping("/checkCart")
    public String checkCart(HttpServletRequest request,HttpServletResponse response,
                            String skuId,String isCheckedFlag,Model model,String abc){
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isBlank(userId)){
            //用户未登录,取cookie中的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                cartInfoList = JSON.parseArray(cartListCookie,CartInfo.class);
            }
        }else {
            //用户登录查询数据库
            cartInfoList = cartService.getCartListFromUserId(userId);
        }
        if (cartInfoList !=null && cartInfoList.size() > 0){
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfo.setIsChecked(isCheckedFlag);
                    if (StringUtils.isNotBlank(userId)){
                        //更新数据库
                        cartService.updateCart(cartInfo);
                    }
                }
            }
        }
        if (StringUtils.isBlank(userId)){
            //更新cookie
            CookieUtil.setCookie(request,response,"cartListCookie",
                    JSON.toJSONString(cartInfoList),60*60*24,true);
        }else {
            //刷新缓存
            cartService.flushCache(userId);
        }
        model.addAttribute("userId",userId);
        model.addAttribute("cartList",cartInfoList);
        model.addAttribute("totalPrice",getTotalPrice(cartInfoList));
        return "cartListInner";
    }

    @LoginRequired(isNeedSuccess = false)
    @RequestMapping("/cartList")
    public String cartList(String skuId,String message, HttpServletRequest request, Model model){
        System.err.println(request.getRequestURL());
        //判断用户是否登录
        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartInfoList = null;
        if (StringUtils.isBlank(userId)){
            //取cookie中的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)){
                cartInfoList = JSON.parseArray(cartListCookie, CartInfo.class);
            }
        }else {
            //取缓存中的数据
            cartInfoList = cartService.getCartListFromCache(userId);
        }
        model.addAttribute("message",message);
        model.addAttribute("skuId",skuId);
        model.addAttribute("cartList",cartInfoList);
        BigDecimal totalPrice = getTotalPrice(cartInfoList);
        model.addAttribute("totalPrice",totalPrice);
        return "cartList";
    }

    private BigDecimal getTotalPrice(List<CartInfo> cartInfoList) {
        BigDecimal totalPrice = new BigDecimal(0);
        //计算购物车被选中商品的总价格
        if (cartInfoList != null){
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getIsChecked().equals("1")){
                    totalPrice = totalPrice.add(cartInfo.getCartPrice());
                }

            }
        }
        return  totalPrice;
    }
    @LoginRequired(isNeedSuccess = false)
    @RequestMapping("/addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, int num){
        SkuInfo skuInfo = skuService.getSkuById(skuId);
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(num)));
        cartInfo.setSkuNum(num);
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setIsChecked("1");
        cartInfo.setSkuName(skuInfo.getSkuName());
        String userId = (String) request.getAttribute("userId");

        if (StringUtils.isNotBlank(userId)){
            //用户已登录
            cartInfo.setUserId(userId);
            CartInfo cartInfoDb = cartService.getCartExist(cartInfo);
            if (cartInfoDb == null){
                //数据库中没有,进行新增操作
                cartService.saveCart(cartInfo);
            }else {
                //db中没有,进行更新
                cartInfoDb.setSkuNum(cartInfoDb.getSkuNum()+num);
                cartInfoDb.setCartPrice(cartInfoDb.getSkuPrice().multiply(new BigDecimal(cartInfoDb.getSkuNum())));
                cartService.updateCart(cartInfoDb);
            }
            //同步购物车缓存
            cartService.flushCache(cartInfo.getUserId());
        }else {
            //用户未登录
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            List<CartInfo> cartInfoList = new ArrayList<>();
            if (StringUtils.isBlank(cartListCookie)){
                cartInfoList.add(cartInfo);
            }else {
                cartInfoList = JSON.parseArray(cartListCookie, CartInfo.class);
                boolean b = if_sku_NotExists(cartInfoList,cartInfo);
                if (b){
                    cartInfoList.add(cartInfo);
                }else {
                    for (CartInfo info : cartInfoList) {
                        if(info.getSkuId().equals(cartInfo.getSkuId())){
                            info.setSkuNum(info.getSkuNum()+num);
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            }
            CookieUtil.setCookie(request,response,"cartListCookie",
                    JSON.toJSONString(cartInfoList),60*60*24,true);
        }
        return  "redirect:/success.html";
    }

    private boolean if_sku_NotExists(List<CartInfo> cartInfoList, CartInfo cartInfo) {
        boolean exist = true;
        for (CartInfo info : cartInfoList) {
            if (info.getSkuId().equals(cartInfo.getSkuId())){
                exist = false;
                break;
            }
        }
        return  exist;
    }


    @RequestMapping("/addToSuccess")
    public String cartAddSuccess(){
        return  "success";
    }
}
