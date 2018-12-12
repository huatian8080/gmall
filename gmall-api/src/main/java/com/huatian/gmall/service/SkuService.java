package com.huatian.gmall.service;

import com.huatian.gmall.bean.SkuInfo;

import java.util.List;

public interface SkuService {
    List<SkuInfo> getSkuList(String spuId);

    void saveSku(SkuInfo skuInfo);

    SkuInfo getSkuById(String skuId);

    List<SkuInfo> getSkuAttrValueListBySpuId(String spuId);

    List<SkuInfo> getMySkuInfoList(String catalog3Id);
}
