package com.huatian.gmall.manage.mapper;

import com.huatian.gmall.bean.SkuInfo;
import com.huatian.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuInfo> selectSkuAttrValueListBySpuId(String spuId);
}
