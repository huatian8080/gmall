package com.huatian.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huatian.gmall.bean.BaseAttrInfo;
import com.huatian.gmall.bean.SkuInfo;
import com.huatian.gmall.bean.SpuImage;
import com.huatian.gmall.bean.SpuSaleAttr;
import com.huatian.gmall.service.BaseAttrService;
import com.huatian.gmall.service.SkuService;
import com.huatian.gmall.service.SpuImgService;
import com.huatian.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuController {

    @Reference
    SkuService skuService;
    @Reference
    BaseAttrService baseAttrService;
    @Reference
    SpuService spuService;
    @Reference
    SpuImgService spuImgService;

    @RequestMapping("/saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){
        skuService.saveSku(skuInfo);
        return "SUCCESS";
    }


    @RequestMapping("/getSpuImgList")
    @ResponseBody
    public List<SpuImage> getSpuImgList(String spuId){
        List<SpuImage> spuImageList = spuImgService.getSpuImgList(spuId);
        return  spuImageList;
    }

    @RequestMapping("/spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        List<SpuSaleAttr> spuSaleAttrList = spuService.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @RequestMapping("/getSkuInfoListByspuId")
    @ResponseBody
    public List<SkuInfo> getSkuList(String spuId){
        List<SkuInfo> skuInfos = skuService.getSkuList(spuId);
        return  skuInfos;
    }

    @RequestMapping("/attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id){
        List<BaseAttrInfo> attrInfoList = baseAttrService.getAttrInfoList(catalog3Id);
        return  attrInfoList;
    }
}
