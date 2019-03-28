package com.huatian.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huatian.gmall.annotations.LoginRequired;
import com.huatian.gmall.bean.*;
import com.huatian.gmall.bean.enums.PaymentWay;
import com.huatian.gmall.service.CartService;
import com.huatian.gmall.service.OrderService;
import com.huatian.gmall.service.SkuService;
import com.huatian.gmall.service.UserService;
import com.huatian.gmall.utils.HttpClientUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderController {

    @Reference
    OrderService orderService;
    @Reference
    CartService cartService;
    @Reference
    UserService userService;
    @Reference
    SkuService skuService;

    @LoginRequired(isNeedSuccess = true)
    @RequestMapping("/submitOrder")
    public String submitOrder(HttpServletRequest request,OrderInfo orderInfo,
                              String tradeCode,Model model,String userAddressId){
        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        //防止订单重复提交
        boolean b = orderService.checkTradeCode(userId,tradeCode);
        if (b){
            UserAddress userAddress = userService.getUserAddress(userAddressId);
            //生成订单和订单数据
            List<CartInfo> cartListFromUserId = cartService.getCartListFromUserId(userId);
            //设置订单各项的值
            OrderInfo orderInfoForDB = new OrderInfo();
            orderInfoForDB.setConsignee(userAddress.getConsignee());
            orderInfoForDB.setConsigneeTel(userAddress.getPhoneNum());
            orderInfoForDB.setTotalAmount(getTotalPrice(cartListFromUserId));
            orderInfoForDB.setOrderStatus("订单已提交");
            orderInfoForDB.setProcessStatus("订单已提交");
            orderInfoForDB.setUserId(userId);
            orderInfoForDB.setPaymentWay(PaymentWay.ONLINE);
            orderInfoForDB.setCreateTime(new Date());
            //设置过期时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,1);
            orderInfoForDB.setExpireTime(calendar.getTime());
            // 外部订单号
            // atguigugmall+毫秒时间戳字符串+订单生成的时间字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(new Date());
            String outTradeNo = "huatian"+System.currentTimeMillis()+format;
            orderInfoForDB.setOutTradeNo(outTradeNo);

            orderInfoForDB.setDeliveryAddress(userAddress.getUserAddress());
            orderInfoForDB.setOrderComment("华天订单");
            // 需要被删除的购物车的集合
            List<String> delCartIdList = new ArrayList<>();
            //订单详情集合
            List<OrderDetail> orderDetailList = new ArrayList<>();
            for (CartInfo cartInfo : cartListFromUserId) {
                if (cartInfo.getIsChecked().equals("1")){
                    //验价
                    String skuId = cartInfo.getSkuId();
                    SkuInfo skuInfo = skuService.getSkuById(skuId);
                    if (!skuInfo.getPrice().equals(cartInfo.getSkuPrice())){
                        cartInfo.setSkuPrice(skuInfo.getPrice());
                        cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
                        cartService.updateCart(cartInfo);
                    }
                    //该数据是从数据库中获取,无需验价
                    //验库存,远程通过ws调用库存接口
                    String doGet = HttpClientUtil.doGet("http://manage.gware.com:9001/hasStock?skuId=" + cartInfo.getSkuId()
                            + "&num=" + cartInfo.getSkuNum());
                    if (doGet.equals("0")){
                        cartService.flushCache(userId);
                        return "redirect:http://cart.gmall.com:8084/cartList?message="+
                                "该商品库存不足,请重新选购"+"&skuId="+cartInfo.getSkuId();
                    }
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setSkuNum(cartInfo.getSkuNum());
                    orderDetail.setImgUrl(cartInfo.getImgUrl());
                    orderDetail.setOrderPrice(cartInfo.getCartPrice());
                    orderDetail.setSkuId(cartInfo.getSkuId());
                    orderDetail.setSkuName(cartInfo.getSkuName());
                    orderDetailList.add(orderDetail);
                    delCartIdList.add(cartInfo.getId());
                }
            }
            orderInfoForDB.setOrderDetailList(orderDetailList);
            orderService.saveOrder(orderInfoForDB);
            //删除购物车数据
            orderService.delCheckedCart(delCartIdList);
            //刷新购物车缓存
            cartService.flushCache(userId);
            //重定向到支付页面

            return "redirect:http://payment.gmall.com:8090/index?outTradeNo="+outTradeNo+"&totalAmount="+getTotalPrice(cartListFromUserId);
        }else{
            return "tradeFail";
        }




    }

    @LoginRequired(isNeedSuccess = true)
    @RequestMapping("/toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map){
        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        //查询收货地址
        List<UserAddress> userAddressList = orderService.getAddressListByUserId(userId);
        //查询用户购物车信息
        List<CartInfo> cartList = cartService.getCartListFromUserId(userId);
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getIsChecked().equals("1")){
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetailList.add(orderDetail);
            }
        }
        //生成结算验证码,只允许结算一次
        String tradeCode = UUID.randomUUID().toString();
        orderService.putTradeCodeForCache(userId,tradeCode);
        map.put("tradeCode",tradeCode);
        map.put("nickName",nickName);
        map.put("userAddressList",userAddressList);
        map.put("orderDetailList",orderDetailList);
        map.put("totalAmount",getTotalPrice(cartList));
        return "trade";
    }
    // 计算购物车中被选中商品的总价格
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
}
