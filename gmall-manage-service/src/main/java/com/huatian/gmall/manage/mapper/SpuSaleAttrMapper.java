package com.huatian.gmall.manage.mapper;

import com.huatian.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    List<SpuSaleAttr> selectSpuSaleAttrListBySpuId(String skuId,String spuId);
}
