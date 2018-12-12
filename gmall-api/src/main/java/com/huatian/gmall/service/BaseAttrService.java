package com.huatian.gmall.service;

import com.huatian.gmall.bean.BaseAttrInfo;
import com.huatian.gmall.bean.BaseAttrValue;
import com.huatian.gmall.bean.BaseSaleAttr;

import java.util.List;
import java.util.Set;

public interface BaseAttrService {
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    void saveAttr(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(String attrId);

    List<BaseSaleAttr> getSaleAttrList();

    List<BaseAttrInfo> getAttrInfoList(String catalog3Id);

    void delAttrInfo(String attrId);

    List<BaseAttrInfo> getAttrListByValueIds(Set<String> valueIds);
}
