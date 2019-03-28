package com.huatian.gmall.service;

import com.huatian.gmall.bean.SkuLsInfo;
import com.huatian.gmall.bean.SkuLsParam;
import com.huatian.gmall.bean.SkuLsResult;

import java.util.List;

public interface ListService {
    List<SkuLsInfo> search(SkuLsParam skuLsParam);

    void saveSkuLsInfoToList(SkuLsInfo skuLsInfo);
}
