package com.huatian.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huatian.gmall.bean.*;
import com.huatian.gmall.service.BaseAttrService;
import com.huatian.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    BaseAttrService baseAttrService;

    @RequestMapping("/index")
    public String toIndex(){
        return "index";
    }

    @RequestMapping("/list.html")
    public String toList(SkuLsParam skuLsParam,Model model){
        //调用商品的搜索服务
        List<SkuLsInfo> skuLsInfoList = listService.search(skuLsParam);;

        Set<String> valueIds = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfoList) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                valueIds.add(valueId);
            }
        }
        //根据sku属性列表的值查询出的属性列表集合
        List<BaseAttrInfo> baseAttrInfoList = baseAttrService.getAttrListByValueIds(valueIds);
        //删除已选择过的属性值的属性列表
        String[] delValueIds = skuLsParam.getValueId();
        if (delValueIds != null && delValueIds.length > 0){
            //面包屑
            List<Crumb> crumbList = new ArrayList<>();
            for (String delValueId : delValueIds) {
                Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator();

                loop: while (iterator.hasNext()){
                    BaseAttrInfo baseAttrInfo = iterator.next();
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                    for (BaseAttrValue baseAttrValue : attrValueList) {
                        String valueId = baseAttrValue.getId();
                        if (valueId.equals(delValueId)){
                            Crumb crumb = new Crumb();
                            String myCrumbUrl = getMyCrumbUrl(skuLsParam, delValueId);
                            crumb.setUrlParam(myCrumbUrl);
                            crumb.setValueName(baseAttrValue.getValueName());
                            crumbList.add(crumb);
                            iterator.remove();
                            continue loop;
                        }
                    }
                }
            }
            model.addAttribute("attrValueSelectedList",crumbList);
        }

        model.addAttribute("skuLsInfoList",skuLsInfoList);
        model.addAttribute("attrList",baseAttrInfoList);
        String myUrlParam = getMyUrlParam(skuLsParam);
        model.addAttribute("urlParam",myUrlParam);
        return "list";
    }
    public String getMyCrumbUrl(SkuLsParam skuLsParam,String delValueId){
        String urlParam = "";
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueIds = skuLsParam.getValueId();
        if (StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (valueIds != null){
            for (String valueId : valueIds) {
                if (!valueId.equals(delValueId)){
                    urlParam = urlParam +"&valueId="+valueId;
                }
            }
        }
        return urlParam;
    }

    public String getMyUrlParam(SkuLsParam skuLsParam){
        String urlParam = "";
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueIds = skuLsParam.getValueId();
        if (StringUtils.isNotBlank(catalog3Id)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)){
            if (StringUtils.isNotBlank(urlParam)){
                urlParam = "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (valueIds != null){
            for (String valueId : valueIds) {
                urlParam = urlParam +"&valueId="+valueId;
            }
        }
        return urlParam;
    }
}
