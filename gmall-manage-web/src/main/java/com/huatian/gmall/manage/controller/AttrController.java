package com.huatian.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huatian.gmall.bean.BaseAttrInfo;
import com.huatian.gmall.bean.BaseAttrValue;
import com.huatian.gmall.bean.BaseSaleAttr;
import com.huatian.gmall.service.BaseAttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrController {

    @Reference
    BaseAttrService baseAttrService;


    @RequestMapping("/attrListPage")
    public String toAttrListPage(){
        return "attrListpage";
    }

    @RequestMapping("/delAttrInfo")
    @ResponseBody
    public String delAttrInfo(String attrId){
        baseAttrService.delAttrInfo(attrId);
        return "SUCCESS";
    }

    @RequestMapping("/getAttrList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrList(String catalog3Id){
        List<BaseAttrInfo> baseAttrInfos = baseAttrService.getAttrList(catalog3Id);
        return baseAttrInfos;
    }

    @RequestMapping("/saveAttr")
    @ResponseBody
    public String saveAttr(BaseAttrInfo baseAttrInfo){

        baseAttrService.saveAttr(baseAttrInfo);

        return  "SUCCESS";
    }

    @RequestMapping("/getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
        List<BaseAttrValue> baseAttrValues = baseAttrService.getAttrValueList(attrId);
        return  baseAttrValues;
    }
    @RequestMapping("/baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getSaleAttrList(){
        return baseAttrService.getSaleAttrList();
    }
}

