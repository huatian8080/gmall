package com.huatian.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
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

    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request, Model model){
        //判断用户是否登录
        String userId = "";
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
        model.addAttribute("cartList",cartInfoList);
        BigDecimal totalPrice = getTotalPrice(cartInfoList);

        return "cartList";
    }

    private BigDecimal getTotalPrice(List<CartInfo> cartInfoList) {
        BigDecimal totalPrice = new BigDecimal(0);
        //计算购物车被选中商品的总价格
        for (CartInfo cartInfo : cartInfoList) {
            if (cartInfo.getIsChecked().equals("1")){
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }

        }
        return  totalPrice;
    }

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
        String userId = "2";

        if (StringUtils.isNotBlank(userId)){
            //用户已登录
            cartInfo.setUserId("2");
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
