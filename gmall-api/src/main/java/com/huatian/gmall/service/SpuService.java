package com.huatian.gmall.service;

import com.huatian.gmall.bean.SpuInfo;
import com.huatian.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface SpuService {
    List<SpuInfo> getSpuList(String catalog3Id);

    void saveSpu(SpuInfo spuInfo);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String skuId,String spuId);

    void delSpu(String id);
}
