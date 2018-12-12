package com.huatian.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huatian.gmall.bean.BaseCatalog1;
import com.huatian.gmall.bean.BaseCatalog2;
import com.huatian.gmall.bean.BaseCatalog3;
import com.huatian.gmall.manage.mapper.BaseCatalog1Mapper;
import com.huatian.gmall.manage.mapper.BaseCatalog2Mapper;
import com.huatian.gmall.manage.mapper.BaseCatalog3Mapper;
import com.huatian.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> catalog1s = baseCatalog1Mapper.selectAll();
        return catalog1s;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 catalog2 = new BaseCatalog2();
        catalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> catalog2s = baseCatalog2Mapper.select(catalog2);
        return catalog2s;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3s = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3s;
    }
}
