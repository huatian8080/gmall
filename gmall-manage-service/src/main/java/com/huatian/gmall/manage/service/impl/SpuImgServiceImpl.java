package com.huatian.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huatian.gmall.bean.SpuImage;
import com.huatian.gmall.manage.mapper.SpuImgMapper;
import com.huatian.gmall.service.SpuImgService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuImgServiceImpl implements SpuImgService{
    @Autowired
    SpuImgMapper spuImgMapper;

    @Override
    public List<SpuImage> getSpuImgList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImgMapper.select(spuImage);
        return spuImageList;
    }
}
