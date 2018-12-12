package com.huatian.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.huatian.gmall.bean.SkuAttrValue;
import com.huatian.gmall.bean.SkuInfo;
import com.huatian.gmall.bean.SkuSaleAttrValue;
import com.huatian.gmall.bean.SpuSaleAttr;
import com.huatian.gmall.service.SkuService;
import com.huatian.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") String skuId,Model model){
        SkuInfo skuInfo = skuService.getSkuById(skuId);
        model.addAttribute("skuInfo",skuInfo);

        //根据spuId查询销售属性列表
        String spuId = skuInfo.getSpuId();
        List<SpuSaleAttr> spuSaleAttrList = spuService.getSpuSaleAttrListBySpuId(skuId,spuId);
        model.addAttribute("spuSaleAttrListCheckBySku",spuSaleAttrList);

        //根据spuId制作页面销售属性的hash表
        //将属性值id和skuId组合
        List<SkuInfo> skuInfoList = skuService.getSkuAttrValueListBySpuId(spuId);
        Map<String,String> stringMap = new HashMap<>();
        for (SkuInfo info : skuInfoList) {
            String skuSaleAttrValueIdsKey = "";
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValueIdsKey = skuSaleAttrValueIdsKey +"|" +skuSaleAttrValue.getSaleAttrValueId();
            }
            String skuIdValue = info.getId();
            stringMap.put(skuSaleAttrValueIdsKey,skuIdValue);
        }
        String s = JSON.toJSONString(stringMap);
        model.addAttribute("valuesSkuJson",s);
        return  "item";
    }



    @RequestMapping("test.html")
    public String test(Model model){
        model.addAttribute("hello","test");
        model.addAttribute("num",10);
        List<String> list = new ArrayList<>();
        list.add("zhangsan");
        list.add("lisi");
        list.add("wangwu");
        model.addAttribute("list",list);
        model.addAttribute("name","a函数");
        return  "test";
    }
}
