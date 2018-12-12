package com.huatian.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huatian.gmall.bean.SpuImage;
import com.huatian.gmall.bean.SpuInfo;
import com.huatian.gmall.bean.SpuSaleAttr;
import com.huatian.gmall.bean.SpuSaleAttrValue;
import com.huatian.gmall.manage.mapper.SpuImageMapper;
import com.huatian.gmall.manage.mapper.SpuInfoMapper;
import com.huatian.gmall.manage.mapper.SpuSaleAttrMapper;
import com.huatian.gmall.manage.mapper.SpuSaleAttrValueMapper;
import com.huatian.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfos = spuInfoMapper.select(spuInfo);
        return spuInfos;
    }

    @Override
    public void saveSpu(SpuInfo spuInfo) {
        spuInfoMapper.insertSelective(spuInfo);
        //保存图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insertSelective(spuImage);
        }
        //保存销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            //保存销售属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.select(spuSaleAttr);
        for (SpuSaleAttr saleAttr : spuSaleAttrList) {
            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
            spuSaleAttrValue.setSpuId(spuId);
            spuSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
            List<SpuSaleAttrValue> select = spuSaleAttrValueMapper.select(spuSaleAttrValue);
            saleAttr.setSpuSaleAttrValueList(select);
        }
        return spuSaleAttrList;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String skuId,String spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrListBySpuId(skuId,spuId);
        return spuSaleAttrList;
    }

    @Override
    public void delSpu(String id) {
        spuInfoMapper.deleteByPrimaryKey(id);
    }
}
