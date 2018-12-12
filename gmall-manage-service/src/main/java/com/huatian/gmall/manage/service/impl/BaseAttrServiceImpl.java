package com.huatian.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huatian.gmall.bean.BaseAttrInfo;
import com.huatian.gmall.bean.BaseAttrValue;
import com.huatian.gmall.bean.BaseSaleAttr;
import com.huatian.gmall.manage.mapper.BaseAttrInfoMapper;
import com.huatian.gmall.manage.mapper.BaseAttrValueMapper;
import com.huatian.gmall.manage.mapper.BaseSaleAttrMapper;
import com.huatian.gmall.service.BaseAttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Service
public class BaseAttrServiceImpl implements BaseAttrService {
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfos;
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null && !"".equals(baseAttrInfo.getId())){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
            //删除原有的attrValue
            BaseAttrValue baseAttrValue1 = new BaseAttrValue();
            baseAttrValue1.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.delete(baseAttrValue1);
            //添加新增的attrValue
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }else {
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.select(baseAttrValue);
        return baseAttrValues;
    }

    @Override
    public List<BaseSaleAttr> getSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> attrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        for (BaseAttrInfo attrInfo : attrInfoList) {
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(attrInfo.getId());
            List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
            attrInfo.setAttrValueList(attrValueList);
        }
        return  attrInfoList;
    }

    @Override
    public void delAttrInfo(String attrId) {
        baseAttrInfoMapper.deleteByPrimaryKey(attrId);
    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(Set<String> valueIds) {
        String join = StringUtils.join(valueIds, ",");
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrListByValueIds(join);
        return baseAttrInfoList;
    }
}
